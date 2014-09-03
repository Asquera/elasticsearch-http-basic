# HTTP Basic auth for ElasticSearch

This plugin provides an extension of ElasticSearchs HTTP Transport module to enable HTTP Basic authorization.

Requesting / does not request authentication to simplify health check configuration.

There is no way to configure this on a per index basis.

## Version Mapping

|     Http Basic Plugin       | elasticsearch         |
|-----------------------------|-----------------------|
| 1.2.0(master)               | 1.2.0                 |
| 1.1.0                       | 1.0.0                 |
| 1.0.4                       | 0.90.7                |

## Installation

Download the current version from https://github.com/Asquera/elasticsearch-http-basic/releases and copy it to `plugins/http-basic`.

## Configuration

Once the plugin is installed it can be configured in the [elasticsearch modules configuration file](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/setup-configuration.html#settings). See the [elasticserach directory layout information](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/setup-dir-layout.html) for more information about the default paths of an ES installation.

|     Setting key             |  Default value               | Notes                                                                   |
|-----------------------------|------------------------------|-------------------------------------------------------------------------|
| `http.basic.enabled`        | true                         | **true** disables the default ES HTTP Transport module                  |
| `http.basic.user`           | "admin"                      |                                                                         |
| `http.basic.pasword`        | "admin_pw"                   |                                                                         |
| `http.basic.whitelist`      | ["localhost", "127.0.0.1"]   |                                                                         |
| `http.basic.log`            | false                        | enables pugin logging to ES log                                         |
| `http.basic.xforward`       | ""                           | example [X-Forwarded-For](http://en.wikipedia.org/wiki/X-Forwarded-For) |

Be aware that the password is stored in plain text.

### configuration example

The following code enables plugin logging, and sets user and password:

```
http.basic.log: true
http.basic.user: "some_user"
http.basic.password: "some_password"
```

## Testing

```
$ curl -v localhost:9200 # works
$ curl -v --user my_username:my_password localhost:9200/foo # works
$ curl -v --user my_username:password localhost:9200/foo # sends 401
```

## Problems

This will not send WWW-Authorize headers - this is due to elasticsearch not allowing to add custom headers to responses.

## Issues

Please file your issue here: https://github.com/Asquera/elasticsearch-http-basic/issues
