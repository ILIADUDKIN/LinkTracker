package backend.academy.scrapper.parsedLinkTest;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.ApiException;
import backend.academy.scrapper.utils.LinkParser;
import backend.academy.scrapper.utils.LinkSourceUtil;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(classes = {LinkSourceUtil.class})
@EnableConfigurationProperties(value = ScrapperConfig.class)
class LinkParserTest {

    static Stream<Arguments> validLink() {
        return Stream.of(
            Arguments.of("https://github.com/JetBrains/kotlin"),
            Arguments.of("https://github.com/JetBrains/kotlin/tree/branch-name"),
            Arguments.of("https://github.com/JetBrains/kotlin/pull/1"),
            Arguments.of("https://github.com/JetBrains/kotlin/issues/1"),
            Arguments.of("https://stackoverflow.com/questions/24840667"),
            Arguments.of("https://stackoverflow.com/q/24840667"),
            Arguments.of("https://stackoverflow.com/questions/24840667/question-title")
        );
    }

    static Stream<Arguments> invalidLink() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("dummy"),
            Arguments.of("github.com/JetBrains/kotlin"),
            Arguments.of("http:/github.com/JetBrains/kotlin"),
            Arguments.of("https://stackoverflow.com/search!q=exception")
        );
    }

    static Stream<Arguments> notSupportedLink() {
        return Stream.of(
            Arguments.of("https://www.baeldung.com/mockito-series"),
            Arguments.of("https://leetcode.com/problemset/algorithms/"),
            Arguments.of("https://github.com"),
            Arguments.of("https://stackoverflow.com"),
            Arguments.of("https://github.com/JetBrains/kotlin/releases"),
            Arguments.of("https://github.com/JetBrains/kotlin/stargazers"),
            Arguments.of("https://github.com/JetBrains/kotlin/pulls"),
            Arguments.of("https://github.com/JetBrains/kotlin/issues"),
            Arguments.of("https://stackoverflow.com/a/32872406"),
            Arguments.of("https://stackoverflow.com/questions/24840667#32872406")
        );
    }

    @Nested
    class ParseLinkTest {

        @ParameterizedTest
        @MethodSource("backend.academy.scrapper.parsedLinkTest.LinkParserTest#validLink")
        void shouldReturnLinkWhenLinkIsValid(String url) {
            Link parsedLink = LinkParser.parseLink(url);

            assertThat(parsedLink.url()).isEqualTo(url);
        }

        @ParameterizedTest
        @MethodSource("backend.academy.scrapper.parsedLinkTest.LinkParserTest#invalidLink")
        void shouldThrowExceptionWhenLinkIsInvalid(String url) {
            assertThatThrownBy(() -> LinkParser.parseLink(url))
                .isInstanceOf(ApiException.class);
        }

        @ParameterizedTest
        @MethodSource("backend.academy.scrapper.parsedLinkTest.LinkParserTest#notSupportedLink")
        void shouldThrowExceptionWhenLinkIsNotSupported(String url) {
            assertThatThrownBy(() -> LinkParser.parseLink(url))
                .isInstanceOf(ApiException.class);
        }
    }
}
