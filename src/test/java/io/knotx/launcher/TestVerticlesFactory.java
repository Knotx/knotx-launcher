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

import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TestVerticlesFactory implements VerticleFactory {

  private static final BiFunction<Integer, String, Boolean> EVERY_VERTICLE_STARTS = (i, name) -> false;
  private static final BiFunction<Integer, String, Boolean> EVERY_VERTICLE_FAILS = (i, name) -> true;
  private static final BiFunction<Integer, String, Boolean> EVERY_SECOND_VERTICLE_FAILS = (i, name) -> i % 2 == 0;
  private static final BiFunction<Integer, String, Boolean> FAIL_BY_NAME = (i, name) -> name.endsWith("fail");

  private final AtomicInteger count = new AtomicInteger();
  private final BiFunction<Integer, String, Boolean> shouldVerticleFail;
  private final VerificationContext verificationContext;

  private TestVerticlesFactory(VerificationContext verificationContext,
      BiFunction<Integer, String, Boolean> shouldVerticleFail) {
    this.verificationContext = verificationContext;
    this.shouldVerticleFail = shouldVerticleFail;
  }

  static TestVerticlesFactory allVerticlesStart(VerificationContext verificationContext) {
    return new TestVerticlesFactory(verificationContext, EVERY_VERTICLE_STARTS);
  }

  static TestVerticlesFactory allVerticlesFail() {
    return new TestVerticlesFactory(VerificationContext.instance(), EVERY_VERTICLE_FAILS);
  }

  static TestVerticlesFactory everySecondVerticleFails() {
    return new TestVerticlesFactory(VerificationContext.instance(), EVERY_SECOND_VERTICLE_FAILS);
  }

  static TestVerticlesFactory everyWithFailPostfixInNameFails() {
    return new TestVerticlesFactory(VerificationContext.instance(), FAIL_BY_NAME);
  }

  @Override
  public String prefix() {
    return "test";
  }

  int getDeployedVerticlesCount() {
    return count.get();
  }

  @Override
  public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
    Consumer<JsonObject> checkedAssertions = null;
    if (verificationContext.shouldVerify()) {
      checkedAssertions =
          jsonObject -> verificationContext.getTestContext()
              .verify(() -> verificationContext.getAssertions().accept(jsonObject));
    }
    return new VerifiableVerticle(checkedAssertions,
        shouldVerticleFail.apply(count.getAndIncrement(), verticleName));
  }

  static class VerificationContext {

    private VertxTestContext testContext;
    private Consumer<JsonObject> assertions;

    static VerificationContext instance() {
      return new VerificationContext();
    }

    VertxTestContext getTestContext() {
      return testContext;
    }

    VerificationContext setTestContext(VertxTestContext testContext) {
      this.testContext = testContext;
      return this;
    }

    Consumer<JsonObject> getAssertions() {
      return assertions;
    }

    VerificationContext setAssertions(Consumer<JsonObject> assertions) {
      this.assertions = assertions;
      return this;
    }

    boolean shouldVerify() {
      return testContext != null && assertions != null;
    }
  }
}