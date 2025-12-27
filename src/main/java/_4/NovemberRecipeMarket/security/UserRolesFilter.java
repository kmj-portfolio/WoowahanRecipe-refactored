package _4.NovemberRecipeMarket.security;


import _4.NovemberRecipeMarket.domain.entity.Seller;
import _4.NovemberRecipeMarket.domain.entity.User;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.SellerRepository;
import _4.NovemberRecipeMarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserRolesFilter {
    private final UserRepository userRepository;

    private final SellerRepository sellerRepository;

    public String validateAndGetRole(String username, String role) {
        log.info("username:{}", username);
        log.info("role:{}", role);
        if (role.equals("ROLE_USER")) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

            return user.getUserRole().getValue();
        }
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
        return seller.getUserRole().getValue();
    }
}
