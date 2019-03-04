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
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class KnotxStarterVerticleTest {

  @Test
  @DisplayName("Example with empty modules starts successfully.")
  void startWithNoModules(VertxTestContext testContext, Vertx vertx) {
    // given
    String storesConfig = FileReader.readTextSafe("bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    // when
    vertx.rxDeployVerticle("io.knotx.launcher.KnotxStarterVerticle", options)
        .subscribe(
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

    // when
    vertx.registerVerticleFactory(verifiableVerticleFactory(jsonObject -> {
      assertNotNull(jsonObject.getString("myValueKey"));
      assertEquals("myValue", jsonObject.getString("myValueKey"));
    }));

    vertx.rxDeployVerticle("io.knotx.launcher.KnotxStarterVerticle", options)
        .subscribe(
            success -> {
              testContext.completeNow();
            },
            testContext::failNow
        );
  }

  @Test
  @DisplayName("Deploy a module with a property defined in system properties.")
  void startModuleWithSystemPropertyValue(VertxTestContext testContext, Vertx vertx) {
    // given
    String storesConfig = FileReader.readTextSafe("system/bootstrap.json");
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject(storesConfig));

    // when
    vertx.registerVerticleFactory(verifiableVerticleFactory(jsonObject -> {
      assertNotNull(jsonObject.getString("myValueKey"));
      assertEquals("myValue", jsonObject.getString("systemPropertyValue"));
    }));

    vertx.rxDeployVerticle("io.knotx.launcher.KnotxStarterVerticle", options)
        .subscribe(
            success -> {
              testContext.completeNow();
            },
            testContext::failNow
        );
  }

  private VerticleFactory verifiableVerticleFactory(Consumer<JsonObject> assertions) {
    return new VerticleFactory() {
      @Override
      public String prefix() {
        return "test";
      }

      @Override
      public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
        return new VerifiableVerticle(assertions);
      }
    };
  }

}