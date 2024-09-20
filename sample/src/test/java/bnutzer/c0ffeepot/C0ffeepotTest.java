package bnutzer.c0ffeepot;

import lombok.SneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class C0ffeepotTest {

    @Container
    private static final GenericContainer<?> c0ffeepot = new GenericContainer<>("bnutzer/c0ffeepot").withExposedPorts(80);

    @SneakyThrows
    private void withResponseAssert(String query, Consumer<CloseableHttpResponse> responseConsumer) {

        var url = new URI("http",
                null,
                c0ffeepot.getHost(),
                c0ffeepot.getMappedPort(80),
                "/",
                query,
                "").toURL().toString();

        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                responseConsumer.accept(response);
            }
        }
    }

    @SneakyThrows
    private void withResponseContentAssert(String query, Consumer<String> responseContentConsumer) {

        withResponseAssert(query, response ->
        {
            HttpEntity entity = response.getEntity();
            try {
                var resultContent = EntityUtils.toString(entity);
                responseContentConsumer.accept(resultContent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testDefaultRequest() {

        withResponseAssert("", response -> assertThat(response.getCode()).isEqualTo(200));
    }

    @Test
    void test404IsReturned() {

        withResponseAssert("status=404", response -> assertThat(response.getCode()).isEqualTo(404));
    }

    @Test
    void test503IsReturned() {

        withResponseAssert("status=503", response -> assertThat(response.getCode()).isEqualTo(503));
    }

    @Test
    void testLocationIsReturned() {

        withResponseAssert("location=http://example.com",
                response -> assertThat(response.getFirstHeader("Location").getValue()).isEqualTo("http://example.com"));
    }

    @Test
    void testBodyIsReturned() {

        withResponseContentAssert("body=hi+there",
                resultContent -> assertThat(resultContent).isEqualToIgnoringNewLines("hi there"));
    }
}
