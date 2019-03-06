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

public class ModuleDescriptor {

  public static final String MODULE_DEFAULT_PREFIX = "java:";

  public static final char MODULE_ALIAS_SEPARATOR = '=';

  private String alias;
  private String name;
  private String deploymentId;
  private DeploymentState state = DeploymentState.UNKNOWN;
  private int instances;

  private ModuleDescriptor() {
    //Default constructor
  }

  public ModuleDescriptor(ModuleDescriptor other) {
    this.alias = other.alias;
    this.name = other.name;
    this.deploymentId = other.deploymentId;
    this.state = other.state;
    this.instances = other.instances;
  }

  public static ModuleDescriptor parse(String line) {
    ModuleDescriptor descriptor = new ModuleDescriptor();
    int separatorIdx = line.indexOf(MODULE_ALIAS_SEPARATOR);

    if (separatorIdx == -1) {
      throw new IllegalArgumentException(
          "Module '" + line + "'should have form of <alias>" + MODULE_ALIAS_SEPARATOR
              + "<service>, e.g.: myAlias"
              + MODULE_ALIAS_SEPARATOR + "com.acme.VerticleClassName");
    }
    descriptor.alias = line.substring(0, separatorIdx);

    String name = line.substring(separatorIdx + 1);
    if (name.indexOf(':') != -1) {
      descriptor.name = name;
    } else {
      descriptor.name = MODULE_DEFAULT_PREFIX + name;
    }
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

  public String toDescriptorLine() {
    return alias + MODULE_ALIAS_SEPARATOR + name;
  }

  public int getInstances() {
    return instances;
  }

  public ModuleDescriptor setInstances(int instances) {
    this.instances = Integer.max(instances, 1);
    return this;
  }

  @Override
  public String toString() {
    return "ModuleDescriptor{" +
        "alias='" + alias + '\'' +
        ", name='" + name + '\'' +
        ", deploymentId='" + deploymentId + '\'' +
        ", state=" + state +
        ", instances=" + instances +
        '}';
  }

  public enum DeploymentState {
    UNKNOWN("Unknown state"),
    SUCCESS("Deployed"),
    FAILED_OPTIONAL("Failed to deploy optional"),
    FAILED_REQUIRED("Failed to deploy required");

    private final String message;

    DeploymentState(String message) {
      this.message = message;
    }

    @Override
    public String toString() {
      return message;
    }
  }
}
