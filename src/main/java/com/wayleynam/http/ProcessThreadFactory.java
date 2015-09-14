package com.wayleynam.http;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wei4liverpool on 9/11/15. 线程工厂;
 */
public class ProcessThreadFactory implements ThreadFactory {

  private static AtomicInteger threadNum = new AtomicInteger(1);
  private String threadPrefix;
  private ThreadGroup group;

  public ProcessThreadFactory() {
    threadPrefix = "process_thread_";
    SecurityManager s = System.getSecurityManager();
    group = group != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();

  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(group, r, threadPrefix + threadNum.incrementAndGet(), 0);
    t.setPriority(Thread.MAX_PRIORITY);
    if (t.isDaemon()) {
      t.setDaemon(false);
    }
    return t;
  }
}
