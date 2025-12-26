package _4.NovemberRecipeMarket.domain.dto.cart.response;

import _4.NovemberRecipeMarket.domain.dto.cart.CartItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailsResonse {
    private List<CartItemDto> cartOrderList;

    private int numberOfUniqueItems;
    private int itemCost;
    private int deliveryCost;
    private int totalCost;

}
