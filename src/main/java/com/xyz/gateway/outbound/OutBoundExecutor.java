package com.xyz.gateway.outbound;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OutBoundExecutor {

    private static final ExecutorService proxyService;

    static {
        int cores = 10;
        long keepAliveTime = 1000;
        int queueSize = 5000;
        RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        proxyService = new ThreadPoolExecutor(cores,
                cores,
                keepAliveTime, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new HttpThreadFactory("proxyService"),
                rejectHandler);
    }

    public static ExecutorService getProxyService() {
        return proxyService;
    }


    public static class HttpThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;
        private final boolean daemon;

        public HttpThreadFactory(String namePrefix, boolean daemon) {
            this.daemon = daemon;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public HttpThreadFactory(String namePrefix) {
            this(namePrefix, false);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + "-thread-" + threadNumber.getAndIncrement(), 0);
            t.setDaemon(daemon);
            return t;
        }

    }

}
