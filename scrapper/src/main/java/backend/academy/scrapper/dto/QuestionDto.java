package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record QuestionDto(@JsonProperty("items") List<Question> questions) {
    public record Question(@JsonProperty("last_activity_date") OffsetDateTime updatedAt, String title) {
    }

    ;
}
