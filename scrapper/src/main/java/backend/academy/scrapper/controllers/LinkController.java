package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.services.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "link")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/links")
@Slf4j
public class LinkController {

    private final LinkService linkService;

    @Operation(summary = "get all tracked links")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "links are received",
            content = @Content(schema = @Schema(implementation = ListLinksResponse.class))),
        @ApiResponse(responseCode = "400", description = "invalid request parameters",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "chat not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") @NotNull @Positive Long chatId) {
        return linkService.getChatLinks(chatId);
    }

    @Operation(summary = "add a tracking link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "link is added",
            content = @Content(schema = @Schema(implementation = LinkResponse.class))),
        @ApiResponse(responseCode = "400", description = "invalid request parameters",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "chat not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "link already added",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public LinkResponse addLink(
        @RequestHeader("Tg-Chat-Id") @NotNull @Positive Long chatId,
        @RequestBody @Valid AddLinkRequest linkRequest
    ) {
        return linkService.addLinkToChat(chatId, linkRequest.link(), linkRequest.tags(), linkRequest.filters());
    }

    @Operation(summary = "remove a tracked link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "link is removed",
            content = @Content(schema = @Schema(implementation = LinkResponse.class))),
        @ApiResponse(responseCode = "400", description = "invalid request parameters",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "chat or link not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping
    public LinkResponse removeLink(
        @RequestHeader("Tg-Chat-Id") @NotNull @Positive Long chatId,
        @RequestBody @Valid RemoveLinkRequest linkRequest
    ) {
        return linkService.removeLinkFromChat(chatId, linkRequest.link());
    }
}
