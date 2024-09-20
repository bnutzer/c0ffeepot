package bnutzer.c0ffeepot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class C0ffeepotTest {

    @Container
    private static final GenericContainer<?> c0ffeepot = new GenericContainer<>("bnutzer/c0ffeepot").withExposedPorts(80);

    private static URI buildUri(String query) throws URISyntaxException {
        return buildUri(query, "/");
    }

    private static URI buildUri(String query, String path) throws URISyntaxException {
        return new URI("http",
                null,
                c0ffeepot.getHost(),
                c0ffeepot.getMappedPort(80),
                path,
                query,
                "");
    }

    private static <T> T executeRequest(URI uri,
                                 Function<URI, ClassicHttpRequest> requestBuilder,
                                 HttpClientResponseHandler<? extends T> responseHandler) throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(requestBuilder.apply(uri), responseHandler);
        }
    }

    private static String getResponseBody(ClassicHttpResponse response) throws IOException, ParseException {

        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
}

    @Test
    void testDefaultRequest() throws IOException, URISyntaxException {

        var uri = buildUri("");
        var code = executeRequest(uri, HttpGet::new, HttpResponse::getCode);
        assertThat(code).isEqualTo(200);
    }

    @Test
    void test404IsReturned() throws IOException, URISyntaxException {

        var uri = buildUri("status=404");
        var code = executeRequest(uri, HttpGet::new, HttpResponse::getCode);
        assertThat(code).isEqualTo(404);
    }

    @Test
    void test503IsReturned() throws IOException, URISyntaxException {

        var uri = buildUri("status=503");
        var code = executeRequest(uri, HttpGet::new, HttpResponse::getCode);
        assertThat(code).isEqualTo(503);
    }

    @Test
    void testLocationIsReturned() throws IOException, URISyntaxException {

        var uri = buildUri("location=http://example.com");
        var locationHeader = executeRequest(uri, HttpGet::new, response -> response.getFirstHeader("Location").getValue());
        assertThat(locationHeader).isEqualTo("http://example.com");
    }

    @Test
    void testBodyIsReturned() throws URISyntaxException, IOException {

        var uri = buildUri("body=hi+there");
        var body = executeRequest(uri, HttpGet::new, C0ffeepotTest::getResponseBody);
        assertThat(body).isEqualToIgnoringNewLines("hi there");
    }

    @Test
    void testNonRootPath() throws IOException, URISyntaxException {

        var uri = buildUri("", "/foo/bar");
        var code = executeRequest(uri, HttpGet::new, HttpResponse::getCode);
        assertThat(code).isEqualTo(200);
    }

    @Test
    void testRespondsToPost() throws URISyntaxException, IOException {

        var uri = buildUri("");
        var code = executeRequest(uri, HttpPost::new, HttpResponse::getCode);
        assertThat(code).isEqualTo(200);
    }
}
