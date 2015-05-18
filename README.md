[![Build Status](https://travis-ci.org/emig/elasticsearch-http-basic.svg?branch=1.5)](https://travis-ci.org/emig/elasticsearch-http-basic)

**IMPORTANT NOTICE**: versions 1.0.4 is *insecure and should not be used*.
They have a bug that allows an attacker to get ip authentication by setting
its ip on the 'Host' header.

# HTTP Basic auth for ElasticSearch

This plugin provides an extension of ElasticSearchs HTTP Transport module to enable HTTP Basic authorization and
Ip based authorization.

Requesting `/` does not request authentication to simplify health check configuration.

There is no way to configure this on a per index basis.


## Version Mapping

|     Http Basic Plugin       | elasticsearch         |
|-----------------------------|-----------------------|
| v1.4.0(master)              | 1.4.0                 |
| v1.3.0                      | 1.3.0                 |
| v1.2.0                      | 1.2.0                 |
| 1.1.0                       | 1.0.0                 |
| 1.0.4                       | 0.90.7                |

## Installation

Download the current version from https://github.com/Asquera/elasticsearch-http-basic/releases and copy it to `plugins/http-basic`.

## Configuration

Once the plugin is installed it can be configured in the [elasticsearch modules configuration file](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/setup-configuration.html#settings). See the [elasticserach directory layout information](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/setup-dir-layout.html) for more information about the default paths of an ES installation.

|     Setting key                   |  Default value               | Notes                                                                   |
|-----------------------------------|------------------------------|-------------------------------------------------------------------------|
| `http.basic.enabled`              | true                         | **true** disables the default ES HTTP Transport module                  |
| `http.basic.user`                 | "admin"                      |                                                                         |
| `http.basic.password`              | "admin_pw"                   |                                                                         |
| `http.basic.ipwhitelist`            | ["localhost", "127.0.0.1"]   | uses Host Name Resolution from [java.net.InetAddress](http://docs.oracle.com/javase/7/docs/api/java/net/InetAddress.html)                     |
| `http.basic.trusted_proxy_chains` | []                           | Set an array of trusted proxies ips chains                              |
| `http.basic.log`                  | false                        | enables plugin logging to ES log. Unauthenticated requests are always logged.                                         |
| `http.basic.xforward`             | ""                           | most common is [X-Forwarded-For](http://en.wikipedia.org/wiki/X-Forwarded-For) |

Be aware that the password is stored in plain text.

## Ip based authentication

A client is **authenticated iff** its **request** is **trusted** and its **ip is whitelisted**.
A Request from a client connected *directly* (direct client) is **trusted**.  Its ip is the request ip.
A Request form a client connected *via proxies* (remote client) is **trusted iff** there is a tail
subchain of the request chain that matches a tail subchain of the trusted proxy chains.

**A tail subchain** of a chain "*A,B,C*" is a subchain that matches it by the end.
Example: the 3 tail subchains of the ip chain *A,B,C* are:

    (pseudo code) tailSubchains("A,B,C") --> ["A,B,C", "B,C", "C"]

The request chain of a remote client is obtained following these steps:

- read the request's xforward configured header field.
- remove the xforwarded defined client's ip (first listed ip as defined by X-Forwarded-For) from it.
- append the request ip to it.

The ip chain of a remote client is the ip previous to the longest trusted tail subchain .Is the ip used to check
  against the whitelist.


### Request chain checks

Having the following configuration:

    http.basic.xforward = 'X-Forwarded-For'
    http.basic.trusted_proxy_chains = ["B,C", "Z"]

#### Trusted cases:

- A remote client with ip *A* connects to [server] via proxies with ips *B* and *C*. *X-Forwarded-For* header has "*A,B*", removing the client's ip "*A*" and adding the request ip *C*, the resulting chain *B,C* matches a trusted tail subchain. Client's ip is A.

        [A] --> B --> C --> [server]

- A remote client  with ip *A* connects to [server] via proxies with ips *R*, *P*, *B*  and *C*. *X-Forwarded-For* header has "*A,R,P,B*".
  Removing the client's ip "*A*" and adding the request ip *C* , the resulting chain ** matches a trusted tail subchain. **note**: in this case "*P*" is taken as the client's ip, and checked against the white list. Client's ip is P.

        [A] --> R --> P --> B --> C --> [server]

- A remote client with ip *A* connects to [server] via *C*. *X-Forwarded-For* header has
  *A*, removing the client's ip *A*  and adding the request ip *C*, the resulting chain *C* matches a trusted tail subchain. Client's ip is A.

        [A] --> C --> [server]

- client *A* connects directly to [server]. *X-Forwarded-For* header is not set. Client's ip is A.

        [A] --> [server]

#### Untrusted cases:

- A remote client with ip *A* connects to [server] via *D*. *X-Forwarded-For* header has
  "*A*", removing the client's ip "*A*"  and adding the request ip *D*, the resulting chain *D* doesn't match any trusted sub ip chain.

        [A] --> D --> [server]

- A remote client with ip *X* connects to proxy with ip *C* passing a faked *X-Forwarded-For* header "*R*". *C* will check the IP of the request and add it to the *X-Forwarded-For* field. the server will receive and *X-Forwarded-For* header
  as: "*R,X*", remove the client's ip "*R*", add the request ip "*C*" and finally drop the request, as "*X,C*" doesn't match the trusted ip.

        [X] -- R --> C --> [server]


### configuration example

The following code enables plugin logging, sets user and password, sets chain
"1.1.1.1,2.2.2.2" as trusted , whitelists ip 3.3.3.3 and defines xforward
header as the common 'X-Forwarded-For':

```
http.basic.log: true
http.basic.user: "some_user"
http.basic.password: "some_password"
http.basic.ipwhitelist: ["3.3.3.3"]
http.basic.xforward: "X-Forwarded-For"
http.basic.trusted_proxy_chains: ["1.1.1.1,2.2.2.2"]
```

## Testing

```
$ curl -v localhost:9200 # works
$ curl -v --user my_username:my_password localhost:9200/foo # works
```

**note:** localhost is a whitelisted ip as default.
```
$ curl -v --user my_username:password localhost:9200/foo # sends 401
```

## Development

### Testing
  Maven is configured to run the unit and integration tests. This plugin makes
  use of [ES Integration Tests](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/integration-tests.html)

  `mvn test` test runs all tests
  `mvn integration-test` test runs integration tests only

## Issues

Please file your issue here: https://github.com/Asquera/elasticsearch-http-basic/issues
