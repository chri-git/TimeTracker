package kri.app.timetracker.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Utility to run stuff asynchronously
 */
public class TaskRunner {

    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface VoidCallback {
        void onComplete();
    }

    public interface Callback<R> {
        void onComplete(R result);
    }

    /**
     * Asynchronously executes the callable and returns a result to the callback when completed
     *
     * @param callable a {@link Callable} holding the code to be executed
     * @param callback a {@link Callback} to be notified with the result of the callable once it is done or with
     *                 <code>null</code> in case the callable throws an exception
     * @param <R>      the return type of the callable
     */
    public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
        executor.execute(() -> {
            final R result;

            try {
                result = callable.call();
                handler.post(() -> callback.onComplete(result));
            } catch (Exception e) {
                Log.e("Runner", "Exception when running async task: " + e);
                callback.onComplete(null);
            }
        });
    }

    /**
     * Asynchronously executes the callable and notifies the callback once it is complete
     *
     * @param runnable a {@link Runnable} holding the code to be executed
     * @param callback a {@link VoidCallback} to be notified when the runnable has finished
     */
    public void executeAsyncVoid(Runnable runnable, VoidCallback callback) {
        executor.execute(() -> {
            runnable.run();
            handler.post(callback::onComplete);
        });
    }
}