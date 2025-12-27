package _4.NovemberRecipeMarket.domain.dto.order;

import lombok.Getter;
import lombok.Setter;

// 결제 완료 시 프론트에서 서버로 보내줄 DTO

@Getter
@Setter
public class PaymentCompleteRequest {
    private Long orderId;
    private String impUid;
}
