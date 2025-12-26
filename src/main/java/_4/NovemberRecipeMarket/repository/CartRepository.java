package _4.NovemberRecipeMarket.repository;

import _4.NovemberRecipeMarket.domain.entity.Cart;
import _4.NovemberRecipeMarket.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findCartByUser(User user);
    void deleteByUser(User user);
}
