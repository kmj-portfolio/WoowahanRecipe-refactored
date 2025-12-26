package _4.NovemberRecipeMarket.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "사용자의 아이디가 중복됩니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일 입니다"),
    USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 아이디입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 다릅니다"),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "사용자가 권한이 없습니다."),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 판매자입니다."),
    DUPLICATE_BUSINESS_REG_NUM(HttpStatus.CONFLICT, "이미 등록된 사업자 등록 번호입니다."),
    INVALID_TOKEN(HttpStatus.NOT_FOUND, "만료된 토큰입니다."),
    ROLE_FORBIDDEN(HttpStatus.FORBIDDEN, "허용되지 않은 권한입니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB 에러입니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),

    // item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다"),
    NOT_ENOUGH_STOCK(HttpStatus.CONFLICT, "재고가 충분하지 않습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "품절된 상품입니다."),

    // recipe
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 레시피입니다."),

    // recipe item
    RECIPE_ITEM_DOES_NOT_EXIST(HttpStatus.NOT_FOUND, "레시피의 재료에 등록되지 않은 재료입니다."),

    //cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니 입니다."),

    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 존재하지 않는 상품입니다."),
    INVALID_QUANTITY(HttpStatus.CONFLICT, "0 이상의 수량만 가능합니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    SELECT_ORDER_ITEM(HttpStatus.FORBIDDEN, "주문할 상품을 선택해주세요"),

    FORBIDDEN(HttpStatus.FORBIDDEN, "사용자 정보와 일치하지 않음"),

    // payment
    MISMATCH_AMOUNT(HttpStatus.CONFLICT, "결제 금액이 주문 금액과 일치하지 않습니다."),
    INVALID_PAYMENT(HttpStatus.CONFLICT, "유효하지 않은 결제 요청입니다."),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "이미 다른 주문에서 처리된 결제입니다.");

    private HttpStatus httpStatus;
    private String message;
}
