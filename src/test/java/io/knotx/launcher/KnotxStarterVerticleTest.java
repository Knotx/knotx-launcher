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

import static io.knotx.launcher.util.DeploymentOptionsFactory.deploymentOptionsFromBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.knotx.launcher.TestVerticlesFactory.VerificationContext;
import io.vertx.core.DeploymentOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class KnotxStarterVerticleTest {

  private static final String MY_VALUE_KEY = "myValueKey";

  @Test
  @DisplayName("Example with empty modules starts successfully.")
  void startWithNoModules(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("bootstrap.json");

    // when
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            // then
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy a module with a property defined in the application.conf file.")
  void startModuleWithConfiguredOption(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("simple/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStarts(
            VerificationContext.instance()
                .setAssertions(jsonObject -> {
                  // then
                  assertNotNull(jsonObject.getString(MY_VALUE_KEY));
                  assertEquals("myValue", jsonObject.getString(MY_VALUE_KEY));
                })
                .setTestContext(testContext))
    );

    // when
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy a module with a property defined in system properties.")
  void startModuleWithSystemPropertyValue(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("system/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStarts(
            VerificationContext.instance()
                .setAssertions(jsonObject -> {
                  // then
                  assertNotNull(jsonObject.getString(MY_VALUE_KEY));
                  assertEquals("systemPropertyValue", jsonObject.getString(MY_VALUE_KEY));
                })
                .setTestContext(testContext))
    );

    // when
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy a module with a configuration included from a separate file.")
  void startModuleWithIncludes(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("complex/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStarts(
            VerificationContext.instance()
                .setAssertions(jsonObject -> {
                  // then
                  assertNotNull(jsonObject.getString(MY_VALUE_KEY));
                  assertEquals("overloadedValue", jsonObject.getString(MY_VALUE_KEY));
                })
                .setTestContext(testContext))
    );

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy single failing module and expect instance start fails by default.")
  void failStartWhenModuleDeploymentFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("failing/default/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory.allVerticlesFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.failNow(new RuntimeException("This test should fail")),
            throwable -> testContext.completeNow()
        );
  }

  @Test
  @DisplayName("Deploy multiple modules when some are failing and expect instance start fails by default.")
  void failStartWhenAnyModuleDeploymentFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("failing/multiple/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory.everySecondVerticleFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.failNow(new RuntimeException("This test should fail")),
            throwable -> testContext.completeNow()
        );
  }

  @Test
  @DisplayName("Deploy failing module marked as not required and expect instance start successfully.")
  void successStartWhenModuleDeploymentFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap("failing/optional/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory.allVerticlesFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy multiple instances of failing module marked as not required and expect instance start successfully.")
  void successStartWhenModuleDeploymentFailsWithThreeInstances(VertxTestContext testContext,
      Vertx vertx) {
    // given
    DeploymentOptions options = deploymentOptionsFromBootstrap(
        "failing/multiple-optional/bootstrap.json");

    vertx.registerVerticleFactory(TestVerticlesFactory.everySecondVerticleFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }
}