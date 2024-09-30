package bnutzer.c0ffeepot;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class C0ffeepotSpringRestClientTest extends C0ffeepotSpringResponseEntityTest {

    private static RestClient buildRestClient() {
        return RestClient.builder()
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> { })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> { })
                .build();
    }

    private static final RestClient REST_CLIENT = buildRestClient();
    private static final RestClient.RequestHeadersUriSpec<?> GET_REQUEST = REST_CLIENT.get();
    private static final RestClient.RequestHeadersUriSpec<?> POST_REQUEST = REST_CLIENT.get();

    @Override
    ResponseEntity<String> executeGet(URI uri) {
        return executeRequest(uri, GET_REQUEST);
    }

    @Override
    ResponseEntity<String> executePost(URI uri) {
        return executeRequest(uri, POST_REQUEST);
    }

    @Override
    ResponseEntity<String> executePost(URI uri, String postBody) {

        return REST_CLIENT
                .post()
                .uri(uri)
                .body(postBody)
                .retrieve()
                .toEntity(String.class);
    }

    private ResponseEntity<String> executeRequest(URI uri, RestClient.RequestHeadersUriSpec<?> request) {

        return request
                .uri(uri)
                .retrieve()
                .toEntity(String.class);
    }
}
