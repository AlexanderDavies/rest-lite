package com.adavie.util;

import com.adavie.server.config.ThreadPoolConfig;

import java.util.concurrent.*;

public class ThreadPoolFactory {

  public static ThreadPoolExecutor newThreadPool(ThreadPoolConfig threadPoolConfig) {
    return new ThreadPoolExecutor(threadPoolConfig.getMinPoolSize(), threadPoolConfig.getMaxPoolSize(), threadPoolConfig.getKeepAliveSeconds(), TimeUnit.SECONDS, new LinkedBlockingQueue<>(threadPoolConfig.getQueueSize()));
  }
}
