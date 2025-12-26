package _4.NovemberRecipeMarket.domain.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemResponse {
    private String itemName;
    private int orderPrice;
    private int orderQuantity;
}
