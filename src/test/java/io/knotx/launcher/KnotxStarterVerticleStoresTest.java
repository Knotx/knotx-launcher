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

import static io.knotx.launcher.util.DeploymentOptionsFactory.fromBootstrapFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.knotx.launcher.TestVerticlesFactory.VerificationContext;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import java.util.function.Consumer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class KnotxStarterVerticleStoresTest {

  private static final String CONFIG_TEST_OPTION_KEY = "testOption";

  @Test
  @DisplayName("Expect instance running, when starting with one optional and present store")
  void startWithOneOptionalPresentStore(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapFile(
        "multiple-stores/bootstrap/one-present-optional-store.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStart(
            VerificationContext.instance()
                // then
                .setAssertions(validateTestOptionValue("testValue"))
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
  @DisplayName("Expect instance running, when starting with one optional and missing store")
  void startWithOneOptionalMissingStore(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapFile(
        "multiple-stores/bootstrap/one-missing-optional-store.json");

    // when
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Expect instance running, when starting with one not-optional and present store")
  void startWithOneMandatoryPresentStore(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapFile(
        "multiple-stores/bootstrap/one-present-mandatory-store.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStart(
            VerificationContext.instance()
                // then
                .setAssertions(validateTestOptionValue("testValue"))
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
  @DisplayName("Expect instance failing, when starting with one non-optional and missing store")
  void startWithOneMandatoryMissingStore(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapFile(
        "multiple-stores/bootstrap/one-missing-mandatory-store.json");

    // when
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            s -> testContext.failNow(new RuntimeException("This deployment should fail")),
            throwable -> {
              assertTrue(throwable instanceof FileSystemException);
              assertTrue(throwable.getMessage().contains("file-that-does-not-exists.conf"));
              testContext.completeNow();
            }
        );
  }

  @Test
  @DisplayName("Expect configuration overridden by second store")
  void checkConfigurationOverriding(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapFile(
        "multiple-stores/bootstrap/two-stores-with-overrides.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStart(
            VerificationContext.instance()
                // then
                .setAssertions(jsonObject -> {
                  assertEquals("testValue-overridden", jsonObject.getString(CONFIG_TEST_OPTION_KEY));
                  assertEquals("knotx", jsonObject.getString("defaultOption"));
                  assertEquals("rocks", jsonObject.getString("extraOption"));
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
  @DisplayName("Expect all modules deployed from several stores")
  void checkAllModulesDeployed(VertxTestContext testContext, Vertx vertx) {
    // given
    DeploymentOptions options = fromBootstrapFile(
        "multiple-stores/bootstrap/three-stores-mandatory.json");

    vertx.registerVerticleFactory(TestVerticlesFactory
        .allVerticlesStart(
            VerificationContext.instance()
                // then
                .setAssertions(validateTestOptionValue("testValue"))
                .setTestContext(testContext))
    );

    // when
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  private Consumer<JsonObject> validateTestOptionValue(String expectedValue) {
    return jsonObject -> {
      assertNotNull(jsonObject.getString(CONFIG_TEST_OPTION_KEY));
      assertEquals(expectedValue, jsonObject.getString(CONFIG_TEST_OPTION_KEY));
    };
  }

}