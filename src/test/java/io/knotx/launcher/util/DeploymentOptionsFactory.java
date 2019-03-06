package io.knotx.launcher.util;

import io.knotx.junit5.util.FileReader;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public final class DeploymentOptionsFactory {

  private DeploymentOptionsFactory() {
    //empty
  }

  public static DeploymentOptions fromBootstrapFile(String bootstrapPath) {
    String storesConfig = FileReader.readTextSafe(bootstrapPath);
    return new DeploymentOptions().setConfig(new JsonObject(storesConfig));
  }

  public static DeploymentOptions fromBootstrapTemplate(String bootstrapTemplatePath,
      String storeConfigPath) {
    String storesConfigTemplate = FileReader.readTextSafe(bootstrapTemplatePath);
    return new DeploymentOptions().setConfig(
        new JsonObject(storesConfigTemplate.replaceAll("PATH_TO_CONFIG_FILE", storeConfigPath)));
  }

}
