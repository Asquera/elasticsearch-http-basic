# HTTP Basic auth for ElasticSearch

This plugin provides an extension of ElasticSearchs HTTP Transport module to enable HTTP Basic authorization.

Requesting / does not request authentication to simplify health heck configuration.

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

The plugin is disabled by default. Enabling basic authorization will disable the default HTTP Transport module.

```
http.basic.enabled: true
http.basic.user: "my_username"
http.basic.password: "my_password"
```

Be aware that the password is stored in plain text.

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
