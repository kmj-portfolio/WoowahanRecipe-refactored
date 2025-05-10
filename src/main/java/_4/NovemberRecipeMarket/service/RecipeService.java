package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.domain.dto.recipe.*;
import _4.NovemberRecipeMarket.domain.dto.recipeItem.RecipeItemGetResponse;
import _4.NovemberRecipeMarket.domain.dto.recipeItem.RecipeItemRequest;
import _4.NovemberRecipeMarket.domain.dto.review.ReviewResponseForList;
import _4.NovemberRecipeMarket.domain.entity.*;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.ItemRepository;
import _4.NovemberRecipeMarket.repository.LikeRepository;
import _4.NovemberRecipeMarket.repository.RecipeRepository;
import _4.NovemberRecipeMarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final LikeRepository likeRepository;

    public RecipeGetResponse getRecipeById(Long recipeId) {
        Recipe recipe = validateRecipeById(recipeId);
        recipe.increaseViewCount();
        return toRecipeGetResponse(recipe);
    }

    @Transactional(readOnly = true)
    public Page<RecipeGetResponseForList> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable)
                .map(this::toRecipeGetResponseForList);
    }

    public RecipeCreateResponse createRecipe(String username, RecipeRequest recipeRequest) {
        User user = validateUserByUsername(username);

        Recipe recipe = new Recipe(user, recipeRequest.getTitle(), recipeRequest.getContent());
        recipeRepository.save(recipe);
        List<RecipeItem> recipeItemList = createRecipeItem(recipeRequest.getIngredients(), recipe);
        recipe.setIngredients(recipeItemList);
        return new RecipeCreateResponse(recipe.getId());
    }

    public RecipeUpdateResponse updateRecipe(Long recipeId, String username,
                                             RecipeRequest recipeRequest) {
        Recipe recipe = validateRecipeById(recipeId);
        hasPermission(username, recipe);
        List<RecipeItem> recipeItemList = createRecipeItem(recipeRequest.getIngredients(), recipe);
        recipe.updateRecipe(recipeRequest.getTitle(), recipeRequest.getContent(), recipeItemList);

        List<RecipeItemGetResponse> ingredientsDtoList = toRecipeItemDtoList(recipeItemList);
        return new RecipeUpdateResponse(recipe.getId(), recipe.getTitle(),
                recipe.getContent(), ingredientsDtoList);
    }

    public RecipeDeleteResponse deleteRecipe(Long recipeId, String username) {
        Recipe recipe = validateRecipeById(recipeId);
        String title = recipe.getTitle();
        hasPermission(username, recipe);
        recipeRepository.delete(recipe);
        return new RecipeDeleteResponse(recipeId, title);
    }

    @Transactional(readOnly = true)
    public Page<RecipeGetResponseForList> getAllRecipesByUser(Long userId, Pageable pageable) {
        User author = validateUserById(userId);
        return recipeRepository.findAllByAuthor(author,pageable)
                .map(this::toRecipeGetResponseForList);
    }

    // 좋아요
    public String pushLike(Long recipeId, String username) {
        User user = validateUserByUsername(username);
        Recipe recipe = validateRecipeById(recipeId);

        Optional<Like> optionalLike = likeRepository.findByRecipeAndUser(recipe, user);
        if (optionalLike.isEmpty()) {
            Like like = new Like(user, recipe);
            likeRepository.save(like);
            recipe.increaseLike();
            return "좋아요를 눌렀습니다.";
        }
        Long likeId = optionalLike.get().getId();
        likeRepository.delete(optionalLike.get());
        recipe.decreaseLike();
        return "좋아요를 취소했습니다.";
    }

    @Transactional(readOnly = true)
    public int getLikeCount(Long recipeId) {
        Recipe recipe = validateRecipeById(recipeId);
        return likeRepository.countByRecipe(recipe);
    }

    // List<RecipeItem> -> List<RecipeItemGetReponse>
    // Recipe와 Item의 무한 참조를 막는다
    private List<RecipeItemGetResponse> toRecipeItemDtoList(List<RecipeItem> recipeItemList) {
        return recipeItemList.stream()
                .map(this::toRecipeItemDto)
                .collect(Collectors.toList());
    }

    private RecipeItemGetResponse toRecipeItemDto(RecipeItem recipeItem) {
        return new RecipeItemGetResponse(recipeItem.getItem().getId(), recipeItem.getItem().getItemName(),
                recipeItem.getQuantity());
    }

    private List<RecipeItem> createRecipeItem(List<RecipeItemRequest> recipeItemRequestList,
                                                      Recipe recipe) {
        List<RecipeItem> recipeItemList = new ArrayList<>();
        for (RecipeItemRequest request : recipeItemRequestList) {
            Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
            RecipeItem recipeItem = new RecipeItem(item, recipe, request.getQuantity());
            recipeItemList.add(recipeItem);
        }
        return recipeItemList;
    }

    private RecipeGetResponseForList toRecipeGetResponseForList(Recipe recipe) {
        return RecipeGetResponseForList.builder()
                .recipeId(recipe.getId())
                .author(recipe.getAuthor().getUsername())
                .title(recipe.getTitle())
                .likeCount(recipe.getLikeCount())
                .viewCount(recipe.getViewCount())
                .createdDate(recipe.getCreatedDate())
                .lastModifiedDate(recipe.getLastModifiedDate())
                .build();
    }

    private RecipeGetResponse toRecipeGetResponse(Recipe recipe) {
        List<RecipeItemGetResponse> ingredientDtoList = toRecipeItemDtoList(recipe.getIngredients());
        List<ReviewResponseForList> reivewDtoList = toReviewDtoList(recipe.getReviews());

        return RecipeGetResponse.builder()
                .recipeId(recipe.getId())
                .author(recipe.getAuthor().getUsername())
                .title(recipe.getTitle())
                .content(recipe.getContent())
                .ingredients(ingredientDtoList)
                .reviews(reivewDtoList)
                .likeCount(recipe.getLikeCount())
                .viewCount(recipe.getViewCount())
                .createdDate(recipe.getCreatedDate())
                .lastModifiedDate(recipe.getLastModifiedDate())
                .build();
    }

    private List<ReviewResponseForList> toReviewDtoList(List<Review> reviews) {
        return reviews.stream().map(review -> new ReviewResponseForList(
                review.getId(),
                review.getAuthor().getUsername(),
                review.getTitle(),
                review.getContent(),
                review.getCreatedDate()))
                .collect(Collectors.toList());
    }

    private void hasPermission(String username, Recipe recipe) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        if (!recipe.getAuthor().getUsername().equals(username) && user.getUserRole() != UserRole.ADMIN) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    private User validateUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
    }

    private User validateUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
    }

    private Recipe validateRecipeById(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));
    }


}
