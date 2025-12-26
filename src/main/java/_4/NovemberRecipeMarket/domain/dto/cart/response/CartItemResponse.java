package _4.NovemberRecipeMarket.domain.dto.cart.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponse {
    private Long itemId;
    private String itemName;
    private int quantity;
    private String message;

}
