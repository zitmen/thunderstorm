package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class processes rendering tasks serially. It consists of a queue and a
 * thread that takes tasks out of the queue and processes them. The lifetime of
 * the rendering thread is tied to the lifetime of this object. Can perform a
 * user defined task after every [frequency] invocations.
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class RenderingQueue {

  BlockingQueue<RenderTask> queue = null;
  RenderingThread thread;
  Runnable repaintTask;

  /**
   * Example: new RenderingQueue(method, new Runnable(){public void
   * run(){image.repaint();}},10) will call image.repaint() every 10 tasks
   * executed (either renderLater,invokeLater or repaintLater)
   *
   * @param method rendering method
   * @param repaintTask Runnable, which will be called after executing every
   * repaintFrequency tasks. Or null if no periodic action should be run.
   * @param repaintFrequency specifies how often to call repaintTask.run().
   */
  public RenderingQueue(IncrementalRenderingMethod method, Runnable repaintTask, int repaintFrequency) {
    if (method != null && repaintFrequency > 0) {
      queue = new LinkedBlockingQueue<RenderTask>();
      this.repaintTask = repaintTask;
      thread = new RenderingThread(this, repaintTask, repaintFrequency, queue, method);
      thread.start();
    }
  }

  public void renderLater(double[] x, double[] y, double[] z, double[] dx) {
    if (queue != null) {
      queue.add(new VariableDXTask(x, y, z, dx));
    }
  }

  public void repaintLater() {
    if (queue != null) {
      queue.add(new InvokeTask(repaintTask));
    }
  }

  public void invokeLater(Runnable r) {
    if (queue != null) {
      queue.add(new InvokeTask(r));
    }
  }
}

class RenderingThread extends Thread {

  WeakReference<Object> tiedToObject;
  Runnable repaintTask;
  int repaintFrequency;
  int invocations = 0;
  IncrementalRenderingMethod renderingMethod;
  BlockingQueue<RenderTask> queue;

  RenderingThread(Object tiedToObject, Runnable repaintTask, int repaintFrequency, BlockingQueue<RenderTask> queue, IncrementalRenderingMethod renderingMethod) {
    if (tiedToObject == null) {
      throw new IllegalArgumentException("The thread must be tied to some object. Reference was null.");
    }
    this.tiedToObject = new WeakReference<Object>(tiedToObject);
    setDaemon(true);

    this.repaintTask = repaintTask;
    this.repaintFrequency = repaintFrequency;
    this.queue = queue;
    this.renderingMethod = renderingMethod;
    this.setName("ThunderSTORM repaint thread");
  }

  @Override
  public void run() {
    while (tiedToObject.get() != null || !queue.isEmpty()) {
      try {
        RenderTask task = queue.poll(5, TimeUnit.SECONDS);
        if (task != null) {
          task.doTask(renderingMethod);
          invocations++;
          if (invocations % repaintFrequency == 0 && repaintTask != null) {
            repaintTask.run();
          }
        }
      } catch (InterruptedException ex) {
      }
    }
  }
}

interface RenderTask {

  public void doTask(IncrementalRenderingMethod method);
}

class VariableDXTask implements RenderTask {

  double[] x, y, z, dx;

  public VariableDXTask(double[] x, double[] y, double[] z, double[] dx) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.dx = dx;
  }

  @Override
  public void doTask(IncrementalRenderingMethod method) {
    method.addToImage(x, y, z, dx);
  }
}

class InvokeTask implements RenderTask {

  Runnable task;

  public InvokeTask(Runnable task) {
    this.task = task;
  }

  @Override
  public void doTask(IncrementalRenderingMethod method) {
    if (task != null) {
      task.run();
    }
  }
}