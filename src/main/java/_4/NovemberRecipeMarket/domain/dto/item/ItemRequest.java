package _4.NovemberRecipeMarket.domain.dto.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemRequest {
    @NotNull
    private String itemName;

    @Min(0)
    @NotNull
    private Integer price;

    @Min(0)
    @NotNull
    private Integer stock;
}
