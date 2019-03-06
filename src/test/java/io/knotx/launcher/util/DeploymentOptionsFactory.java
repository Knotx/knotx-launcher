package io.knotx.launcher.util;

import io.knotx.junit5.util.FileReader;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public final class DeploymentOptionsFactory {

  private DeploymentOptionsFactory() {
    //empty
  }

  public static DeploymentOptions deploymentOptionsFromBootstrap(String bootstrapPath) {
    String storesConfig = FileReader.readTextSafe(bootstrapPath);
    return new DeploymentOptions().setConfig(new JsonObject(storesConfig));
  }

}
