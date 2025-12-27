package _4.NovemberRecipeMarket.domain.dto.order;

import _4.NovemberRecipeMarket.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 주문 생성 후 프론트에 전달할 응답
@Getter
@Builder
@AllArgsConstructor
public class OrderReadyResponse {
    private Long orderId;
    private int totalAmount;
    private List<OrderItemResponse> orderedItems;
}
