package _4.NovemberRecipeMarket.domain.entity;

import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String orderNumber;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Setter
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderedItemList = new ArrayList<>();

    @Builder.Default
    private int totalPrice = 0;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime orderedDate;

    @Setter
    @Column(unique = true)
    private String impUid;

    public Order(User user, List<OrderItem> orderedItemList, int totalPrice) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.orderNumber = createOrderNumber();
        this.orderedItemList = orderedItemList;
    }

    public static String createOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // 새로운 Order Entity를 생성 한 후,
    public static Order ready(User user) {
        return Order.builder()
                .orderNumber(createOrderNumber())
                .user(user)
                .orderStatus(OrderStatus.WAITING_PAYMENT)
                .build();
    }

    public void changeTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void markPaid(String impUid) {
        this.impUid = impUid;
        this.orderStatus = OrderStatus.PAID;
    }

    public boolean isPaid() {
        return this.orderStatus == OrderStatus.PAID;
    }
}

