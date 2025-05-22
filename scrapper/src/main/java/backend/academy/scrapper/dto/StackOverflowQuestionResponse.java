package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowQuestionResponse(
    Long questionId,
    String title,
    @JsonProperty("last_activity_date")
    OffsetDateTime lastActivityDate
) {
}
