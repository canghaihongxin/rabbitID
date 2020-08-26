package com.bdf.rabbitId.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 田培融
 */
public class NamedThreadFactory implements ThreadFactory{
    private final AtomicLong POOL_SEQ = new AtomicLong(1);

    private final String namePrefix;

    private final boolean daemon;

    private final ThreadGroup threadGroup = new ThreadGroup("extrasky");


    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix + "-thread-";
        this.daemon = daemon;
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory() {
        this("pool", false);
    }

    public static ThreadFactory create(String namePrefix, boolean daemon) {
        return new NamedThreadFactory(namePrefix, daemon);
    }

    @Override
    public Thread newThread(Runnable runnable) {

        Thread thread = new Thread(threadGroup, runnable,
                threadGroup.getName() + "-" + namePrefix + "-" + POOL_SEQ.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
