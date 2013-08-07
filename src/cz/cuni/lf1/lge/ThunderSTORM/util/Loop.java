package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    int chunksize = Math.max((stop - start + CPUs - 1) / CPUs, 1);
    int loops = (stop - start + chunksize - 1) / chunksize;
    Future<?>[] futures = new Future<?>[loops];
    int fi = 0;
    for (int i = start; i < stop;) {
      final int lo = i;
      i += chunksize;
      final int hi = (i < stop) ? i : stop;
      futures[fi++] = executor.submit(new Runnable() {
        @Override
        public void run() {
          for (int i = lo; i < hi; i++) {
            body.run(i);
          }
        }
      });
    }
    //gather any exceptions
    RuntimeException t = null;
    Error e = null;
    for (Future<?> f : futures) {
      try {
        f.get();
      } catch (InterruptedException ex) {
      } catch (ExecutionException ex) {
        if (ex.getCause() instanceof RuntimeException) {
          t = (RuntimeException) ex.getCause();
        }
        if (ex.getCause() instanceof Error) {
          e = (Error) ex.getCause();
        }
      }
    }
    if (t != null) {
      throw t;
    }
    if (e != null) {
      throw e;
    }
  }

  public static void shutdown() {
    executor.shutdown();
  }
}
