package _4.NovemberRecipeMarket.controller.api;

import _4.NovemberRecipeMarket.domain.dto.Response;
import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateResponse;
import _4.NovemberRecipeMarket.domain.dto.order.PaymentCompleteRequest;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.service.OrderService;
import _4.NovemberRecipeMarket.service.IamportClient;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentRestController {

    private final IamportClient iamportClient;
    private final OrderService orderService;


    // 결제 후 주문 확정 및 재고 차감
    @PostMapping("/payment/complete")
    public Response<OrderCreateResponse> paymentComplete(Authentication authentication,
                                                         @RequestBody PaymentCompleteRequest request) throws IOException {
        String username = authentication.getName();

        // 1. IMP_SECRET과 IMP_KEY로 아잎포트에서 토큰 발급
        String token = iamportClient.getToken();

        // 2. PG (아임포트)에서 실제 결제 금액 조회
        int paidAmount = iamportClient.paymentInfo(request.getImpUid(), token);

        try {

            // 결제 검증 + 주문 확정
            OrderCreateResponse response = orderService.finalizeOrderAfterPayment(
                    request.getOrderId(), request.getImpUid(), paidAmount, username);
            return Response.success(response);

        } catch (AppException e) {
            // 재고 부족, 금액 불일치 등으로 에러 발생 시 결제 취소
            iamportClient.cancelPayment(token, request.getImpUid(), paidAmount, "주문 확정 실패(재고/금액 검증 실패)");
            throw e;
        }
    }
}
