package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.review.ReviewDeleteResponse;
import _4.NovemberRecipeMarket.domain.dto.review.ReviewRequest;
import _4.NovemberRecipeMarket.domain.dto.review.ReviewResponse;
import _4.NovemberRecipeMarket.domain.entity.Recipe;
import _4.NovemberRecipeMarket.domain.entity.Review;
import _4.NovemberRecipeMarket.domain.entity.User;
import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.RecipeRepository;
import _4.NovemberRecipeMarket.repository.ReviewRepository;
import _4.NovemberRecipeMarket.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RecipeRepository recipeRepository;

    @InjectMocks
    ReviewService reviewService;

    private User user;
    private User anotherUser;
    private Recipe recipe;
    private Review review;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("BaekJongWon")
                .userRole(UserRole.USER)
                .build();

        anotherUser = User.builder()
                .id(2L)
                .username("GordonRamsay")
                .userRole(UserRole.USER)
                .build();

        recipe = Recipe.builder()
                .id(1L)
                .author(user)
                .title("유부초밥")
                .content("이렇게")
                .build();

        review = Review.builder()
                .id(1L)
                .recipe(recipe)
                .author(user)
                .title("너무 맛있어용")
                .content("정말 맛있네요")
                .build();
    }

    @Nested
    @DisplayName("리뷰 등록")
    class CreateReview {

        @Test
        @DisplayName("리뷰 등록 성공")
        void createReviewSuccess() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);

            assertDoesNotThrow(() -> reviewService.createReview(
                    recipe.getId(), user.getUsername(), new ReviewRequest("제목", "내용")));
        }

        @Test
        @DisplayName("리뷰 등록 실패 - 로그인 하지 않은 경우")
        void createReviewFailWhenUserMissing() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));

            AppException exception = assertThrows(AppException.class, () -> reviewService.createReview(
                    recipe.getId(), user.getUsername(), new ReviewRequest("제목", "내용")));
            assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("리뷰 수정")
    class UpdateReview {
        @Test
        @DisplayName("리뷰 수정 성공")
        void updateReviewSuccess() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            ReviewRequest request = new ReviewRequest("댓글 수정", review.getContent());
            ReviewResponse reviewResponse = reviewService.updateReview(review.getId(), user.getUsername(), request);
            assertThat(reviewResponse.getMessage()).isEqualTo("댓글이 수정되었습니다.");
            assertThat(review.getTitle()).isEqualTo("댓글 수정");
        }

        @Test
        @DisplayName("리뷰 수정 실패 - 작성자와 유저가 일치하지 않는 경우")
        void updateReviewFailWhenNotAuthor() {
            when(userRepository.findByUsername(anotherUser.getUsername())).thenReturn(Optional.of(anotherUser));
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            ReviewRequest request = new ReviewRequest("댓글 수정", review.getContent());
            AppException exception = assertThrows(AppException.class, () -> reviewService.updateReview(review.getId(), anotherUser.getUsername(), request));
            assertEquals(ErrorCode.INVALID_PERMISSION, exception.getErrorCode());
        }

        @Test
        @DisplayName("리뷰 수정 실패 - 리뷰가 없는 경우")
        void updateReviewFailWhenReviewMissing() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.empty());

            ReviewRequest request = new ReviewRequest("댓글 수정", review.getContent());
            AppException exception = assertThrows(AppException.class, () -> reviewService.updateReview(review.getId(), user.getUsername(), request));
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {
        @Test
        @DisplayName("리뷰 삭제 성공")
        void deleteReviewSuccess() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            ReviewDeleteResponse response = reviewService.deleteReview(review.getId(), user.getUsername());
            assertThat(response.getReviewId()).isEqualTo(review.getId());
        }

        @Test
        @DisplayName("리뷰 삭제 실패 - 해당 유저가 존재하지 않음")
        void deleteReviewFailWhenUserMissing() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> reviewService.deleteReview(review.getId(), user.getUsername()));
            assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("리뷰 삭제 실패 - 리뷰가 존재하지 않음")
        void deleteReviewFailWhenReviewMissing() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> reviewService.deleteReview(review.getId(), user.getUsername()));
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("리뷰 삭제 실패 - 작성자와 유저가 일치하지 않는 경우")
        void deleteReviewFailWhenNotAuthor() {
            when(userRepository.findByUsername(anotherUser.getUsername())).thenReturn(Optional.of(anotherUser));
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            AppException exception = assertThrows(AppException.class, () -> reviewService.deleteReview(review.getId(), anotherUser.getUsername()));
            assertEquals(ErrorCode.INVALID_PERMISSION, exception.getErrorCode());
        }
    }

}
