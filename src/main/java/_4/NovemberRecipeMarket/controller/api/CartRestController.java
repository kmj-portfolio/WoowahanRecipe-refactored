package _4.NovemberRecipeMarket.controller.api;

import _4.NovemberRecipeMarket.domain.dto.Response;
import _4.NovemberRecipeMarket.domain.dto.cart.*;
import _4.NovemberRecipeMarket.domain.dto.cart.request.CartItemDeleteRequest;
import _4.NovemberRecipeMarket.domain.dto.cart.response.CartCreateResponse;
import _4.NovemberRecipeMarket.domain.dto.cart.response.CartDetailsResonse;
import _4.NovemberRecipeMarket.domain.dto.cart.response.CartItemDeleteResponse;
import _4.NovemberRecipeMarket.domain.dto.cart.response.CartItemResponse;
import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateRequest;
import _4.NovemberRecipeMarket.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CartRestController {

    private final CartService cartService;

    @PostMapping("/carts")
    public Response<CartCreateResponse> createCart(Authentication authentication) {
        CartCreateResponse cart = cartService.createCart(authentication.getName());
        return Response.success(cart);
    }

    // 카트 가져오기
    @GetMapping("/users/carts")
    public Response<CartDetailsResonse> getCartByUser(Authentication authentication, Pageable pageable) {
        CartDetailsResonse response = cartService.getCartDetailsByUser(authentication.getName(), pageable);
        return Response.success(response);
    }

    // 장바구니 삭제
    @DeleteMapping("/users/carts")
    public Response<String> deleteCart(Authentication authentication) {
        String delteCartResponse = cartService.deleteCart(authentication.getName());
        return Response.success(delteCartResponse);
    }

    // 상품 추가
    @PutMapping("/users/carts/items/one")
    public Response<CartItemResponse> addItemToCart(@RequestBody CartItemDto cartItemDto,
                                                    Authentication authentication) {
        CartItemResponse cartItemResponse = cartService.addOneItemToCart(cartItemDto, authentication.getName());
        return Response.success(cartItemResponse);
    }

    // 상품 여러 개 추가
    @PutMapping("/users/carts/items/multiple")
    public Response<List<CartItemDto>> addMultipleItemToCart(@RequestBody List<CartItemDto> cartItems,
                                                    Authentication authentication) {
        List<CartItemDto> cartItemResponseList = cartService.addMultipleItemsToCart(cartItems, authentication.getName());
        return Response.success(cartItemResponseList);
    }


    // 상품 제거
    @DeleteMapping("/users/carts/items")
    public Response<CartItemDeleteResponse> removeItemFromCart(@RequestBody CartItemDeleteRequest deleteRequest,
                                                               Authentication authentication) {
        CartItemDeleteResponse deleteResponse = cartService.removeFromCart(deleteRequest, authentication.getName());
        return Response.success(deleteResponse);
    }

    @PutMapping("/users/carts/items/update_quantity")
    public Response<CartItemResponse> updateQuantity(@RequestBody CartItemDto cartItemDto,
                                                     Authentication authentication) {
        CartItemResponse cartItemResponse = cartService.updateQuantity(cartItemDto, authentication.getName());
        return Response.success(cartItemResponse);
    }

    // 장바구니에서 주문 생성 (결제 전)
    @PostMapping("/orderss/from-cart")
    public Response<List<OrderCreateRequest>> orderSelectedItemsFromCart(@RequestBody CartItemOrderListDto cartItemDtoList,
                                                                   Authentication authentication, String imp_uid) {
        List<OrderCreateRequest> response = cartService.orderCartItems(cartItemDtoList, authentication.getName());
        return Response.success(response);
    }
}
