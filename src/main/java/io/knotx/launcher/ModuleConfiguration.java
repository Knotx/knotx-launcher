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
package io.knotx.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

class ModuleConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleConfiguration.class);

  private static final String CONFIG_OVERRIDE = "config";
  private static final String MODULE_OPTIONS = "options";
  private static final String OPTIONAL_KEY = "optional";

  private final String moduleName;
  private DeploymentOptions deploymentOptions;
  private boolean optional;

  private ModuleConfiguration(String moduleName) {
    this.moduleName = moduleName;
  }

  static ModuleConfiguration fromJson(JsonObject json, String alias) {
    final ModuleConfiguration module = new ModuleConfiguration(alias);
    module.deploymentOptions = new DeploymentOptions();
    if (json.containsKey(CONFIG_OVERRIDE)) {
      if (json.getJsonObject(CONFIG_OVERRIDE).containsKey(alias)) {
        JsonObject moduleConfig = json.getJsonObject(CONFIG_OVERRIDE).getJsonObject(alias);
        if (moduleConfig.containsKey(MODULE_OPTIONS)) {
          module.deploymentOptions.fromJson(moduleConfig.getJsonObject(MODULE_OPTIONS));
          module.optional = moduleConfig.getJsonObject(MODULE_OPTIONS)
              .getBoolean(OPTIONAL_KEY, false);
        } else {
          LOGGER.warn(
              "Module '{}' has config, but missing 'options' object. "
                  + "Default configuration is to be used", alias);
        }
      } else {
        LOGGER.warn("Module '{}' if not configured in the config file. Used default configuration",
            alias);
      }
    }

    return module;
  }

  public String getModuleName() {
    return moduleName;
  }

  public DeploymentOptions getDeploymentOptions() {
    return deploymentOptions;
  }

  public boolean isOptional() {
    return optional;
  }

  @Override
  public String toString() {
    return "ModuleConfiguration{" +
        "moduleName='" + moduleName + '\'' +
        ", deploymentOptions=" + deploymentOptions.toJson().encode() +
        ", optional=" + optional +
        '}';
  }
}
