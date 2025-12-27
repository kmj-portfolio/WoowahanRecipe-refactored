package _4.NovemberRecipeMarket.domain.dto.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemListResponse {
    private Long id;
    private String itemImagePath;
    private  String itemName;
    private Integer price;
    private Integer stock;

}
