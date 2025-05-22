package backend.academy.scrapper.validDataTest;

import backend.academy.scrapper.controllers.LinkController;
import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.services.LinkService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

//тесты на сохранение ссылок в репозиториях + тесты на happy-path
@ExtendWith(MockitoExtension.class)
class ValidDataTest {

    private static final Long CHAT_ID = 123L;
    private static final String VALID_URL = "https://stackoverflow.com/questions/12345678";
    @Mock
    private LinkService linkService;
    @InjectMocks
    private LinkController linkController;

    @Test
    void getLinks_ValidChatId_ReturnsLinks() {
        // Arrange
        List<LinkResponse> expectedLinks = List.of(
            new LinkResponse(1L, "https://stackoverflow.com/questions/12345678", List.of("tag1"), List.of("filter1")),
            new LinkResponse(2L, "https://stackoverflow.com/questions/12345678", List.of("tag2"), List.of("filter2"))
        );
        when(linkService.getChatLinks(CHAT_ID)).thenReturn(new ListLinksResponse(expectedLinks, 2));

        // Act
        ListLinksResponse actualLinks = linkController.getLinks(CHAT_ID);

        // Assert
        assertThat(actualLinks.links()).isEqualTo(expectedLinks);
        assertThat(actualLinks.size()).isEqualTo(expectedLinks.size());
    }

    @Test
    void addLink_ValidRequest_ReturnsLinkResponse() {
        // Arrange
        AddLinkRequest addLinkRequest = new AddLinkRequest(VALID_URL, List.of("java", "stack"), List.of("upvotes"));
        LinkResponse expectedLinkResponse = new LinkResponse(1L, VALID_URL, List.of("java", "stack"), List.of("upvotes"));

        when(linkService.addLinkToChat(eq(CHAT_ID), eq(VALID_URL), anyList(), anyList())).thenReturn(expectedLinkResponse);

        // Act
        LinkResponse actualLinkResponse = linkController.addLink(CHAT_ID, addLinkRequest);

        // Assert
        assertThat(actualLinkResponse).isEqualTo(expectedLinkResponse);
    }

    @Test
    void addLink_InvalidUrl_ThrowsException() {
        // Arrange
        String invalidUrl = "not a valid url";
        AddLinkRequest addLinkRequest = new AddLinkRequest(invalidUrl, List.of("java", "stack"), List.of("upvotes"));

        when(linkService.addLinkToChat(eq(CHAT_ID), eq(invalidUrl), anyList(), anyList()))
            .thenThrow(new IllegalArgumentException("Invalid URL"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> linkController.addLink(CHAT_ID, addLinkRequest));
    }


    @Test
    void addLink_NullAddLinkRequest_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> linkController.addLink(CHAT_ID, null));
    }

    @Test
    void addLink_EmptyTagsAndFilters_ReturnsLinkResponse() {
        // Arrange
        String validUrl = "https://example.com";
        AddLinkRequest addLinkRequest = new AddLinkRequest(validUrl, List.of(), List.of());
        LinkResponse expectedLinkResponse = new LinkResponse(1L, validUrl, List.of(), List.of());

        when(linkService.addLinkToChat(eq(CHAT_ID), eq(validUrl), anyList(), anyList())).thenReturn(expectedLinkResponse);

        // Act
        LinkResponse actualLinkResponse = linkController.addLink(CHAT_ID, addLinkRequest);

        // Assert
        assertThat(actualLinkResponse).isEqualTo(expectedLinkResponse);
    }

    @Test
    void removeLink_EmptyTagsAndFilters_ReturnsLinkResponse() {
        // Arrange
        String linkToRemove = "https://example.com";
        Long linkId = 67890L;
        LinkResponse expectedLinkResponse = new LinkResponse(linkId, linkToRemove, Collections.emptyList(), Collections.emptyList());
        when(linkService.removeLinkFromChat(CHAT_ID, linkToRemove)).thenReturn(expectedLinkResponse);

        // Act
        LinkResponse actualLinkResponse = linkController.removeLink(CHAT_ID, new RemoveLinkRequest(linkToRemove));

        // Assert
        assertEquals(expectedLinkResponse, actualLinkResponse);

        // Verify
        verify(linkService, times(1)).removeLinkFromChat(CHAT_ID, linkToRemove);
        verifyNoMoreInteractions(linkService);

    }
}
