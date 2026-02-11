package _4.NovemberRecipeMarket.service.order;

import _4.NovemberRecipeMarket.domain.dto.order.OrderCreateResponse;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.service.OrderService;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderPaymentFacade {

    private final OrderService orderService;

    @Retryable(
            retryFor = {
                    DeadlockLoserDataAccessException.class,
                    CannotAcquireLockException.class,
                    PessimisticLockingFailureException.class,
                    LockTimeoutException.class,
                    PessimisticLockException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, random = true)
    )
    public OrderCreateResponse finalizeOrderAfterPayment(Long orderId, String impUid,
                                                         int paidAmount, String username) {

        return orderService.finalizeOrderAfterPayment(orderId, impUid, paidAmount, username);
    }

    @Recover
    public OrderCreateResponse recover(RuntimeException e,
                                       Long orderId, String impUid, int paidAmount, String username) {
        throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "주문에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
}
