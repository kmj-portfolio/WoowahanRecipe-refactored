package _4.NovemberRecipeMarket.repository;

import _4.NovemberRecipeMarket.domain.entity.Cart;
import _4.NovemberRecipeMarket.domain.entity.CartItem;
import _4.NovemberRecipeMarket.domain.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);
}
