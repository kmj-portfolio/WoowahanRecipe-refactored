package _4.NovemberRecipeMarket.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse extends RuntimeException {

    private ErrorCode errorCode;
    private String message;

}