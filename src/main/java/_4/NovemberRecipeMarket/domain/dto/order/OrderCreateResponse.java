package _4.NovemberRecipeMarket.domain.dto.order;

import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResponse {

    private Long orderId;
    private String orderNumber;
    private int numberOfUniqueItems;
    private int totalPrice;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;

}
