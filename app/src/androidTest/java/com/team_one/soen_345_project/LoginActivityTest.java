package com.team_one.soen_345_project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.team_one.soen_345_project.view.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginFlow_navigatesToMainActivity() throws InterruptedException {
        // 1. Type valid email
        onView(withId(R.id.editTextEmail))
                .perform(typeText("ammar.ranko707@gmail.com"), closeSoftKeyboard());

        // 2. Type valid password
        onView(withId(R.id.editTextPassword))
                .perform(typeText("test123"), closeSoftKeyboard());

        // 3. Click the Login button
        onView(withId(R.id.buttonLogin))
                .perform(click());

        /* 4. Since we are testing with a real Firebase connection, the time between
        sending the login request to Firebase and receiving the response takes a few seconds.
        However, Espresso expects the Main Page to appear immediately. Therefore, we introduce a short wait to allow the response
        to be received, the login to complete, and the Main Page to be displayed.
        If the Main Page changes in the future, the test should be updated accordingly
         */

        Thread.sleep(5000);

        // 5. Verify that the Dashboard is displayed
        onView(withId(R.id.dashboardTitle)).check(matches(isDisplayed()));
    }
}
