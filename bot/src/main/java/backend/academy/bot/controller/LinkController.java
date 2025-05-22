package backend.academy.bot.controller;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.stateMachine.StateMachine;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/updates")
@Validated
public class LinkController {

    private static final Logger logger = LoggerFactory.getLogger(LinkController.class);
    private static final String INCORRECT_REQUEST = "Некорректные параметры запроса";
    private static final String INCORRECT_REQUEST_CODE = "400";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION = "IllegalArgumentException";
    private static final String CHAT_ID_COULD_NOT_BE_NULL = "Chat ID не должен быть null";

    private final StateMachine stateMachine;

    public LinkController(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostMapping
    public ResponseEntity<?> receiveUpdate(@Valid @RequestBody backend.academy.bot.dto.LinkUpdate update) {
        if (!update.isEmpty()) {
            try {
                List<Long> chatIds = update.tgChatIds();
                for (Long chatId : chatIds) {
                    stateMachine.notifyUser(chatId, update);
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                    new ApiErrorResponse(INCORRECT_REQUEST, INCORRECT_REQUEST_CODE, ILLEGAL_ARGUMENT_EXCEPTION,
                        CHAT_ID_COULD_NOT_BE_NULL, new ArrayList<String>()));
            }
        } else {
            return ResponseEntity.badRequest().body(
                new ApiErrorResponse(INCORRECT_REQUEST, INCORRECT_REQUEST_CODE, ILLEGAL_ARGUMENT_EXCEPTION,
                    CHAT_ID_COULD_NOT_BE_NULL, new ArrayList<String>()));
        }
    }
}
