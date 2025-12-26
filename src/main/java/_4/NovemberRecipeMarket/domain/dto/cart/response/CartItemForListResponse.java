package _4.NovemberRecipeMarket.domain.dto.cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CartItemForListResponse {
    private Long cartId;

    private Long itemId;

    private String itemName;

    private String imagePath;

    private Integer price;

    private Integer quantity;

}
