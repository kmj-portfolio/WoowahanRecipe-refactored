package _4.NovemberRecipeMarket.domain.dto.cart.response;

import lombok.Getter;

@Getter
public class CartItemDeleteResponse {
    private String itemName;
    private String message;

    public CartItemDeleteResponse(String itemName) {
        this.itemName = itemName;
        message = "상품을 삭제했습니다.";
    }
}
