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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;

//@ExtendWith(VertxExtension.class)
public class KnotxCommandTest {

  @Test
  @DisplayName("Should read configuration from given path")
  public void should_read_configuration(){
    //given
    KnotxCommand knotxCommand = new KnotxCommandFactory().create(create(create("run-knox")));
    knotxCommand.setConfig("./conf/bootstrap.json");

    //when
    JsonObject config = knotxCommand.getConfiguration();

    //then
    Assertions.assertNull(config);
  }

  @Test
  @DisplayName("Should read configuration from given path")
  void should_read_default_configuration(){
    //given
    KnotxCommand knotxCommand = new KnotxCommandFactory().create(create(create("run-knox")));

    //when
    JsonObject config = knotxCommand.getConfiguration();

    //then
    Assertions.assertNotNull(config);
  }

  //@Test
  //@DisplayName("Should deploy Knot.x Starter Verticle")
  private void should_deploy_knotx_vertivle(VertxTestContext testContext, Vertx vertx) throws Throwable {
    //given
    CLI cli= create("run-knox");
    CommandLine commandLine = create(cli);
    KnotxCommand knotxCommand = new KnotxCommandFactory().create(commandLine);
    knotxCommand.setCluster(false);
    knotxCommand.setConfig("./conf/bootstrap.json");
    knotxCommand.setVertex(vertx.getDelegate());

    //when
    JsonObject config = knotxCommand.getConfiguration();
    knotxCommand.deploy(config);

    //then
    testContext.verify(() -> {
      if (vertx.deploymentIDs().contains(knotxCommand.getStarterDeploymentId())) {
        testContext.completeNow();
      }
    });

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));

    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}