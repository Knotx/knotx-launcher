package io.knotx.launcher;

import io.knotx.launcher.property.SystemPropertyProvider;
import java.util.Properties;

public class TestingSystemPropertiesProvider implements SystemPropertyProvider {

  @Override
  public Properties getProperties() {
    Properties properties = new Properties();
    properties.put("custom.system.property", "systemPropertyValue");
    return properties;
  }
}
