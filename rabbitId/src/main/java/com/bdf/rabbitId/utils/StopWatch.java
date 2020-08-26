package com.bdf.rabbitId.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author YangGuodong
 * @date: 2019-08-21
 */
public class StopWatch {
    /** Name of the current task */
    private String currentTaskName;

    private final Ticker ticker;
    private final List<TaskInfo> taskList = new LinkedList<>();

    private boolean keepTaskList = true;
    private boolean isRunning;
    private long elapsedNanos;
    private long startTick;
    private TaskInfo lastTaskInfo;


    /**
     * Construct a new stop watch with the given id.
     * Does not start any task.
     * Handy when we have output from multiple stop watches
     * and need to distinguish between them.
     */
    public StopWatch() {
        this(Ticker.systemTicker());
    }

    /**
     * Construct a new stop watch with the given id.
     * Does not start any task.
     * @param ticker the specified time source {@link StopWatch.Ticker}
     * Handy when we have output from multiple stop watches
     * and need to distinguish between them.
     */
    public StopWatch(Ticker ticker){
        this.ticker = ticker;
    }

    /**
     * Creates (but does not start) a new stopwatch using {@link System#nanoTime}
     * as its time source.
     *
     */
    public static StopWatch createUnstarted() {
        return new StopWatch();
    }

    /**
     * Creates (and starts) a new stopwatch using {@link System#nanoTime}
     * as its time source.
     * @return
     */
    public static StopWatch createStarted(){
        return new StopWatch().start();
    }

    /**
     * Start a named task. The results are undefined if {@link #stop()}
     * or timing methods are called without invoking this method.
     * @param taskName the name of the task to start
     * @see #stop()
     */
    public StopWatch start(String taskName) throws IllegalStateException {
        Assert.checkState(!isRunning, "This stopwatch is already running.");
        this.currentTaskName = taskName;
        isRunning = true;
        startTick = ticker.read();
        return this;
    }

    /**
     * Start an unnamed task. The results are undefined if {@link #stop()}
     * or timing methods are called without invoking this method.
     *
     * @return this {@code Stopwatch} instance
     * @throws IllegalStateException if the stopwatch is already running.
     */
    public StopWatch start() {
        return this.start("");
    }

    /**
     * Stops the stopwatch. Future reads will return the fixed duration that had
     * elapsed up to this point.
     *
     * @return this {@code Stopwatch} instance
     * @throws IllegalStateException if the stopwatch is already stopped.
     */
    public StopWatch stop() {
        Assert.checkState(isRunning, "This stopwatch is already stopped.");
        long tick = ticker.read();
        isRunning = false;
        elapsedNanos += tick - startTick;
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, tick - startTick);
        if (this.keepTaskList) {
            this.taskList.add(lastTaskInfo);
        }
        return this;
    }


    private long elapsedNanos() {
        return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
    }

    /**
     * Returns the current elapsed time shown on this stopwatch, expressed
     * in the desired time unit, with any fraction rounded down.
     *
     * <p>Note that the overhead of measurement can be more than a microsecond, so
     * it is generally not useful to specify {@link TimeUnit#NANOSECONDS}
     * precision here.
     */
    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    }


    @Override public String toString() {
        long nanos = elapsedNanos();
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);
        StringBuilder sb = new StringBuilder(String.format("StopWatch %s: running time %.4g %s",currentTaskName,value, abbreviate(unit)));
        sb.append("\n");
        if (this.keepTaskList) {
            for (TaskInfo task : taskList) {
                unit = chooseUnit(task.getElapsedNanos());
                double elapsed = (double) task.getElapsedNanos() / NANOSECONDS.convert(1, unit);
                sb.append(task.getTaskName()).append(" took ").append(elapsed).append(" ").append(abbreviate(unit));
                double percent = Math.round((100.00 * task.getElapsedNanos()) / elapsedNanos());
                sb.append(" = ").append(percent).append("%").append(";\n");
            }
        }
        return sb.toString();
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                // μs
                return "\u03bcs";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }

    /**
     * A time source; returns a time value representing the number of nanoseconds elapsed since some
     * fixed but arbitrary point in time. Note that most users should use {@link StopWatch} instead of
     * interacting with this class directly.
     */
    interface Ticker{
        long read();

        /**
         * A ticker that reads the current time using {@link System#nanoTime}.
         */
        static Ticker systemTicker(){
            return SYSTEM_TICKER;
        }
        Ticker SYSTEM_TICKER = () -> System.nanoTime();
    }

    /**
     * Inner class to hold data about one task executed within the stop watch.
     */
    final class TaskInfo {

        private final String taskName;

        private final long elapsedNanos;

        TaskInfo(String taskName, long timeMillis) {
            this.taskName = taskName;
            this.elapsedNanos = timeMillis;
        }

        /**
         * Return the name of this task.
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * Return the elapsed time in nanos this task took.
         */
        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }
}
