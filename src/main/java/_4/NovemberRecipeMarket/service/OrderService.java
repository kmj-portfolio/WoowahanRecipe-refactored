package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.order.*;
import _4.NovemberRecipeMarket.domain.entity.*;
import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.*;
import _4.NovemberRecipeMarket.repository.order.OrderCustomRepository;
import _4.NovemberRecipeMarket.repository.order.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderCustomRepository orderCustomRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    // Order 생성
    @Transactional
    public OrderReadyResponse createOrder(String username, List<OrderCreateRequest> reqList) {

        User user = validateUserByUsername(username);

        // 금액 계산
        Order order = Order.ready(user);
        orderRepository.save(order);

        int totalCost = 0;
        List<OrderItem> orderItemList = new ArrayList<>();
        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();

        for (OrderCreateRequest request : reqList) {
            Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            int requestedQuantity = request.getQuantity();
            int orderPrice = item.getPrice() * requestedQuantity;
            totalCost += orderPrice;

            OrderItem orderItem = new OrderItem(item, orderPrice, requestedQuantity);
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);

            orderItemList.add(orderItem);
            orderItemResponseList.add(new OrderItemResponse(item.getItemName(), item.getPrice(), requestedQuantity));
        }

        order.changeTotalPrice(totalCost);

        return OrderReadyResponse.builder()
                .orderedItems(orderItemResponseList)
                .totalAmount(order.getTotalPrice())
                .orderId(order.getId())
                .build();
    }

    @Transactional
    public OrderCreateResponse finalizeOrderAfterPayment(Long orderId, String impUid, int paidAmount, String username) {
        User user = validateUserByUsername(username);

        // 주문 조회 & 소유자 검증
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!user.getId().equals(order.getUser().getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "본인의 주문만 결제할 수 있습니다.");
        }

        // 이미 결제 완료 처리된 주문인지 확인
        if (order.isPaid()) {

            // 요청한 imp_uid가 주문의 imp_uid와 같다면 바로 반환
            if (order.getImpUid() != null && order.getImpUid().equals(impUid)) {
                return toOrderCreateResponse(order);
            }
            // 주문의 imp_uid가 없거나, 다른 imp_uid라면 에러
            throw new AppException(ErrorCode.INVALID_PAYMENT);
        }

        // 아직 미결제 주문일 때, 중복 체크
        if (orderRepository.existsByImpUid(impUid)) {
            throw new AppException(ErrorCode.DUPLICATE_PAYMENT);
        }

        // 서버에 저장된 주문 금액과 PG 결제 금액 비교
        int serverTotalAmount = order.getTotalPrice();
        if (serverTotalAmount != paidAmount) {
            throw new AppException(ErrorCode.MISMATCH_AMOUNT, "결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        // 결제 정보 업데이트 (impUid & 상태 변경)
        order.markPaid(impUid);

        // 재고 차감 (비관적 락 사용)
        for (OrderItem orderItem : order.getOrderedItemList()) {
            Long itemId = orderItem.getItem().getId();
            Item lockedItem = itemRepository.findByIdWithLock(itemId)
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            // 재고 차감
            lockedItem.removeStock(orderItem.getQuantity());
        }

        // 주문한 상품 장바구니에서 비우기
        Cart cart = cartRepository.findCartByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        for (OrderItem orderItem : order.getOrderedItemList()) {
            CartItem cartItem = cartItemRepository.findByCartAndItem(cart, orderItem.getItem())
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

            cartItemRepository.delete(cartItem);
        }
        return toOrderCreateResponse(order);
    }


    public String cancelOrder(Long orderId, String username) {
        User user = validateUserByUsername(username);
        Order order = validateOrder(orderId);
        if (hasPermission(user, order)) {
            order.setOrderStatus(OrderStatus.CANCELLED);

            // increase stock
            for (OrderItem orderItem: order.getOrderedItemList()) {
                Item item = orderItem.getItem();
                item.increaseStock(orderItem.getQuantity());
            }
        }
        return "주문이 취소되었습니다.";
    }


    @Transactional(readOnly = true)
    public OrderInfoResponse getOrderById(Long orderId, String username) {
        User user = validateUserByUsername(username);
        Order order = validateOrder(orderId);
        hasPermission(user, order);
        return toOrderInfoResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderInfoResponse getOrderByOrderNumber(String orderNumber, String username) {
        validateUserByUsername(username);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return toOrderInfoResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderInfoResponse> getAllOrdersByUser(String username, Pageable pageable) {
        User user = validateUserByUsername(username);
        Page<Order> orders = orderRepository.findAllByUser(user, pageable);
        return orders.map(this::toOrderInfoResponse);
    }

    // search ordered items
    @Transactional(readOnly = true)
    public Page<OrderInfoResponse> findMyOrder(OrderSearch cond, String username, Pageable pageable
    ) {
        User user = validateUserByUsername(username);
        Page<Order> orders = orderCustomRepository.searchOrder(cond, user.getUsername(), pageable);
        return orders.map(this::toOrderInfoResponse);
    }

    // -------- private methods --------//

    // Order -> OrdrInfoResponse
    private OrderInfoResponse toOrderInfoResponse(Order order) {
        return OrderInfoResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderedDate())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .impUid(order.getImpUid())
                .build();
    }

    // Order -> OrderCreateResponse
    private OrderCreateResponse toOrderCreateResponse(Order order ) {
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderedDate())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    private Cart validateCart(User user) {
        return cartRepository.findCartByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private boolean hasPermission(User user, Order order) {
        if (!order.getUser().getUsername().equals(user.getUsername()) &&
                user.getUserRole() != UserRole.ADMIN) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        return true;
    }

    private Order validateOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private User validateUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
        return user;
    }
}
