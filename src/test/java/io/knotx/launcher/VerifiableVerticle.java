package io.knotx.launcher;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.function.Consumer;

public class VerifiableVerticle implements Verticle {

  private Consumer<JsonObject> assertions;
  private Vertx vertx;

  VerifiableVerticle(Consumer<JsonObject> assertions) {
    this.assertions = assertions;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public void init(Vertx vertx, Context context) {
    this.vertx = vertx;
    assertions.accept(context.config());
  }

  @Override
  public void start(Future<Void> startFuture) {
    startFuture.complete();
  }

  @Override
  public void stop(Future<Void> stopFuture) {
    stopFuture.complete();
  }
}
