package com.team_one.soen_345_project.util;

import android.app.UiAutomation;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

/**
 * Disables system animations during instrumentation tests to avoid Espresso PerformException:
 * "Animations or transitions are enabled on the target device".
 *
 * Requires emulator/device to allow shell commands.
 */
public class DisableAnimationsRule extends ExternalResource {

    private Float window;
    private Float transition;
    private Float animator;

    @Override
    protected void before() {
        // Read current values and then disable.
        window = getScale("window_animation_scale");
        transition = getScale("transition_animation_scale");
        animator = getScale("animator_duration_scale");

        setScale("window_animation_scale", 0f);
        setScale("transition_animation_scale", 0f);
        setScale("animator_duration_scale", 0f);
    }

    @Override
    protected void after() {
        if (window != null) setScale("window_animation_scale", window);
        if (transition != null) setScale("transition_animation_scale", transition);
        if (animator != null) setScale("animator_duration_scale", animator);
    }

    private static void setScale(@NonNull String key, float value) {
        executeShell("settings put global " + key + " " + value);
    }

    private static Float getScale(@NonNull String key) {
        try {
            String out = executeShell("settings get global " + key);
            return Float.parseFloat(out.trim());
        } catch (Throwable t) {
            return null;
        }
    }

    private static String executeShell(@NonNull String command) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return "";

        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        ParcelFileDescriptor pfd = null;
        try {
            pfd = automation.executeShellCommand(command);
            try (ParcelFileDescriptor.AutoCloseInputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pfd)) {
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            }
        } catch (Exception e) {
            if (pfd != null) {
                try { pfd.close(); } catch (Exception ignored) {}
            }
            return "";
        }
    }
}
