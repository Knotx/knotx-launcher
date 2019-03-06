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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.function.Consumer;

public class VerifiableVerticle implements Verticle {

  private Consumer<JsonObject> assertions;
  private final boolean shouldFail;
  private Vertx vertx;

  VerifiableVerticle(Consumer<JsonObject> assertions, boolean shouldFail) {
    this.assertions = assertions;
    this.shouldFail = shouldFail;
  }

  VerifiableVerticle(Consumer<JsonObject> assertions) {
    this(assertions, false);
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public void init(Vertx vertx, Context context) {
    this.vertx = vertx;
    if (assertions != null) {
      assertions.accept(context.config());
    }
  }

  @Override
  public void start(Future<Void> startFuture) {
    if (shouldFail) {
      startFuture.fail("Start failed");
    } else {
      startFuture.complete();
    }
  }

  @Override
  public void stop(Future<Void> stopFuture) {
    stopFuture.complete();
  }
}
