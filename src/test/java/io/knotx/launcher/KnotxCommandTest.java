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

import static io.vertx.core.cli.CLI.create;
import static io.vertx.core.cli.CommandLine.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.impl.launcher.commands.BareCommand;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;

//@Execution(ExecutionMode.CONCURRENT)
public class KnotxCommandTest {

  @Test
  @DisplayName("Should read configuration from given path")
  public void should_read_configuration_from_file(){
    //given
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));
    underTest.setConfig("./conf/bootstrap.json");

    //when
    JsonObject config = underTest.getConfiguration();

    //then
    Assertions.assertNotNull(config);
  }

  @Test
  @DisplayName("Should read default configuration")
  void should_read_default_configuration(){
    //given
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));

    //when
    JsonObject config = underTest.getConfiguration();

    //then
    Assertions.assertNotNull(config);
  }

  @Test
  @DisplayName("Should return null configuration for not existing file")
  void should_return_null_for_not_existing_file(){
    //given
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));
    underTest.setConfig("./not_existing_file.json");

    //when
    JsonObject config = underTest.getConfiguration();

    //then
    Assertions.assertNull(config);
  }

  @Test
  @DisplayName("Should return default deployment options")
  void should_return_default_deployment_options(){
    //given
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));
    JsonObject config = new JsonObject();
    //when
    DeploymentOptions deploymentOptions = underTest.getDeploymentOptions(config);

    //then
    Assertions.assertEquals(false, deploymentOptions.isHa());
    Assertions.assertEquals(config, deploymentOptions.getConfig());
  }

  @Test
  @DisplayName("Should overwrite HA option")
  void should_overwriteHaOption(){
    //given
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));
    underTest.setHighAvailability(true);
    //when
    DeploymentOptions deploymentOptions = underTest.getDeploymentOptions(new JsonObject());

    //then
    assertTrue(deploymentOptions.isHa());
  }

  @Test
  @DisplayName("Should read Vertex worker pool size from system property")
  void should_read_properties_from_system() {
    //given
    System.setProperty(BareCommand.DEPLOYMENT_OPTIONS_PROP_PREFIX + "workerPoolSize", "10");
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));

    //when
    DeploymentOptions deploymentOptions = underTest.getDeploymentOptions(new JsonObject());

    //then
    assertEquals(10, deploymentOptions.getWorkerPoolSize());
  }

  @Test
  @DisplayName("One and only one Starter Verticle instance allowed")
  void should_throws_exception_when_more_then_one_instance_set() {
    //given
    System.setProperty(BareCommand.DEPLOYMENT_OPTIONS_PROP_PREFIX + "instances", "10");
    KnotxCommand underTest = new KnotxCommandFactory().create(create(create("run-knox")));

    //then
    assertThrows(IllegalStateException.class, () -> {

      //when
      underTest.getDeploymentOptions(new JsonObject());
    });
  }

}