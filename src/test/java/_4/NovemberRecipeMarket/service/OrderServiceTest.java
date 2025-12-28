package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateRequest;
import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateResponse;
import _4.NovemberRecipeMarket.domain.dto.order.OrderItemResponse;
import _4.NovemberRecipeMarket.domain.dto.order.OrderReadyResponse;
import _4.NovemberRecipeMarket.domain.entity.*;
import _4.NovemberRecipeMarket.domain.entity.Order;
import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.repository.*;
import _4.NovemberRecipeMarket.repository.order.OrderCustomRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock OrderRepository orderRepository;
    @Mock OrderCustomRepository orderCustomRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock UserRepository userRepository;
    @Mock ItemRepository itemRepository;
    @Mock OrderItemRepository orderItemRepository;

    private User user;
    private Seller seller;
    private Item item;

    private final String USERNAME = "username";

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .userRole(UserRole.USER)
                .username("username")
                .name("김땡땡")
                .email("user@example.com")
                .address("서울시 어딘가")
                .birthdate("1990-01-01")
                .password("pw")
                .phoneNumber("010-0000-0000")
                .build();

        seller = Seller.builder()
                .username("seller")
                .password("pw")
                .companyName("오리온")
                .businessRegNum("123-456-789")
                .phoneNumber("010-1122-0000")
                .address("서울시 어딘가")
                .email("seller@example.com")
                .build();

        item = new Item(seller, "초콜렛", 2000, 100);
    }

    @Nested
    @DisplayName("주문")
    class order {

        @Test
        @DisplayName("성공")
        void create_order_success(){
            OrderCreateRequest request = new OrderCreateRequest(item.getId(), 1);

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
            given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));

            OrderReadyResponse response = orderService.createOrder(USERNAME, List.of(request));

            assertThat(response.getTotalAmount()).isEqualTo(2000);
            assertThat(response.getOrderedItems()).hasSize(1);

            OrderItemResponse orderedItem = response.getOrderedItems().get(0);
            assertThat(orderedItem.getItemName()).isEqualTo("초콜렛");
            assertThat(orderedItem.getOrderQuantity()).isEqualTo(1);

            then(orderRepository).should().save(any(Order.class));
            then(orderItemRepository).should().save(any(OrderItem.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void create_order_throws_Item_Not_Found() {
            OrderCreateRequest request = new OrderCreateRequest(100L, 1);

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
            given(itemRepository.findById(100l)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(USERNAME, List.of(request)))
                    .isInstanceOf(AppException.class);

            then(itemRepository).should(never()).save(any(Item.class));
            then(orderItemRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("결제 후 주문 확정")
    class orderConfirmation {
        private String imp_uid = "IMP_UID_123";

        @Test
        @DisplayName("성공")
        void finalizeOrderAfterPayment_success(){
            OrderItem orderItem = new OrderItem(item, item.getPrice(), 1);

            Order order = Order.builder()
                    .id(1l)
                    .user(user)
                    .orderNumber("ORDER12")
                    .orderStatus(OrderStatus.WAITING_PAYMENT)
                    .orderedItemList(List.of(orderItem))
                    .totalPrice(1000)
                    .build();

            Cart cart = new Cart(user);
            CartItem cartItem = new CartItem(cart, item, 1);

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderRepository.existsByImpUid(imp_uid)).willReturn(false);
            given(itemRepository.findByIdWithLock(item.getId())).willReturn(Optional.of(item));
            given(cartRepository.findCartByUser(user)).willReturn(Optional.of(cart));
            given(cartItemRepository.findByCartAndItem(cart, item)).willReturn(Optional.of(cartItem));

            OrderCreateResponse response = orderService.finalizeOrderAfterPayment(1L, imp_uid, 1000, USERNAME);
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(response.getTotalPrice()).isEqualTo(1000);
            assertThat(order.getImpUid()).isEqualTo(imp_uid);
        }

        @Test
        @DisplayName("실패 - 이미 결제 완료 처리된 주문인데 imp_uid가 없거나 다름")
        void finalizeOrderAfterPayment_throws_invalid_payment(){
            OrderItem orderItem = new OrderItem(item, item.getPrice(), 1);

            Order order = Order.builder()
                    .id(1l)
                    .user(user)
                    .orderNumber("ORDER12")
                    .orderStatus(OrderStatus.PAID)
                    .orderedItemList(List.of(orderItem))
                    .impUid(imp_uid)
                    .totalPrice(1000)
                    .build();

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.finalizeOrderAfterPayment(1L, "WRONG_IMP_UID", 1000, USERNAME))
                    .isInstanceOf(AppException.class);

            then(orderRepository).should(never()).existsByImpUid("WRONG_IMP_UID");
            then(orderItemRepository).shouldHaveNoInteractions();
            then(cartRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패 - 중복 결제")
        void finalizeOrderAfterPayment_throws_duplicate_payment(){
            OrderItem orderItem = new OrderItem(item, item.getPrice(), 1);

            Order order = Order.builder()
                    .id(1l)
                    .user(user)
                    .orderNumber("ORDER12")
                    .orderStatus(OrderStatus.WAITING_PAYMENT)
                    .orderedItemList(List.of(orderItem))
                    .impUid(imp_uid)
                    .totalPrice(1000)
                    .build();

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderRepository.existsByImpUid(imp_uid)).willReturn(true);

            assertThatThrownBy(() -> orderService.finalizeOrderAfterPayment(1L, imp_uid, 1000, USERNAME))
                    .isInstanceOf(AppException.class);

            then(orderItemRepository).shouldHaveNoInteractions();
            then(cartRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패 - 결제 금액이 주문 금액과 일치하지 않음")
        void finalizeOrderAfterPayment_throws_mismatch_amount(){
            OrderItem orderItem = new OrderItem(item, item.getPrice(), 1);

            Order order = Order.builder()
                    .id(1l)
                    .user(user)
                    .orderNumber("ORDER12")
                    .orderStatus(OrderStatus.WAITING_PAYMENT)
                    .orderedItemList(List.of(orderItem))
                    .totalPrice(1000)
                    .build();

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderRepository.existsByImpUid(imp_uid)).willReturn(false);

            assertThatThrownBy(() -> orderService.finalizeOrderAfterPayment(1L, imp_uid, 0, USERNAME))
                    .isInstanceOf(AppException.class);

            then(orderItemRepository).shouldHaveNoInteractions();
            then(cartRepository).shouldHaveNoInteractions();
        }
    }

}