package com.team_one.soen_345_project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.team_one.soen_345_project.util.DisableAnimationsRule;
import com.team_one.soen_345_project.view.LoginActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BookEventFlowTest {

    // Disable animations to make Espresso clicks stable.
    @Rule
    public final DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private long testStartMs;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @After
    public void cleanupReservationsCreatedByTest() throws Exception {
        // If we never got far enough to login, nothing to cleanup.
        if (testStartMs <= 0) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) return;

        // Find reservations created since the test started.
        Timestamp startTs = new Timestamp(new java.util.Date(testStartMs));

        // Avoid composite index requirements by only querying on userId, then filtering client-side.
        QuerySnapshot reservationsSnap = Tasks.await(
                firestore.collection("reservation")
                        .whereEqualTo("userId", uid)
                        .get()
        );

        List<DocumentSnapshot> reservations = reservationsSnap.getDocuments();
        if (reservations.isEmpty()) return;

        // For each reservation created since the test started, decrement the event reservations and delete the reservation.
        for (DocumentSnapshot reservationDoc : reservations) {
            Timestamp ts = reservationDoc.getTimestamp("timestamp");
            if (ts == null || ts.compareTo(startTs) < 0) {
                continue;
            }

            String eventId = reservationDoc.getString("eventId");
            if (eventId == null) {
                // Still delete reservation doc.
                Tasks.await(reservationDoc.getReference().delete());
                continue;
            }

            DocumentReference eventRef = firestore.collection("event").document(eventId);
            DocumentReference reservationRef = reservationDoc.getReference();

            Tasks.await(firestore.runTransaction(transaction -> {
                DocumentSnapshot eventSnap = transaction.get(eventRef);
                Long capacity = eventSnap.getLong("capacity");
                Long reservationsCount = eventSnap.getLong("reservations");

                long cap = capacity != null ? capacity : 0L;
                long res = reservationsCount != null ? reservationsCount : 0L;

                long newRes = Math.max(0L, res - 1L);
                boolean isFull = cap > 0 && newRes >= cap;

                Map<String, Object> updates = new HashMap<>();
                updates.put("reservations", newRes);
                updates.put("full", isFull);
                transaction.update(eventRef, updates);
                transaction.delete(reservationRef);
                return null;
            }));
        }
    }

    @Test
    public void openEvent_clickBook_showsSuccess_andDecrementsAvailableSeats() {
        // Track test start so we can cleanup any created bookings afterwards.
        testStartMs = System.currentTimeMillis();

        // Login with a real Firebase test account (same as existing test)
        onView(withId(R.id.editTextEmail))
                .perform(typeText("ryan123@gmail.com"), closeSoftKeyboard());

        onView(withId(R.id.editTextPassword))
                .perform(typeText("123456"), closeSoftKeyboard());

        onView(withId(R.id.buttonLogin)).perform(click());

        // Wait for user dashboard (login redirects to UserDashActivity for non-admin users)
        waitForView(withId(R.id.btnSeeAllEvents), 15_000);

        // Go to All Events
        onView(withId(R.id.btnSeeAllEvents)).perform(click());

        waitForView(withId(R.id.rvAllEvents), 10_000);

        // Click an event that is actually bookable (Book Now becomes visible)
        final int selectedIndex = findAndOpenFirstBookableEventOrFail(50);

        // Bottom sheet should appear
        waitForView(withId(R.id.tvSheetEventTitle), 5_000);

        // Wait until Book Now is actually visible/clickable (it is toggled asynchronously)
        waitForView(withId(R.id.btnBookNow), 10_000);

        // Read available seats from the capacity label "Capacity: X (Y available)"
        final int[] beforeAvailable = new int[1];
        onView(withId(R.id.tvSheetEventCapacity)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "read available seats";
            }

            @Override
            public void perform(UiController uiController, View view) {
                String text = ((android.widget.TextView) view).getText().toString();
                beforeAvailable[0] = parseAvailable(text);
            }
        });

        // Must have at least 1 available seat for this test
        if (beforeAvailable[0] <= 0) {
            throw new AssertionError("Selected event has no available seats. Ensure test data has an available event at position 0.");
        }

        // Click Book Now
        onView(withId(R.id.btnBookNow)).perform(click());

        // Booking success is verified via UI state change (toast matching is flaky across devices)
        // Wait for bottom sheet to dismiss by waiting for the list to be visible again.
        waitForView(withId(R.id.rvAllEvents), 15_000);

        // Bottom sheet dismissed on success; reopen SAME item to check updated availability
        onView(withId(R.id.rvAllEvents)).perform(clickRecyclerViewItemAtPosition(selectedIndex));

        waitForView(withId(R.id.tvSheetEventCapacity), 5_000);

        final int[] afterAvailable = new int[1];
        onView(withId(R.id.tvSheetEventCapacity)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "read available seats after booking";
            }

            @Override
            public void perform(UiController uiController, View view) {
                String text = ((android.widget.TextView) view).getText().toString();
                afterAvailable[0] = parseAvailable(text);
            }
        });

        if (afterAvailable[0] != beforeAvailable[0] - 1) {
            throw new AssertionError("Expected available seats to decrease by 1. Before=" + beforeAvailable[0] + " After=" + afterAvailable[0]);
        }
    }

    private static int parseAvailable(@NonNull String capacityText) {
        // Expected: "Capacity: X (Y available)"
        // We'll extract Y.
        try {
            int open = capacityText.indexOf('(');
            int space = capacityText.indexOf(' ', open + 1);
            if (open < 0 || space < 0) return -1;
            String num = capacityText.substring(open + 1, space).trim();
            return Integer.parseInt(num);
        } catch (Exception e) {
            return -1;
        }
    }

    private static void waitForView(Matcher<View> matcher, long timeoutMs) {
        long start = SystemClock.elapsedRealtime();
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            try {
                onView(matcher).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError ignored) {
                SystemClock.sleep(250);
            }
        }
        throw new AssertionError("Timed out waiting for view: " + matcher);
    }


    private static ViewAction clickFirstRecyclerViewItem() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "click first RecyclerView item";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!(view instanceof androidx.recyclerview.widget.RecyclerView)) {
                    throw new AssertionError("View is not a RecyclerView");
                }
                androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) view;
                uiController.loopMainThreadUntilIdle();

                androidx.recyclerview.widget.RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(0);
                if (vh == null) {
                    uiController.loopMainThreadForAtLeast(500);
                    vh = rv.findViewHolderForAdapterPosition(0);
                }
                if (vh == null) {
                    throw new AssertionError("No ViewHolder at position 0");
                }

                // Use Espresso click action (not raw performClick) so it doesn't depend on hidden input APIs.
                actionWithAssertions(click()).perform(uiController, vh.itemView);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Opens events until it finds one where btnBookNow becomes visible.
     * This is more reliable than checking the status pill because the bottom sheet
     * decides Book Now visibility asynchronously (already-booked/full checks).
     */
    private static int findAndOpenFirstBookableEventOrFail(int maxScan) {
        waitForRecyclerViewItemCountAtLeast(R.id.rvAllEvents, 1, 20_000);

        for (int i = 0; i < maxScan; i++) {
            // Ensure RV has enough items for this index; if not, wait for loads.
            waitForRecyclerViewItemCountAtLeast(R.id.rvAllEvents, i + 1, 20_000);

            // Open item
            onView(withId(R.id.rvAllEvents)).perform(clickRecyclerViewItemAtPosition(i));

            // Wait for sheet root
            waitForView(withId(R.id.tvSheetEventTitle), 5_000);

            // Now wait briefly for one of the terminal states.
            SheetState state = waitForSheetTerminalState(6_000);
            if (state == SheetState.BOOKABLE) {
                return i;
            }

            // Not bookable -> close and continue.
            androidx.test.espresso.Espresso.pressBack();
            SystemClock.sleep(300);
        }

        throw new AssertionError("No BOOKABLE event found in the first " + maxScan + " items. Ensure there is an event not booked by this user and not full.");
    }

    private enum SheetState { BOOKABLE, ALREADY_BOOKED, FULL, UNKNOWN }

    private static SheetState waitForSheetTerminalState(long timeoutMs) {
        long start = SystemClock.elapsedRealtime();
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            // Bookable
            if (isViewDisplayedNow(R.id.btnBookNow)) return SheetState.BOOKABLE;

            // Not bookable cases
            if (isViewDisplayedNow(R.id.tvAlreadyBookedMessage)) return SheetState.ALREADY_BOOKED;
            if (isViewDisplayedNow(R.id.tvEventFullMessage)) return SheetState.FULL;

            SystemClock.sleep(250);
        }
        return SheetState.UNKNOWN;
    }

    private static boolean isViewDisplayedNow(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void waitForRecyclerViewItemCountAtLeast(int recyclerViewId, int minCount, long timeoutMs) {
        long start = SystemClock.elapsedRealtime();
        final int[] count = new int[1];
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            try {
                onView(withId(recyclerViewId)).perform(getRecyclerViewItemCount(count));
                if (count[0] >= minCount) return;
            } catch (Throwable ignored) {
                // ignore and retry
            }
            SystemClock.sleep(250);
        }
        throw new AssertionError("Timed out waiting for RecyclerView itemCount >= " + minCount + ". Last=" + count[0]);
    }

    private static ViewAction getRecyclerViewItemCount(final int[] outCount) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "get RecyclerView item count";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!(view instanceof androidx.recyclerview.widget.RecyclerView)) {
                    throw new AssertionError("View is not a RecyclerView");
                }
                androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) view;
                if (rv.getAdapter() == null) {
                    outCount[0] = 0;
                    return;
                }
                outCount[0] = rv.getAdapter().getItemCount();
            }
        };
    }

    private static ViewAction clickRecyclerViewItemAtPosition(int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "click RecyclerView item at position " + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!(view instanceof androidx.recyclerview.widget.RecyclerView)) {
                    throw new AssertionError("View is not a RecyclerView");
                }
                androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) view;

                if (rv.getAdapter() == null) {
                    throw new AssertionError("RecyclerView has no adapter");
                }
                int count = rv.getAdapter().getItemCount();
                if (position < 0 || position >= count) {
                    throw new AssertionError("Position " + position + " out of bounds. itemCount=" + count);
                }

                // Scroll to ensure the ViewHolder is bound
                rv.scrollToPosition(position);
                uiController.loopMainThreadUntilIdle();

                androidx.recyclerview.widget.RecyclerView.ViewHolder vh = null;
                long start = SystemClock.elapsedRealtime();
                while (SystemClock.elapsedRealtime() - start < 3000 && vh == null) {
                    vh = rv.findViewHolderForAdapterPosition(position);
                    if (vh == null) {
                        uiController.loopMainThreadForAtLeast(250);
                    }
                }

                if (vh == null) {
                    throw new AssertionError("No ViewHolder at position " + position);
                }

                actionWithAssertions(click()).perform(uiController, vh.itemView);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
