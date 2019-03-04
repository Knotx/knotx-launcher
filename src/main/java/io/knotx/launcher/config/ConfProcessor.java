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
package io.knotx.launcher.config;

import static io.vertx.config.impl.spi.PropertiesConfigProcessor.closeQuietly;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigIncludeContext;
import com.typesafe.config.ConfigIncluder;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigResolver;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import io.knotx.launcher.property.SystemProperties;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * A processor using Typesafe Config to read Hocon files. It also support JSON and Properties. More
 * details on Hocon and the used library on the
 * <a href="https://github.com/typesafehub/config">Hocon documentation page</a>.
 *
 * This is a Vertx-config HOCON processor slightly modified to use custom Hocon file includer. It
 * was required to fix the issue with hocon includer that works best if you specify full path, and
 * we wanted to have ability to specify path relative to the main configuration file.
 *
 * Additionally, a latest versionof typesafe config is used to get 'required' directive for
 * includes.
 */
public class ConfProcessor implements ConfigProcessor {

  private SystemProperties properties = new SystemProperties();

  /**
   * This is the store format that is defined in the stores configuration file (by default
   * <pre>bootstrap.conf</pre>), see <pre>configRetrieverOptions.stores[].format</pre>.
   *
   * @return supported store format
   */
  @Override
  public String name() {
    return "conf";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input,
      Handler<AsyncResult<JsonObject>> handler) {
    // Use executeBlocking even if the bytes are in memory
    // Indeed, HOCON resolution can read others files (includes).
    vertx.executeBlocking(
        future -> {
          Reader reader = new StringReader(input.toString("UTF-8"));
          try {
            Config conf = ConfigFactory.parseReader(reader,
                ConfigParseOptions.defaults().appendIncluder(new KnotxConfIncluder(configuration)));

            conf = conf
                .resolve(ConfigResolveOptions.defaults().appendResolver(new SysPropResolver()));

            String output = conf.root().render(ConfigRenderOptions.concise()
                .setJson(true).setComments(false).setFormatted(false));
            JsonObject json = new JsonObject(output);
            future.complete(json);
          } catch (Exception e) {
            future.fail(e);
          } finally {
            closeQuietly(reader);
          }
        },
        handler
    );
  }

  private class SysPropResolver implements ConfigResolver {

    @Override
    public ConfigValue lookup(String path) {
      Optional<String> property = properties.getProperty(path);
      if (property.isPresent() && StringUtils.isNotBlank(property.get())) {
        String value = property.get();
        if (NumberUtils.isCreatable(value)) {
          return ConfigValueFactory.fromAnyRef(NumberUtils.toInt(value));
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(value) || Boolean.FALSE.toString()
            .equalsIgnoreCase(value)) {
          return ConfigValueFactory.fromAnyRef(BooleanUtils.toBoolean(value));
        } else {
          return ConfigValueFactory.fromAnyRef(value);
        }
      }

      return null;
    }

    @Override
    public ConfigResolver withFallback(ConfigResolver fallback) {
      return fallback;
    }
  }

  /**
   * An includer implementation to help searching for includes.
   * It relies on the 'path' parameter provided by the ConfigurationProvider. The 'path'
   * contains a path to the configuration file currently loaded. The folder of this file will
   * be treated as the root folder for resolving includes on '.conf' file level.
   * E.g. if the loaded config file has 'path'=config/application.conf
   * then the root folder for searching includes will be 'config'.
   * So, any inclue such as 'include "my-file.conf"' or 'include "folder/my-file.conf"'
   * will search for a file in 'config/my-file.conf' and 'config/folder/my-file.conf' respectively.
   */
  private class KnotxConfIncluder implements ConfigIncluder {

    private String configSearchFolder;

    KnotxConfIncluder(JsonObject configuration) {
      configSearchFolder = Optional.ofNullable(configuration.getString("path"))
          .map(path ->
              path.contains("/") ? path.substring(0, path.lastIndexOf("/")) : StringUtils.EMPTY)
          .orElse(properties.getProperty("knotx.home") + "/conf");
    }

    @Override
    public ConfigIncluder withFallback(ConfigIncluder fallback) {
      return this;
    }

    @Override
    public ConfigObject include(ConfigIncludeContext context, String what) {
      final File file;
      if (StringUtils.isBlank(configSearchFolder)) {
        file = new File(what);
      } else {
        file = new File(configSearchFolder, what);
      }
      return ConfigFactory.parseFile(file, context.parseOptions()).root();
    }
  }
}
