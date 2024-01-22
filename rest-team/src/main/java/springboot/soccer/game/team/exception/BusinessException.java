package springboot.soccer.game.team.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final transient Object[] params;
    private final ErrorCode errorCode;

    public BusinessException(String message, ErrorCode errorCode, Object... params) {
        super(message);
        this.params = params;
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause, ErrorCode errorCode, Object... params) {
        super(message, cause);
        this.params = params;
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        GENERAL,
        TEAM_NOT_FOUND,
        THERE_IS_NO_TEAM_FOUND,
        ERROR_TO_PERSIST,
        ERROR_TO_PERSIST_OPTIMISTIC_LOCK;
    }

}

