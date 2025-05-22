package backend.academy.scrapper.utils;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.linkConditions.LinkType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkSourceUtil {

    private static ScrapperConfig applicationConfig;
    private static Map<LinkType, String> domains;
    private static Map<LinkType, List<String>> regex;

    public LinkSourceUtil(ScrapperConfig applicationConfig) {
        LinkSourceUtil.applicationConfig = applicationConfig;
        domains = initDomainMap(applicationConfig);
        regex = initRegexMap(applicationConfig);
    }

    public static Optional<ScrapperConfig.LinkSource> getLinkSource(LinkType linkType) {
        return Optional.ofNullable(applicationConfig.linkSources().get(linkType));
    }

    public static String getDomain(LinkType linkType) {
        return domains.get(linkType);
    }

    public static Optional<LinkType> getLinkType(String host, String url) {
        Optional<LinkType> optype = domains.entrySet().stream()
            .filter(it -> it.getValue().equals(host))
            .map(Map.Entry::getKey)
            .filter(it -> isSupportedSource(it, url.substring(url.indexOf(domains.get(it)))))
            .findFirst();
        log.info(optype.toString());
        return optype;
    }

    private static boolean isSupportedSource(LinkType linkType, String url) {
        return regex.get(linkType).stream()
            .anyMatch(it -> Pattern.matches(domains.get(linkType) + it, url));
    }

    private Map<LinkType, List<String>> initRegexMap(ScrapperConfig applicationConfig) {
        return applicationConfig.linkSources().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().handlers().values().stream()
                    .map(ScrapperConfig.LinkSourceHandler::regex)
                    .toList()
            ));
    }

    private Map<LinkType, String> initDomainMap(ScrapperConfig applicationConfig) {
        Map<LinkType, String> map = applicationConfig.linkSources().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().domain()
            ));
        log.info(map.toString());
        return map;
    }
}
