package edu.slz.javalab.plainvanillajava;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadBomb {

  public static void main(String[] args) {
    System.out.println("Thread bomb armed!");

    executeThreadPool();

    System.exit(0);
  }

  private static void executeThreadPool() {

    int corePoolSize = 3000;
    int maxPoolSize = 5000;
    long keepAliveTimeSecs = 30;

    ThreadPoolExecutor execSrvc =
      new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeSecs, TimeUnit.SECONDS, new SynchronousQueue<>());

    final AtomicInteger countExecs = new AtomicInteger();

    for (int i = 0; i < maxPoolSize; i++) {
      execSrvc.execute(() -> {
        System.out.printf("Running task #%d. Pool size: %d%n", countExecs.incrementAndGet(), execSrvc.getPoolSize());

        try {
          Thread.sleep(30000L);
        } catch (InterruptedException interruptedException) {
          System.out.println("Interrupted while sleeping");
        }
      });
    }

    System.out.println("ThreadPool execution completed");
  }

}
