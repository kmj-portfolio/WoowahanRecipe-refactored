package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.domain.dto.seller.*;
import _4.NovemberRecipeMarket.domain.entity.Seller;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.SellerRepository;
import _4.NovemberRecipeMarket.security.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {
    private final SellerRepository sellerRepository;

    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String secretKey;

    private final long expiredTimeMs = 24 * 60 * 60 * 1000;

    public SellerJoinResponse join(SellerJoinRequest request) {
        if (sellerRepository.existsByBusinessRegNum(request.getBusinessRegNum())) {
            throw new AppException(ErrorCode.DUPLICATE_BUSINESS_REG_NUM);
        }
        if (sellerRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.DUPLICATE_USERNAME);
        }

        String encodedPassword = encoder.encode(request.getPassword());

        Seller seller = new Seller(request.getUsername(), encodedPassword, request.getCompanyName(),
                request.getBusinessRegNum(), request.getPhoneNumber(), request.getAddress(), request.getEmail());

        sellerRepository.save(seller);
        return new SellerJoinResponse(seller.getId(), seller.getCompanyName());
    }

    public SellerLoginResponse login(SellerLoginRequest request) {
        Seller seller = validateUsername(request.getUsername());

        if (!encoder.matches(request.getPassword(), seller.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        String token = JwtTokenUtils.createToken(seller.getUsername(), seller.getUserRole().getValue(),
                secretKey, expiredTimeMs);
        return new SellerLoginResponse(token);
    }

    public SellerDeleteResponse delete(Long id, String username) {
        Seller seller = checkForPermission(id, username);
        sellerRepository.delete(seller);
        return new SellerDeleteResponse(id);
    }

    // cannot update businessRegNum or password
    public SellerResponse  update(Long id, String username, SellerUpdateRequest request) {
        Seller seller = checkForPermission(id, username);

        // 바꾸려는 이메일과 같은 이메일이 DB에 없는지 확인
        sellerRepository.findByUsername(request.getEmail())
                .filter(existingSeller -> !existingSeller.getId().equals(seller.getId()))
                .ifPresent(duplicateEmail -> {
                    throw new AppException(ErrorCode.DUPLICATE_EMAIL);
                });
        checkIfUsernameIsUnique(seller, request.getUsername());

        seller.updateSellerInfo(request.getUsername(), request.getCompanyName(),
                request.getPhoneNumber(), request.getAddress(), request.getEmail());

        return toSellerResponse(seller);
    }

    public String updatePassword(Long id, String username, String currentPassword, String newPassword) {
        Seller seller = checkForPermission(id, username);
        if (!encoder.matches(currentPassword, seller.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        seller.changePassword(encoder.encode(newPassword));
        return "비밀번호를 성공적으로 바꾸었습니다.";
    }

    public SellerResponse getMyPage(Long id, String username) {
        Seller seller = checkForPermission(id, username);

        return toSellerResponse(seller);
    }

    public SellerResponse getSellerByUsername(String username) {
        Seller seller = validateUsername(username);
        return toSellerResponse(seller);
    }

    private SellerResponse toSellerResponse(Seller seller) {
        return new SellerResponse(seller.getId(), seller.getUsername(), seller.getCompanyName(),
                seller.getBusinessRegNum(), seller.getPhoneNumber(), seller.getAddress(),
                seller.getEmail(), seller.getUserRole().getValue());
    }

    private void checkIfUsernameIsUnique(Seller seller, String newUsername) {
        sellerRepository.findByUsername(newUsername)
                .filter(existingUser -> !existingUser.getId().equals(seller.getId()))
                .ifPresent((duplicateUsername -> {
                    throw new AppException(ErrorCode.DUPLICATE_USERNAME);
                }));
    }

    private Seller checkForPermission(Long id, String username) {
        Seller seller = validateUsername(username);
        if (seller.getId() != id &&
                !seller.getUserRole().equals(UserRole.ADMIN)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        return seller;
    }

    private Seller validateUsername(String username) {
        return sellerRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

}
