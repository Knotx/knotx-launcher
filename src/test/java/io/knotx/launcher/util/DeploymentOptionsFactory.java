/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
