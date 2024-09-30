package bnutzer.c0ffeepot;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
abstract class C0ffeepotSpringResponseEntityTest {

    @Container
    private static final GenericContainer<?> C0FFEE_POT_CONTAINER = new GenericContainer<>("bnutzer/c0ffeepot").withExposedPorts(80);

    private static URI buildUri(String query) throws URISyntaxException {
        return buildUri(query, "/");
    }

    private static URI buildUri(String query, String path) throws URISyntaxException {
        return new URI("http",
                null,
                C0FFEE_POT_CONTAINER.getHost(),
                C0FFEE_POT_CONTAINER.getMappedPort(80),
                path,
                query,
                "");
    }

    abstract ResponseEntity<String> executeGet(URI uri);
    abstract ResponseEntity<String> executePost(URI uri);
    abstract ResponseEntity<String> executePost(URI uri, String postBody);

    @Test
    void testDefaultRequest() throws URISyntaxException {

        var uri = buildUri("");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test404IsReturned() throws URISyntaxException {

        var uri = buildUri("status=404");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void test503IsReturned() throws URISyntaxException {

        var uri = buildUri("status=503");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void testLocationIsReturned() throws URISyntaxException {

        var uri = buildUri("location=http://example.com");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getHeaders()).containsEntry("Location", List.of("http://example.com"));
    }

    @Test
    void testBodyIsReturned() throws URISyntaxException {

        var uri = buildUri("body=hi+there");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getBody()).isEqualToIgnoringNewLines("hi there");
    }

    @Test
    void testNonRootPath() throws URISyntaxException {

        var uri = buildUri("", "/foo/bar");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testRespondsToPost() throws URISyntaxException {

        var uri = buildUri("");
        var responseEntity = executePost(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCanUsePathSegmentAttributes() throws URISyntaxException {

        var uri = buildUri("", "/status/500/location/foobar/body/hi+there");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders().getLocation()).hasToString("foobar");
        assertThat(responseEntity.getBody()).isEqualToIgnoringNewLines("hi there");
    }

    @Test
    void testCanMixQueryParametersAndPathSegments() throws URISyntaxException {

        var uri = buildUri("location=foobar", "/status/500/body/hi+there");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders().getLocation()).hasToString("foobar");
        assertThat(responseEntity.getBody()).isEqualToIgnoringNewLines("hi there");
    }

    @Test
    void testCanPreload() throws URISyntaxException {

        var uri = buildUri("", "/preload/status/500/location/foobar/body/hi+there");
        var responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        uri = buildUri("", "");
        responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders().getLocation()).hasToString("foobar");
        assertThat(responseEntity.getBody()).isEqualToIgnoringNewLines("hi there");
    }

    @Test
    void testCanPreloadJsonBody() throws URISyntaxException {

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonWithUmlaut = "[{\"foo\":\"bar\"}]";

        var uri = buildUri("", "/preload/status/200");
        var responseEntity = executePost(uri, jsonWithUmlaut);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        uri = buildUri("", "");
        responseEntity = executeGet(uri);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).satisfies(
            b -> {
                assertThat(b).isNotEmpty();
                List<Map<String, String>> response = objectMapper.readValue(b, new TypeReference<List<Map<String, String>>>() { });
                assertThat(response).hasSize(1);
                assertThat(response.getFirst()).containsEntry("foo", "bar");
            }
        );
    }
}
