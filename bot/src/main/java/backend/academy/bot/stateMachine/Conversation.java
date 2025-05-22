package backend.academy.bot.stateMachine;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Conversation {
    private State state;
    private String link;
    private List<String> tags;
    private List<String> filters;

    public Conversation() {
        this.state = State.START;
    }

    public void reset() {
        this.state = State.COMMAND_WAITING;
        this.link = null;
        this.tags = null;
        this.filters = null;
    }
}
