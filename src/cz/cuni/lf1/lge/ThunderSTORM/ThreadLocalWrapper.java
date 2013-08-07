package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import java.util.List;
import javax.swing.JPanel;

public class ThreadLocalWrapper {

  public static List<IFilterUI> wrapFilters(List<IFilterUI> originalList) {
    for (int i = 0; i < originalList.size(); i++) {
      originalList.set(i, wrap(originalList.get(i)));
    }
    return originalList;
  }

  public static List<IDetectorUI> wrapDetectors(List<IDetectorUI> originalList) {
    for (int i = 0; i < originalList.size(); i++) {
      originalList.set(i, wrap(originalList.get(i)));
    }
    return originalList;
  }

  public static List<IEstimatorUI> wrapEstimators(List<IEstimatorUI> originalList) {
    for (int i = 0; i < originalList.size(); i++) {
      originalList.set(i, wrap(originalList.get(i)));
    }
    return originalList;
  }
  
  public static List<IRendererUI> wrapRenderers(List<IRendererUI> originalList) {
    for (int i = 0; i < originalList.size(); i++) {
      originalList.set(i, wrap(originalList.get(i)));
    }
    return originalList;
  }

  public static IEstimatorUI wrap(IEstimatorUI est) {
    return new ThreadLocalEstimatorUI(est);
  }

  public static IDetectorUI wrap(IDetectorUI det) {
    return new ThreadLocalDetectorUI(det);
  }

  public static IFilterUI wrap(IFilterUI filter) {
    return new ThreadLocalFilterUI(filter);
  }
  
  public static IRendererUI wrap(IRendererUI renderer) {
    return new ThreadLocalRendererUI(renderer);
  }
}

class ThreadLocalModule<S extends IModuleUI<T>, T extends IModule> extends ThreadLocal<T> {

  S moduleUI;

  public ThreadLocalModule(S moduleUI) {
    this.moduleUI = moduleUI;
  }

  @Override
  public T initialValue() {
    return moduleUI.getImplementation();
  }
}

class ThreadLocalFilterUI implements IFilterUI {

  IFilterUI filter;
  ThreadLocalModule<IFilterUI, IFilter> threadLocalImplementation;

  public ThreadLocalFilterUI(IFilterUI filter) {
    this.filter = filter;
    threadLocalImplementation = new ThreadLocalModule<IFilterUI, IFilter>(filter);
  }

  @Override
  public String getName() {
    return filter.getName();
  }

  @Override
  public JPanel getOptionsPanel() {
    return filter.getOptionsPanel();
  }

  @Override
  public void readParameters() {
    filter.readParameters();
  }

  @Override
  public void recordOptions() {
    filter.recordOptions();
  }

  @Override
  public void readMacroOptions(String options) {
    filter.readMacroOptions(options);
  }

  @Override
  public IFilter getImplementation() {
    return threadLocalImplementation.get();
  }
}

class ThreadLocalDetectorUI implements IDetectorUI {

  IDetectorUI detector;
  ThreadLocalModule<IDetectorUI, IDetector> threadLocalImplementation;

  public ThreadLocalDetectorUI(IDetectorUI detector) {
    this.detector = detector;
    threadLocalImplementation = new ThreadLocalModule<IDetectorUI, IDetector>(detector);
  }

  @Override
  public String getName() {
    return detector.getName();
  }

  @Override
  public JPanel getOptionsPanel() {
    return detector.getOptionsPanel();
  }

  @Override
  public void readParameters() {
    detector.readParameters();
  }

  @Override
  public void recordOptions() {
    detector.recordOptions();
  }

  @Override
  public void readMacroOptions(String options) {
    detector.readMacroOptions(options);
  }

  @Override
  public IDetector getImplementation() {
    return threadLocalImplementation.get();
  }
}

class ThreadLocalEstimatorUI implements IEstimatorUI {

  IEstimatorUI estimator;
  ThreadLocalModule<IEstimatorUI, IEstimator> threadLocalImplementation;

  public ThreadLocalEstimatorUI(IEstimatorUI estimator) {
    this.estimator = estimator;
    threadLocalImplementation = new ThreadLocalModule<IEstimatorUI, IEstimator>(estimator);
  }

  @Override
  public String getName() {
    return estimator.getName();
  }

  @Override
  public JPanel getOptionsPanel() {
    return estimator.getOptionsPanel();
  }

  @Override
  public void readParameters() {
    estimator.readParameters();
  }

  @Override
  public void recordOptions() {
    estimator.recordOptions();
  }

  @Override
  public void readMacroOptions(String options) {
    estimator.readMacroOptions(options);
  }

  @Override
  public IEstimator getImplementation() {
    return threadLocalImplementation.get();
  }

  public void discardCachedImplementations() {
    threadLocalImplementation = new ThreadLocalModule<IEstimatorUI, IEstimator>(estimator);
  }
}

class ThreadLocalRendererUI implements IRendererUI {

  IRendererUI renderer;
  ThreadLocalModule<IRendererUI, IncrementalRenderingMethod> threadLocalImplementation;

  public ThreadLocalRendererUI(IRendererUI renderer) {
    this.renderer = renderer;
    threadLocalImplementation = new ThreadLocalModule<IRendererUI, IncrementalRenderingMethod>(renderer);
  }

  @Override
  public String getName() {
    return renderer.getName();
  }

  @Override
  public JPanel getOptionsPanel() {
    return renderer.getOptionsPanel();
  }

  @Override
  public void readParameters() {
    renderer.readParameters();
  }

  @Override
  public void recordOptions() {
    renderer.recordOptions();
  }

  @Override
  public void readMacroOptions(String options) {
    renderer.readMacroOptions(options);
  }

  @Override
  public IncrementalRenderingMethod getImplementation() {
    return threadLocalImplementation.get();
  }

  @Override
  public void setSize(int sizeX, int sizeY) {
    renderer.setSize(sizeX, sizeY);
  }

  @Override
  public int getRepaintFrequency() {
    return renderer.getRepaintFrequency();
  }
}