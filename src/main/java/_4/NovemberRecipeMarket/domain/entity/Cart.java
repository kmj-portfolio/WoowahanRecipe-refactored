package _4.NovemberRecipeMarket.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> itemList = new ArrayList<>();

    public Cart(User user) {
        this.user = user;
    }
    public void addToCart(CartItem item) {
        itemList.add(item);
    }

    public void removeFromCart(CartItem item) {
        itemList.remove(item);
    }
}
