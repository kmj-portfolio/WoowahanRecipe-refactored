package _4.NovemberRecipeMarket.service.order;

import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateResponse;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrderPaymentFacadeTest.TestConfig.class)
class OrderPaymentFacadeTest {

    @Configuration
    @EnableRetry
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {
        @Bean
        OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        OrderPaymentFacade orderPaymentFacade(OrderService orderService) {
            return new OrderPaymentFacade(orderService);
        }
    }

    @Autowired
    OrderPaymentFacade orderPaymentFacade;

    @Autowired
    OrderService orderService;

    private final long ORDER_ID = 1L;
    private final String IMP_UID = "imp_uid";
    private final int AMOUNT = 10000;
    private final String USERNAME = "username";

    @Test
    @DisplayName("락_예외_후_재시도_3번째에_성공")
    void retriesAndSucceedsOnThirdAttempt() {

        // 기대 값
        OrderCreateResponse expected = OrderCreateResponse.builder()
                .orderId(1L)
                .build();

        // 락 획득 실패 2번 후 expected 객체 정상적으로 반환
        when(orderService.finalizeOrderAfterPayment(anyLong(), anyString(), anyInt(), anyString()))
                .thenThrow(new CannotAcquireLockException("lock1"))
                .thenThrow(new CannotAcquireLockException("lock2"))
                .thenReturn(expected);

        OrderCreateResponse response = orderPaymentFacade.finalizeOrderAfterPayment(ORDER_ID, IMP_UID, AMOUNT, USERNAME);

        assertThat(response).isSameAs(expected);
        verify(orderService, times(3)).finalizeOrderAfterPayment(ORDER_ID, IMP_UID, AMOUNT, USERNAME);
    }

    @Test
    @DisplayName("락_예외가_계속나면_재시도_3번_후_recover")
    void throwRecoverExceptionAfterMaxAttempts() {
        when(orderService.finalizeOrderAfterPayment(anyLong(), anyString(), anyInt(), anyString()))
                .thenThrow(new CannotAcquireLockException("lock"));

        assertThatThrownBy(() -> orderPaymentFacade.finalizeOrderAfterPayment(ORDER_ID, IMP_UID, AMOUNT, USERNAME))
                .isInstanceOf(AppException.class);

        verify(orderService, times(3)).finalizeOrderAfterPayment(ORDER_ID, IMP_UID, AMOUNT, USERNAME);
    }

    @Test
    @DisplayName("비즈니스_예외는_재시도_안함")
    void doesNotRetryBusinessException() {
        when(orderService.finalizeOrderAfterPayment(anyLong(), anyString(), anyInt(), anyString()))
                .thenThrow(new AppException(ErrorCode.INVALID_PAYMENT));

        assertThatThrownBy(() ->  orderPaymentFacade.finalizeOrderAfterPayment(ORDER_ID, IMP_UID, AMOUNT, USERNAME))
                .isInstanceOf(AppException.class);

        verify(orderService, times(1)).finalizeOrderAfterPayment(ORDER_ID, IMP_UID, AMOUNT, USERNAME);
    }
}