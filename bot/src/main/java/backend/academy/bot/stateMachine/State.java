package backend.academy.bot.stateMachine;

public enum State {
    START,
    COMMAND_WAITING,
    EXECUTE,
    LINK_WAITING,
    DOES_USER_WAIT_INPUT_TAGS,
    DOES_USER_WAIT_INPUT_FILTERS,
    USER_WAITING_FOR_TAGS,
    USER_WAITING_FOR_FILTERS,
}
