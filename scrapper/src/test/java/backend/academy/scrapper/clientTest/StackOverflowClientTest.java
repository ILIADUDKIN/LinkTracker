package backend.academy.scrapper.clientTest;

import backend.academy.scrapper.client.StackOverflowClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

// Тест на клиент SO //
@ExtendWith(MockitoExtension.class)
class StackOverflowClientTest {

    @RegisterExtension
    private static final WireMockExtension wireMockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();
    private static final String API_QUESTION = "/questions/.*?.*";
    private static final String API_QUESTION_ANSWERS = "/questions/.*/answers?.*";
    private StackOverflowClient stackoverflowClient;

    @BeforeEach
    void init() {
        stackoverflowClient = HttpServiceProxyFactory
            .builderFor(
                WebClientAdapter.create(
                    WebClient.builder().baseUrl(wireMockExtension.baseUrl()).build()
                ))
            .build()
            .createClient(StackOverflowClient.class);
    }

    @Nested
    class GetRequestTest {

        @SneakyThrows
        @Test
        void response400Test() {
            wireMockExtension.stubFor(
                get(urlMatching(API_QUESTION))
                    .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "error_id": 400,
                                "error_message": "ids",
                                "error_name": "bad_parameter"
                            }
                            """)
                    )
            );

            WebClientResponseException ex = catchThrowableOfType(
                () -> stackoverflowClient.getQuestion("24840667"),
                WebClientResponseException.class
            );
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
