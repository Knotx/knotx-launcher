package io.knotx.launcher.property;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This class loads all {@link SystemPropertyProvider} implementations using the {@link
 * ServiceLoader} mechanism. The default implementation gets values from System properties and
 * expose them as default ones. It allows to customize your deployment with additional logic.
 */
public class SystemProperties {

  private final LinkedList<Properties> stores;

  public SystemProperties() {
    this.stores = new LinkedList<>();
    ServiceLoader<SystemPropertyProvider> allProviders = ServiceLoader
        .load(SystemPropertyProvider.class);
    allProviders.forEach(provider -> {
      if (provider.getClass() == DefaultSystemPropertyProvider.class) {
        stores.addFirst(provider.getProperties());
      } else {
        stores.addLast(provider.getProperties());
      }
    });
  }

  /**
   * Gets property based on <pre>key</pre>. It search for the key in all {@link
   * SystemPropertyProvider} implementations and responds with first non-null value. Please note
   * that the first (default) store expose properties from System.
   *
   * @param key - property key
   * @return property value
   */
  public Optional<String> getProperty(String key) {
    return stores.stream()
        .map(properties -> properties.getProperty(key))
        .filter(Objects::nonNull)
        .findFirst();
  }

}
