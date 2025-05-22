package backend.academy.scrapper.controllers;


import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "chat")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/tg-chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "register chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "chat is registered",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "invalid request parameters",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "chat already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}")
    public void registerChat(@PathVariable("id") @Positive Long chatId) {
        chatService.registerChat(chatId);
    }

    @Operation(summary = "delete chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "chat is deleted",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "invalid request parameters",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "chat not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable("id") @Positive Long chatId) {
        chatService.deleteChat(chatId);
    }
}
