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

import static io.knotx.launcher.util.DeploymentOptionsFactory.fromBootstrapTemplate;

import io.vertx.core.DeploymentOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class RequiredModulesOptionTest {

  public static final String BOOTSTRAP_TEMPLATE = "required-option/bootstrap.json";

  @Test
  @DisplayName("Expect instance fails when deploying a single failing module (with default required value)")
  void errorWhenRequiredModuleDeploymentFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "one-module-one-instance.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.allVerticlesFail());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.failNow(new RuntimeException("This deployment should fail")),
            throwable -> testContext.completeNow()
        );
  }

  @Test
  @DisplayName("Expect instance fails when deploying required module with 3 instances and one instance fails to start")
  void errorWhenAnyRequiredModuleDeploymentFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "one-module-3-instances.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.everySecondVerticleFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.failNow(new RuntimeException("This deployment should fail")),
            throwable -> testContext.completeNow()
        );
  }

  @Test
  @DisplayName("Expect instance starts when deploying failing module marked as not required")
  void startWhenNotRequiredModuleDeploymentFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "one-module-one-instance-optional.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.allVerticlesFail());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy 3 instances of failing module marked as not required and expect instance start successfully.")
  void successStartWhenModuleDeploymentFailsWithThreeInstances(VertxTestContext testContext,
      Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "one-module-3-instances-optional.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.everySecondVerticleFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Expect instance starts when deploying two optional failing modules and one required starting module")
  void startWhenTwoOptionalFailOneRequiredSucceed(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "two-modules-optional-fail-one-required-ok.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.everyWithFailPostfixInNameFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Expect instance starts when deploying three optional failing modules")
  void startWhenThreeOptionalFail(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "three-modules-optional.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.allVerticlesFail());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Expect instance fails when deploying two optional starting modules and one required failing module")
  void errorWhenTwoOptionalSucceedOneRequiredFails(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapTemplate(BOOTSTRAP_TEMPLATE,
        "two-modules-optional-ok-one-required-fails.conf");

    vertx.registerVerticleFactory(TestVerticlesFactory.everyWithFailPostfixInNameFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.failNow(new RuntimeException("This deployment should fail")),
            throwable -> testContext.completeNow()
        );
  }
}