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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.knotx.junit5.util.FileReader;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class KnotxStarterVerticleMultipleStoresTest {

  private static final String MY_VALUE_KEY = "myValueKey";

  @Test
  @DisplayName("Example with empty modules starts successfully.")
  void startWithNoModules(VertxTestContext testContext, Vertx vertx) {
    // given
    String storesConfig = FileReader.readTextSafe("bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

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
    String storesConfig = FileReader.readTextSafe("simple/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    vertx.registerVerticleFactory(verifiableVerticleFactory(jsonObject -> {
      // then
      assertNotNull(jsonObject.getString(MY_VALUE_KEY));
      assertEquals("myValue", jsonObject.getString(MY_VALUE_KEY));
    }, testContext));

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
    String storesConfig = FileReader.readTextSafe("system/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    vertx.registerVerticleFactory(verifiableVerticleFactory(jsonObject -> {
      // then
      assertNotNull(jsonObject.getString(MY_VALUE_KEY));
      assertEquals("systemPropertyValue", jsonObject.getString(MY_VALUE_KEY));
    }, testContext));

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
    String storesConfig = FileReader.readTextSafe("complex/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    vertx.registerVerticleFactory(verifiableVerticleFactory(jsonObject -> {
      // then
      assertNotNull(jsonObject.getString(MY_VALUE_KEY));
      assertEquals("overloadedValue", jsonObject.getString(MY_VALUE_KEY));
    }, testContext));

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
    String storesConfig = FileReader.readTextSafe("failing/default/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    vertx.registerVerticleFactory(FailingVerticlesFactory.everyVerticleFails());

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
    String storesConfig = FileReader.readTextSafe("failing/multiple/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    vertx.registerVerticleFactory(FailingVerticlesFactory.everySecondVerticleFails());

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
    String storesConfig = FileReader.readTextSafe("failing/optional/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));


    vertx.registerVerticleFactory(FailingVerticlesFactory.everyVerticleFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy multiple instances of failing module marked as not required and expect instance start successfully.")
  void successStartWhenModuleDeploymentFailsWithThreeInstances(VertxTestContext testContext, Vertx vertx) {
    // given
    String storesConfig = FileReader.readTextSafe("failing/multiple-optional/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));


    vertx.registerVerticleFactory(FailingVerticlesFactory.everySecondVerticleFails());

    // then
    vertx.rxDeployVerticle(KnotxStarterVerticle.class.getName(), options)
        .subscribe(
            success -> testContext.completeNow(),
            testContext::failNow
        );
  }

  private VerticleFactory verifiableVerticleFactory(Consumer<JsonObject> assertions,
      VertxTestContext testContext) {
    return new VerticleFactory() {
      @Override
      public String prefix() {
        return "test";
      }

      @Override
      public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
        Consumer<JsonObject> checkedAssertions =
            jsonObject -> testContext.verify(() -> assertions.accept(jsonObject));

        return new VerifiableVerticle(checkedAssertions);
      }
    };
  }

  static class FailingVerticlesFactory implements VerticleFactory {
    static final Function<Integer, Boolean> EVERY_VERTICLE_FAILS = i -> true;
    static final Function<Integer, Boolean> EVERY_SECOND_VERTICLE_FAILS = i -> i % 2 == 0;

    private final AtomicInteger count = new AtomicInteger();
    private final Function<Integer, Boolean> shouldVerticleFail;

    FailingVerticlesFactory(
        Function<Integer, Boolean> shouldVerticleFail) {
      this.shouldVerticleFail = shouldVerticleFail;
    }

    static FailingVerticlesFactory everyVerticleFails() {
      return new FailingVerticlesFactory(EVERY_VERTICLE_FAILS);
    }

    static FailingVerticlesFactory everySecondVerticleFails() {
      return new FailingVerticlesFactory(EVERY_SECOND_VERTICLE_FAILS);
    }

    @Override
    public String prefix() {
      return "test";
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
      return new VerifiableVerticle(null, shouldVerticleFail.apply(count.getAndIncrement()));
    }
  }

}