package _4.NovemberRecipeMarket.repository.order;

import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import lombok.Getter;

@Getter
public class OrderSearch {
    private String itemName;
    private OrderStatus orderStatus;
}
