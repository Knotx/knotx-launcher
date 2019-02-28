# Knot.x Launcher

Knot.x Launcher provides a way to configure your [Knot.x Stack](https://github.com/Knotx/knotx-stack) 
instance. It uses [Vert.x Config](https://vertx.io/docs/vertx-config/java/) module and the concept 
of configuration stores.

Launcher has two main classes are:

- `io.knotx.launcher.KnotxCommand` - allows to run Knot.x instance with `run-knotx` command
- `io.knotx.launcher.KnotxStarterVerticle` - deploys all Verticles defined in a configuration file

## How to run
A `run-knotx` command runs Knot.x (Vert.x) instance and deploys all configured Knot.x modules. It 
can be executed with options:

- config - allows to define `bootstrap.json` configuration file path
- ha - specify that the Knot.x is deployed with High Availability (HA) enabled, see the 
[Automatic failover](https://vertx.io/docs/vertx-core/java/#_automatic_failover) section for more 
details
- hagroup - HA group that denotes a logical group of nodes in the cluster, see more in the 
[HA Groups](https://vertx.io/docs/vertx-core/java/#_ha_groups) section.
- quora - it is the minimum number of votes that a distributed transaction has to obtain in order to 
be allowed to perform an operation in a distributed system
- cluster - enables the clustering, by the default it uses Hazelcast cluster manager, more details 
can be found in the [Vert.x Hazelcast Cluster Manager](https://vertx.io/docs/vertx-hazelcast/java/) 
documentation.

Knot.x provides the [example project](https://github.com/Knotx/knotx-example-project) that provides Docker Compose 
[configuration file](https://github.com/Knotx/knotx-example-project/blob/master/acme-cluster/docker-compose.yml)
to demonstrate cluster and HA capabilities. 

[Knot.x Stack](https://github.com/Knotx/knotx-stack) defines 
[startup scripts](https://github.com/Knotx/knotx-stack/tree/master/knotx-stack-manager/src/main/packaging/bin) 
for both Unix and Windows that specify Java & Vert.x options.

## How to configure

The Knot.x configuration is basically split into two configuration files, such as
- bootstrap.json - a starter configuration what defines the application configuration format, 
location of application configurations (e.g. file, directory), as well as the ability to define 
whether the config should be scanned periodically
- application.conf - a main configuration file for the Knot.x based application. Knot.x 
promotes a [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) format as it provides 
usefull mechanism, such as includes, variable substitutions, etc.