package _4.NovemberRecipeMarket.domain.dto.order;

import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderInfoResponse {

    private String impUid;
    private String username;
    private String orderNumber;

    private int totalPrice;

    private OrderStatus orderStatus;

    private LocalDateTime orderDate;

    @Builder
    public OrderInfoResponse(String impUid, String username, String orderNumber, int totalPrice,
                             OrderStatus orderStatus, LocalDateTime orderDate) {
        this.impUid = impUid;
        this.username = username;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }
}
