package _4.NovemberRecipeMarket.domain.entity;

import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeItem> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe")
    private List<Review> reviews = new ArrayList<>();

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int viewCount;

    private int likeCount;

    public Recipe(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
        viewCount = 0;
        likeCount = 0;
    }

    public void updateRecipe(String title, String content, List<RecipeItem> ingredients) {
        this.title = title;
        this.content = content;
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }

    public void decreaseLike() {
        likeCount--;
    }

    public void increaseLike() {
        likeCount++;
    }

    public void increaseViewCount() {
        viewCount++;
    }

    // for creating recipe
    public void setIngredients(List<RecipeItem> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(RecipeItem recipeItem) {
        if (!ingredients.contains(recipeItem)) {
            ingredients.add(recipeItem);
            return;
        }
        // TODO INGREDIENT의 갯수 설정
    }

    public void removeIngredient(RecipeItem recipeItem) {
        if (ingredients.contains(recipeItem)) {
            ingredients.remove(recipeItem);
            return;
        }
        throw new AppException(ErrorCode.RECIPE_ITEM_DOES_NOT_EXIST);
    }
}
