package me.vrekt.queuesniper.async;

import me.vrekt.queuesniper.guild.database.GuildDatabase;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Concurrent {

    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService SCHEDULED = Executors.newSingleThreadScheduledExecutor();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<Runnable, Task> TASKS = new HashMap<>();

    private static final long HOUR = Duration.of(1, ChronoUnit.HOURS).toMillis();
    private static long lastBackup = System.currentTimeMillis();

    public static void start() {
        SCHEDULED.scheduleWithFixedDelay(Concurrent::update, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a task async
     *
     * @param runnable the task
     */
    public static void runAsync(Runnable runnable) {
        SERVICE.execute(runnable);
    }

    /**
     * Runs a task async later
     *
     * @param runnable the task
     * @param delay    the amount of time to wait (in millis)
     */
    public static void runAsyncLater(Runnable runnable, long delay, long id) {
        TASKS.put(runnable, new Task(delay, id, false));
    }

    /**
     * Cancels a task
     *
     * @param id the id
     */
    public static void cancelTask(long id) {
        TASKS.entrySet().removeIf(entry -> entry.getValue().getId() == id);
    }

    /**
     * Runs the task every X milliseconds async
     *
     * @param runnable the task
     * @param delay    the amount of time to wait between execution (in millis)
     */
    public static void runAsyncTimer(Runnable runnable, long delay) {
        TASKS.put(runnable, new Task(delay, delay, true));
    }

    /**
     * Runs the queued tasks if they are ready and runs other internal stuff if ready
     */
    private static void update() {
        try {
            var now = System.currentTimeMillis();

            // handle backups
            if (now - lastBackup >= HOUR) {
                runAsync(GuildDatabase::backup);
                lastBackup = now;
            }

            TASKS.entrySet().removeIf(entry -> {
                Task task = entry.getValue();
                if (task.isTimer()) {
                    // update
                    if (task.isReady(now)) {
                        task.setQueued(now);
                        SERVICE.execute(entry.getKey());
                    }
                    return false;
                } else {
                    if (entry.getValue().isReady(now)) {
                        SERVICE.execute(entry.getKey());
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            //
        }
    }

    /**
     * A class representing a task
     * Tracks when it was queued as well as delays/timer
     */
    private static class Task {

        private final long delay, id;
        private final boolean timer;

        private long queued;

        Task(long delay, long id, boolean timer) {
            this.queued = System.currentTimeMillis();
            this.delay = delay;
            this.id = id;
            this.timer = timer;
        }

        boolean isTimer() {
            return timer;
        }

        void setQueued(long queued) {
            this.queued = queued;
        }

        boolean isReady(long now) {
            return now - queued >= delay;
        }

        long getId() {
            return id;
        }
    }

}
