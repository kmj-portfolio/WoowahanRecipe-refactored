package _4.NovemberRecipeMarket.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message); //커스텀 메시지도 가능
        this.errorCode = errorCode;
    }
}
