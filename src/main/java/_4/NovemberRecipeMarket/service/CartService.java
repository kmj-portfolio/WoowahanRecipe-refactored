package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.cart.*;
import _4.NovemberRecipeMarket.domain.dto.cart.request.CartItemDeleteRequest;
import _4.NovemberRecipeMarket.domain.dto.cart.response.*;
import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateRequest;
import _4.NovemberRecipeMarket.domain.entity.*;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public CartCreateResponse createCart(String username) {
        User user = validateUserByUsername(username);
        Cart cart = new Cart(user);
        cartRepository.save(cart);
        return new CartCreateResponse(cart.getId());
    }

    // 장바구니 삭제
    public String deleteCart(String username) {
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user);
        cartRepository.delete(cart);
        return "장바구니를 삭제했습니다.";
    }

    public CartDetailsResonse getCartDetailsByUser(String username, Pageable pageable) {
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user);

        int totalCost = 0;
        int numberOfUniqueItems = cart.getItemList().size();

        // convert List<CartItem> -> List<CartItemsDto> : Wrap CartItem in DTO
        List<CartItemDto> cartItemDtoList = new ArrayList<>();
        for (CartItem cartItem : cart.getItemList()) {
            cartItemDtoList.add(new CartItemDto(cartItem.getItem().getId(), cartItem.getQuantity()));
            totalCost += cartItem.getQuantity() * cartItem.getItem().getPrice();
        }

        // calculate 배달비
        int deliveryFee = 0;
        if (totalCost < 40000) {
            deliveryFee = 3000;
        }

        return CartDetailsResonse.builder()
                .cartOrderList(cartItemDtoList)
                .deliveryCost(deliveryFee)
                .itemCost(totalCost)
                .totalCost(totalCost + deliveryFee)
                .numberOfUniqueItems(numberOfUniqueItems)
                .build();
    }

    // 상품 장바구니 등록
    public CartItemResponse addOneItemToCart(CartItemDto request, String username) {
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user); // cart 가 존재하는 지 확인. 없으면 새로 생성

        Item item = validateItem(request.getItemId());

        addToCart(cart, item, request.getQuantity());

        return new CartItemResponse(item.getId(), item.getItemName(), request.getQuantity(),
                "상품이 장바구니에 추가되었습니다.");
    }

    public List<CartItemDto> addMultipleItemsToCart(List<CartItemDto> cartItemDtoList, String username) {
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user); // cart 가 존재하는 지 확인. 없으면 새로 생성

        List<CartItemDto> cartItemDtos = new ArrayList<>();
        for (CartItemDto cartItemDto: cartItemDtoList) {
            Item item = validateItem(cartItemDto.getItemId());
            addToCart(cart, item, cartItemDto.getQuantity());
            new CartItemDto(item.getId(), cartItemDto.getQuantity());
            cartItemDtos.add(cartItemDto);
        }
        return cartItemDtos;
    }

    // 상품 삭제
    public CartItemDeleteResponse removeFromCart(CartItemDeleteRequest deleteRequest, String username) {
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user); // cart 가 존재하는 지 확인. 없으면 새로 생성
        Item item = validateItem(deleteRequest.getItemId());

        CartItem cartItem = hasCartItem(cart, item);
        cart.removeFromCart(cartItem);
        return new CartItemDeleteResponse(item.getItemName());
    }

    public CartItemResponse updateQuantity(CartItemDto request, String username) {
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user);
        Item item = validateItem(request.getItemId());
        CartItem cartItem = hasCartItem(cart, item);

        int newQuantity = request.getQuantity();
        checkIfEnoughStock(item, newQuantity);
        cartItem.updateQuantity(newQuantity);
        return new CartItemResponse(item.getId(), item.getItemName(), cartItem.getQuantity(),
                "수량을 변경했습니다.");
    }

    // clear cart
    public String clearCart(String username) {
        User user = validateUserByUsername(username);
        cartRepository.deleteByUser(user);
        return "카트내 모든 상품이 삭제되었습니다.";
    }

    // 카트에 있는 OrderCreateRequest로 변환하기
    @Transactional
    public List<OrderCreateRequest> orderCartItems(CartItemOrderListDto request, String username) {
        // 사용자 + 장바구니 검증하기
        User user = validateUserByUsername(username);
        Cart cart = validateCart(user);

        if (request.getCartItemDtoList() == null || request.getCartItemDtoList().isEmpty()) {
            throw new AppException(ErrorCode.SELECT_ORDER_ITEM, "주문할 상품이 없습니다.");
        }

        List<OrderCreateRequest> itemsToOrder = new ArrayList<>();

        for (CartItemDto dto : request.getCartItemDtoList()) {
            Long itemId = dto.getItemId();
            int orderQuantity = dto.getQuantity();

            if (orderQuantity <= 0) {
                throw new AppException(ErrorCode.INVALID_QUANTITY, "0보다 작은 수량은 주문할 수 없습니다.");
            }

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

            // 장바구니에 넣은 수량이 음수거나, 재고보다 많이 주문하려 하면 막기
            if (orderQuantity > cartItem.getItem().getStock()) {
                throw new AppException(ErrorCode.INVALID_QUANTITY, "재고 수량보다 많이 주문할 수 없습니다.");
            }

            itemsToOrder.add(new OrderCreateRequest(cartItem.getItem().getId(), cartItem.getQuantity()));
        }

        return itemsToOrder;
    }

    // ------ private methods -------//

    private void checkIfEnoughStock(Item item, int quantity) {
        if (item.getStock() < quantity) {
            throw new AppException(ErrorCode.NOT_ENOUGH_STOCK, "재고가 충분하지 않습니다.");
        }
    }

    private void addToCart(Cart cart, Item item, int quantityRequested) {
        // check enough stock
        checkIfEnoughStock(item, quantityRequested);

        // 카트에 넣으려는 상품이 이미 있는지 확인
        Optional<CartItem> optionalCartItem = cartItemRepository.findByCartAndItem(cart, item);

        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();

            //아이템 stock 충분한지 확인 (충분하지 않으면 error)
            int totalQuantity = cartItem.getQuantity() + quantityRequested;
            checkIfEnoughStock(item, totalQuantity);

            cartItem.updateQuantity(totalQuantity);

        } else {
            int cnt = quantityRequested;
            checkIfEnoughStock(item, cnt);
            CartItem cartItem = new CartItem(cart, item, cnt);
            cartItemRepository.save(cartItem);
            cart.addToCart(cartItem); // cart가 가지고 있는 cart item list에 추가
        }
    }


    private CartItem validateCartItem(Long cartIemId) {
        return cartItemRepository.findById(cartIemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private CartItem hasCartItem(Cart cart, Item item) {
        return cartItemRepository.findByCartAndItem(cart, item)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private Cart validateCart(User user) {
        return cartRepository.findCartByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private Item validateItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
    }

    private User validateUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
        return user;
    }
}
