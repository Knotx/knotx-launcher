package io.knotx.launcher.property;

import java.util.Properties;

public class DefaultSystemPropertyProvider implements SystemPropertyProvider {

  @Override
  public Properties getProperties() {
    return System.getProperties();
  }

}
