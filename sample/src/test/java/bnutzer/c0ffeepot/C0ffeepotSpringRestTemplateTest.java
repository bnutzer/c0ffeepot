package bnutzer.c0ffeepot;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class C0ffeepotSpringRestTemplateTest extends C0ffeepotSpringResponseEntityTest {

    private static RestTemplate buildErrorIgnoringRestTemplate() {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        return restTemplate;
    }

    private static final RestTemplate REST_TEMPLATE = buildErrorIgnoringRestTemplate();

    @Override
    ResponseEntity<String> executeGet(URI uri) {
        return executeRequest(uri, HttpMethod.GET);
    }

    @Override
    ResponseEntity<String> executePost(URI uri) {
        return executeRequest(uri, HttpMethod.POST);
    }

    private ResponseEntity<String> executeRequest(URI uri, HttpMethod method) {

        return REST_TEMPLATE.exchange(uri, method, null, String.class);
    }
}
