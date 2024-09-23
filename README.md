c0ffeepot
=========

c0ffeepot is a web server that will respond to http requests with a
client-defined response. The client can request a specific http status
code, and other trivial response properties.

c0ffeepot is intended to be used in (unit/integration) tests. Never ever run
it in production environments or other contexts where it will respond to
user-supplied data.

c0ffeepot's regular habitat is a docker container running a php and apache.
Feel free to use the default c0ffeepot docker hub repository:

https://hub.docker.com/r/bnutzer/c0ffeepot

Synopsis
========
```shell
docker run --detach --rm bnutzer/c0ffeepot -p 8080:80 --name c0ffeepot
curl -D - http://localhost:8080/?status=404
docker rm c0ffeepot
```

Usage
=====

c0ffeepot has three ways of injecting response attributes:
* Query parameters
* Path segments
* Preloaded data

These are the available attributes:
* `status` The http status to respond with (e.g., 200, 201, 503, ...)
* `location` A "Location" header in the response. Empty per default.
* `contenttype` A "Content-Type" header in the response. "application/json" per
  default.
* `body` The response body. A sample hello-world json response per default.

Query parameters and path segments can be mixed.

Samples:
```shell
$ curl -D - http://localhost:8080/status/200?body=hi+there
$ curl -D - http://localhost:8080/status/302?location=http://example.com

```

Preloading
==========

For testing of transitive requests, it can be helpful to first set up c0ffeepot to respond with certain
properties, and then perform the transitive request. To do so, "preload" c0ffeepot with the properties
it should return in the next call:

```shell
$ curl -D - http://localhost:8080/preload/status/200?body=hi+there
$ curl -D - http://localhost:8080/api/whatever

```

After preloading, the preloaded data will be returned in the next request, after which the data will
be discarded.

Build your own image
====================

If you do not want to use the docker hub image, you may build your own
c0ffeepot docker image:
```shell
docker build -t c0ffeepot .
```

Usage in testcontainers
=======================

c0ffeepot's best friend is testcontainers,
https://www.testcontainers.org/

See the samples/ directory for a more verbose version of this.

Java sample usage:

```java
    @Container
    private static final GenericContainer<?> c0ffeepot = new GenericContainer<>("bnutzer/c0ffeepot").withExposedPorts(80);

    @Test
    void okIsOk() throws IOException {

        c0ffeepot.start();

        var url = "http://localhost:" + c0ffeepot.getFirstMappedPort();

        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                assertThat(response).isNotNull();
                assertThat(response.getCode()).isEqualTo(200);
            }
        }
    }

```
