package springboot.soccer.game.team.exception;

import lombok.Getter;

public class EntityNotFoundException extends Exception {

    @Getter
    private final transient Object[] args;

    public EntityNotFoundException(String messageKey, Object... args)
    {
        super(messageKey);
        this.args = args;
    }

}

