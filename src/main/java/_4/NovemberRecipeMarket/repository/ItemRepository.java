package _4.NovemberRecipeMarket.repository;

import _4.NovemberRecipeMarket.domain.entity.Item;
import _4.NovemberRecipeMarket.domain.entity.Seller;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findById(Long itemId);
    Page<Item> findAllBySeller(Seller seller, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :itemId")
    Optional<Item> findByIdWithLock(@Param("itemId") Long itemId);

    Page<Item> findByItemNameContaining(String keyword, Pageable pageable);

}
