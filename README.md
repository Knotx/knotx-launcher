# Knot.x Launcher

Knot.x Launcher provides a way to configure and run **base** Knot.x instance. It consists of:
- the `run-knotx` starting command that runs a Vert.x instance and deploys all configured modules 
- logger configuration files
- configuration files

It is used in the [Knot.x Stack](https://github.com/Knotx/knotx-stack) to deliver fully functional 
bootstrap project for Knot.x-based projects.

Launcher has two main classes are:
- `io.knotx.launcher.KnotxCommand` - allows to run Knot.x instance with `run-knotx` command
- `io.knotx.launcher.KnotxStarterVerticle` - deploys all Verticles defined in configuration files

## How to run

Knot.x Launcher can be used as a JAR dependency in more complex distributions (like the
[Knot.x Stack](https://github.com/Knotx/knotx-stack)) or started as a standalone Vert.x instance 
from the ZIP distribution.

To build the Launcher distribution:
```
$> ./gradlew
```

To start Knot.x instance:
```
$> cd build/distributions
$> unzip knotx-launcher-X.Y.Z.zip -d launcher
$> cd launcher
$> ./bin/start run-knotx
...
13:56:15.890 [vert.x-eventloop-thread-0] INFO io.knotx.launcher.KnotxStarterVerticle - Knot.x STARTED
13:56:15.894 [vert.x-eventloop-thread-1] INFO io.vertx.core.impl.launcher.commands.VertxIsolatedDeployer - Succeeded in deploying verticle
```

The `run-knotx` command starts Knot.x (Vert.x) instance and deploys all configured modules. This 
command is registered in the `io.vertx.core.Launcher` class that is the main class of Vert.x 
executable jar. Additionally it uses concepts of [configuration stores](https://vertx.io/docs/vertx-config/java/)
to load configuration files from different locations and with different formats.

It accepts options:
- `config` - allows to define the `bootstrap.json` configuration file path, by default it looks for the `bootstrap.json` file in the classpath
- `ha` - specify that the Knot.x is deployed with High Availability (HA) enabled, check Vert.x 
[Automatic failover](https://vertx.io/docs/vertx-core/java/#_automatic_failover) section
- `hagroup` - HA group that denotes a logical group of nodes in the cluster, check Vert.x
[HA Groups](https://vertx.io/docs/vertx-core/java/#_ha_groups) section
- `quora` - it is the minimum number of votes that a distributed transaction has to obtain in order to 
be allowed to perform an operation in a distributed system
- `cluster` - enables the clustering

Knot.x provides the [example project](https://github.com/Knotx/knotx-example-project) that contains Docker Compose 
[configuration file](https://github.com/Knotx/knotx-example-project/blob/master/acme-cluster/docker-compose.yml)
to demonstrate cluster and HA capabilities.

Please note that if you want to start Launcher with `cluster` option you need to provide a required
cluster manager dependency and configuration files in the classpath.

## How to configure

The Knot.x configuration is basically split into two configuration files, such as
- bootstrap.json - a starter configuration what defines the application configuration format, 
location of application configurations (e.g. file, directory), as well as the ability to define 
whether the config should be scanned periodically
- application.conf - a main configuration file for the Knot.x based application. Knot.x 
promotes a [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) format as it provides 
useful mechanism, such as includes, variable substitutions, etc.

The configuration is resolved in the following steps:
- If not specified in command line (`-conf /path/to/bootstrap.json`), Knot.x looks for the 
`bootstrap.json` file in the classpath
- The definitions of [config stores](https://vertx.io/docs/vertx-config/java/) are read from the
`bootstrap.json` file and configured configuration files locations are being loaded and validated
- E.g. if `application.conf` was specified, the Knot.x loads it from the defined location, parses it, 
resolve any includes and starts all the Verticles defined in there.
- In case of any missing files Knot.x stops with the proper error message

### Configuration stores
The `bootstrap.json` file is structured around:
- a **Configuration Retriever** based on [Vert.x Config module](https://vertx.io/docs/vertx-config/java/) 
that configures a set of configuration stores and ability to scan the changes
- a **Configuration store** that defines a location from where the configuration data is read and a syntax

The structure of the file is as follows
```json
{
  "configRetrieverOptions": {
    "scanPeriod": 5000,
    "stores": [
      {
        "type": "file",
        "format": "conf",
        "config": {
          "path": "conf/application.conf"
        },
        "optional": false
      }
    ]
  }
}
```
- `scanPeriod` in milliseconds. If property is specified, the Knot.x scans the defined configuration stores and redeploys the Knot.x application on changes.
- `stores` it's an array of configuration stores. Each store requires two properties:
  - `type` a declared data store, such as File(**file**), JSON(**json**), Environment Variables(**env**), System Properties(**sys**), HTTP endpoint(**http**), Event Bus (**event-bus**), Directory(**dir**), Git (**git**), Kubernetes Config Map(**configmap**), Redis(**redis**), Zookeeper (**zookeeper**), Consul (**consul**), Spring Config (**spring-config-server**), Vault (**vault**)
  - `format` a format of the configuration file, such as JSON(**json**), HOCON(**conf**) and YAML(**yaml**)
  
In addition to the the out of the box config stores and formats it's easy to provide your own [custom 
implementation](https://github.com/Knotx/knotx-launcher/blob/master/src/main/java/io/knotx/launcher/config/ConfProcessor.java) 
thanks to the Vert.x Config SPI.
  
See the [Vert.x Config](https://vertx.io/docs/vertx-config/java/) for details how to use and configure any type of the store.

### Modules configuration
The `application.conf` configuration file used in Knot.x distribution supports the 
[HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) format. In short, the HOCON is 
the human optimized JSON that keeps the semantics (tree structure; set of types; encoding/escaping) 
from JSON, but make it more convenient as a human-editable config file format. Notable differences 
from JSON are comments, variables and includes.

The structure of the file is composed on the following sections:
- `modules` - an array of Verticles to start
- `config` - actual configuration for the given Verticle.

```hocon
########### Modules to start ###########
modules = [
  "myserver=io.knotx.server.KnotxServerVerticle"
  # Other modules to start
]

########### Modules configurations ###########
config.myserver {
  options.config {
    include required("includes/server.conf")
  }
}

## More configs below

```

The `config` section can be defined in the form that works best for you, e.g.
It can be just raw JSON, or HOCONized version of it as follows:
```hocon
config {
   myserver {
      options {
         config {
            # verticle configuration
         }
      }
   }
}
```
Or define it in the form of path in JSON as below
```hocon
config.myserver.options.config {
    # verticle configuration
}
config.myserver.options.instances=2
```

Consult [HOCON specification](https://github.com/typesafehub/config/blob/master/HOCON.md) to explore all possibilities.

Knot.x Stack defines its own [`application.conf`](https://github.com/Knotx/knotx-stack/blob/master/knotx-stack-manager/src/main/packaging/conf/application.conf)
that uses all configuration possibilities (includes, substitution etc).

### System properties
As mentioned above, you can reference any configuration object or property using `${var}` syntax. Thanks to the HOCON capabilities
you can also use the same notation to get the value from JVM system properties, e.g.:
```
java -Dmy.property=1234
```
You can inject to your configuration in a such a way:
```
someField = ${my.property}
```
Additionally, if the value from system property is optional you can use `${?var}` syntax to say that inject that value only if it's available.
E.g. you can configure default value directly in the application.conf and customize it through system property if necessary.
```
someField = 1234
someField = ${?my.field.value}
```
