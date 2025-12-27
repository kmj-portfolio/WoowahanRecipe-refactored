package _4.NovemberRecipeMarket.domain.entity;

import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    private String itemName;
    private int price;
    private int stock;
    private String imagePath;

    // TODO: set imagepath
    public Item(Seller seller, String itemName, int price, int stock) {
        this.seller = seller;
        this.itemName = itemName;
        this.price = price;
        this.stock = stock;
    }

    public void updateItem(String itemName, int price, int stock) {
        this.itemName = itemName;
        this.price = price;
        this.stock = stock;
    }

    public boolean isEnoughStock(int quantity) {
        return stock >= quantity;
    }

    public void removeStock(int quantity) {
        if (stock - quantity < 0) {
            throw new AppException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        stock += quantity;
    }
}
