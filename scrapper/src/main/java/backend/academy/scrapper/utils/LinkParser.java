package backend.academy.scrapper.utils;


import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.ApiExceptionType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkParser {

    private static final String URL_PATTERN = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";

    public static Link parseLink(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw ApiExceptionType.INVALID_LINK.toException();
        }
        if (!Pattern.matches(URL_PATTERN, url)) {
            throw ApiExceptionType.INVALID_LINK.toException();
        }
        return LinkSourceUtil.getLinkType(uri.getHost(), url)
            .map(it -> Link.builder()
                .linkType(it)
                .url("https://" + url.substring(url.indexOf(LinkSourceUtil.getDomain(it))))
                .build())
            .orElseThrow(ApiExceptionType.NOT_SUPPORTED_SOURCE::toException);
    }
}
