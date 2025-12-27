package _4.NovemberRecipeMarket.controller.api;

import _4.NovemberRecipeMarket.domain.dto.Response;
import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateRequest;
import _4.NovemberRecipeMarket.domain.dto.order.OrderInfoResponse;
import _4.NovemberRecipeMarket.domain.dto.order.OrderReadyResponse;
import _4.NovemberRecipeMarket.repository.order.OrderSearch;
import _4.NovemberRecipeMarket.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderRestController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public Response<OrderInfoResponse> getOrderById(@PathVariable Long id, Authentication authentication) {

        OrderInfoResponse orderInfoResponse = orderService.getOrderById(id, authentication.getName());
        return Response.success(orderInfoResponse);
    }

    @GetMapping("/search")
    public Response<Page<OrderInfoResponse>> searchOrder(OrderSearch orderSearch, Authentication authentication,
                                                         Pageable pageable) {
        Page<OrderInfoResponse> orderInfoResponses = orderService.findMyOrder(orderSearch, authentication.getName(), pageable);
        return Response.success(orderInfoResponses);

    }

    // 주문 시작 (OrderEntity 생성)
    @PostMapping("/ready")
    public Response<OrderReadyResponse> createOrder(@RequestBody List<OrderCreateRequest> itemReqList,
                                                    Authentication authentication) {
        String username = authentication.getName();
        OrderReadyResponse response = orderService.createOrder(username, itemReqList);
        return Response.success(response);
    }

}
