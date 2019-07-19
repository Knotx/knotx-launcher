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

public class ModuleDescriptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleDescriptor.class);

  private static final String MODULE_DEFAULT_PREFIX = "java:";

  private static final String CONFIG_OVERRIDE = "config";
  private static final String MODULE_OPTIONS = "options";
  private static final String REQUIRED_KEY = "required";

  private String alias;
  private String name;
  private String deploymentId;
  private DeploymentState state = DeploymentState.UNKNOWN;
  private DeploymentOptions deploymentOptions;
  private boolean required = true;

  private ModuleDescriptor(String alias, String name) {
    this.alias = alias;
    if (name.indexOf(':') != -1) {
      this.name = name;
    } else {
      this.name = MODULE_DEFAULT_PREFIX + name;
    }
  }

  ModuleDescriptor(ModuleDescriptor other) {
    this.alias = other.alias;
    this.name = other.name;
    this.deploymentId = other.deploymentId;
    this.state = other.state;
    this.deploymentOptions = other.deploymentOptions;
    this.required = other.required;
  }

  static ModuleDescriptor fromConfig(String alias, String name, JsonObject json) {
    ModuleDescriptor descriptor = new ModuleDescriptor(alias, name);
    parseJson(json, descriptor);
    return descriptor;
  }

  public String getAlias() {
    return alias;
  }

  public String getName() {
    return name;
  }

  public String getDeploymentId() {
    return deploymentId;
  }


  public ModuleDescriptor setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  public ModuleDescriptor setState(DeploymentState state) {
    this.state = state;
    return this;
  }

  public DeploymentState getState() {
    return state;
  }

  public DeploymentOptions getDeploymentOptions() {
    return deploymentOptions;
  }

  public boolean isRequired() {
    return required;
  }

  String toLogEntry() {
    return getState().getMessage()
        + " " + deploymentOptions.getInstances() + " instance(s)"
        + " of " + (required ? "required " : "optional ") + alias
        + " (" + name + ")"
        + (deploymentId != null ? " [" + deploymentId + "]" : "");
  }

  @Override
  public String toString() {
    return "ModuleDescriptor{" +
        "alias='" + alias + '\'' +
        ", name='" + name + '\'' +
        ", deploymentId='" + deploymentId + '\'' +
        ", state=" + state +
        ", deploymentOptions=" + deploymentOptions +
        ", required=" + required +
        '}';
  }

  private static void parseJson(JsonObject json, ModuleDescriptor descriptor) {
    descriptor.deploymentOptions = new DeploymentOptions();
    if (json.containsKey(CONFIG_OVERRIDE)) {
      if (json.getJsonObject(CONFIG_OVERRIDE).containsKey(descriptor.alias)) {
        JsonObject moduleConfig = json.getJsonObject(CONFIG_OVERRIDE)
            .getJsonObject(descriptor.alias);
        if (moduleConfig.containsKey(MODULE_OPTIONS)) {
          descriptor.deploymentOptions.fromJson(moduleConfig.getJsonObject(MODULE_OPTIONS));
          descriptor.required = moduleConfig.getJsonObject(MODULE_OPTIONS)
              .getBoolean(REQUIRED_KEY, true);
        } else {
          LOGGER.warn(
              "Module '{}' has config, but missing 'options' object. "
                  + "Default configuration is to be used", descriptor.alias);
        }
      } else {
        LOGGER.warn("Module '{}' if not configured in the config file. Used default configuration",
            descriptor.alias);
      }
    }
  }

  public enum DeploymentState {
    UNKNOWN("Unknown state"),
    SUCCESS("Deployed"),
    FAILED("Failed to deploy");

    private final String message;

    DeploymentState(String message) {
      this.message = message;
    }

    private String getMessage() {
      return message;
    }

  }
}
