package com.team_one.soen_345_project.util;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class InstantExecutorExtension extends TestWatcher {
    @Override
    protected void starting(Description description) {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(@NonNull Runnable runnable) {
                runnable.run();
            }

            @Override
            public void postToMainThread(@NonNull Runnable runnable) {
                runnable.run();
            }

            @Override
            public boolean isMainThread() {
                return true;
            }
        });
    }

    @Override
    protected void finished(Description description) {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }
}