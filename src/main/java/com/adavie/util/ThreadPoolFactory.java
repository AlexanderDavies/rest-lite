package com.adavie.util;

import java.util.concurrent.*;

public class ThreadPoolFactory {

  public static final int DEFAULT_THREAD_COUNT = 100;

  public static ThreadPoolExecutor newThreadPool() {
      return new ThreadPoolExecutor(50, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50));

  }
}
