package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * santiwk at stackoverflow.com
 */
public class Loop {

  private static final int CPUs = Runtime.getRuntime().availableProcessors();
  //private static ExecutorService executor = Executors.newCachedThreadPool();
  private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(CPUs, CPUs, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  static {
    executor.allowCoreThreadTimeOut(true);
  }
  
  public interface BodyWithIndex {
    
    void run(int i);
  }
  
  public static void withIndex(int start, int stop, final BodyWithIndex body) {
    int chunksize = (stop - start + CPUs - 1) / CPUs;
    int loops = (stop - start + chunksize - 1) / chunksize;
    final CountDownLatch latch = new CountDownLatch(loops);
    for (int i = start; i < stop;) {
      final int lo = i;
      i += chunksize;
      final int hi = (i < stop) ? i : stop;
      executor.submit(new Runnable() {
        @Override
        public void run() {
          for (int i = lo; i < hi; i++) {
            body.run(i);
          }
          latch.countDown();
        }
      });
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
    }
  }
  
  public static void shutdown(){
    executor.shutdown();
  }
}
