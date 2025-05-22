package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record QuestionAnswerDto(@JsonProperty("items") List<Answer> answers) {
    public record Answer(
        @JsonProperty("last_activity_date") OffsetDateTime updatedAt,
        @JsonProperty("creation_date") OffsetDateTime createdAt,
        @JsonProperty("answer_id") String id,
        @JsonProperty("question_id") String questionId,
        @JsonProperty("body") String body,
        @JsonProperty("owner") Owner owner
    ) {
        public record Owner(@JsonProperty("display_name") String displayName) {
        }
    }
}
