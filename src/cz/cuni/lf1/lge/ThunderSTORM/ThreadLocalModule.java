package cz.cuni.lf1.lge.ThunderSTORM;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class ThreadLocalModule <S extends IModuleUI<T>,T extends IModule> extends ThreadLocal<T> {

  S moduleUI;

  public ThreadLocalModule(S moduleUI) {
    this.moduleUI = moduleUI;
  }

  @Override
  public T initialValue() {
    return moduleUI.getImplementation();
  }
}
