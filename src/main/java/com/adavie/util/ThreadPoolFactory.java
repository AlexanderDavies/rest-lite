package com.adavie.util;

import com.adavie.config.ThreadPoolConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ThreadPoolFactory {
  private static final Logger logger = Logger.getLogger(ThreadPoolFactory.class.getName());
  private static final MethodHandle VIRTUAL_THREAD_EXECUTOR_FACTORY;

  static {
    MethodHandle handle = null;
    try {
      handle = MethodHandles.lookup().findStatic(
          Executors.class,
          "newVirtualThreadPerTaskExecutor",
          MethodType.methodType(ExecutorService.class)
      );
    } catch (NoSuchMethodException | IllegalAccessException e) {
      // Virtual threads not available
    }
    VIRTUAL_THREAD_EXECUTOR_FACTORY = handle;
  }

  public static ExecutorService newExecutorService(ThreadPoolConfig config) {
    if (config.isVirtualThreads() && VIRTUAL_THREAD_EXECUTOR_FACTORY != null) {
      if (config.isDefault()) {
        logger.warning(
            "Virtual threads are enabled. Pool configuration (minPoolSize="
                + config.getMinPoolSize() + ", maxPoolSize=" + config.getMaxPoolSize()
                + ", queueSize=" + config.getQueueSize() + ", keepAliveSeconds="
                + config.getKeepAliveSeconds() + ") will be ignored. "
                + "Set virtualThreads(false) to use platform thread pool configuration."
        );
      }
      return newVirtualThreadExecutor();
    }

    // Fall back to platform threads
    return new ThreadPoolExecutor(
        config.getMinPoolSize(),
        config.getMaxPoolSize(),
        config.getKeepAliveSeconds(),
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(config.getQueueSize())
    );
  }

  private static ExecutorService newVirtualThreadExecutor() {
    try {
      return (ExecutorService) VIRTUAL_THREAD_EXECUTOR_FACTORY.invoke();
    } catch (Throwable e) {
      throw new RuntimeException("Failed to create virtual thread executor", e);
    }
  }
}
