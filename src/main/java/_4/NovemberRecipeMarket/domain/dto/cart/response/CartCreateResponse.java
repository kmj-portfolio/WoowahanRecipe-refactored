package _4.NovemberRecipeMarket.domain.dto.cart.response;

import lombok.Getter;

@Getter
public class CartCreateResponse {
    private Long cartId;
    private String message;

    public CartCreateResponse(Long cartId) {
        this.cartId = cartId;
        message = "장바구니가 생성되었습니다.";
    }
}
