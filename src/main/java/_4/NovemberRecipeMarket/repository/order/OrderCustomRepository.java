package _4.NovemberRecipeMarket.repository.order;

import _4.NovemberRecipeMarket.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderCustomRepository {
    public Page<Order> searchOrder(OrderSearch orderSearch, String username, Pageable pageable);
}
