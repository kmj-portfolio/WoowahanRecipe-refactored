package _4.NovemberRecipeMarket.repository;

import _4.NovemberRecipeMarket.domain.entity.Order;
import _4.NovemberRecipeMarket.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUser(User user, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByImpUid(String impUid);
}
