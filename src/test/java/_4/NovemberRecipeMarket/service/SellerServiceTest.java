package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.seller.*;
import _4.NovemberRecipeMarket.domain.entity.Seller;
import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.SellerRepository;
import _4.NovemberRecipeMarket.security.JwtTokenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private SellerService sellerService;

    @Mock
    private Seller mockSeller;

    @Value("${jwt.token.secret}")
    String secret;

    private final String USERNAME = "seller01";
    private final String PASSWORD = "rawPassword";
    private final String ENCODED_PASSWORD = "encodedPassword";
    private final String COMPANY_NAME = "testCompany";
    private final String BUSINESS_REG_NUM = "1234567890";
    private final String EMAIL = "seller@example.com";
    private final String ADDRESS = "address";
    private final String PHONE_NUMBER = "000-0000-0000";


    @Test
    @DisplayName("회원가입 성공")
    void join_success() {
        // given
        SellerJoinRequest request = new SellerJoinRequest(USERNAME, PASSWORD, COMPANY_NAME,
                BUSINESS_REG_NUM, PHONE_NUMBER, ADDRESS, EMAIL);

        // when
        when(sellerRepository.existsByBusinessRegNum(BUSINESS_REG_NUM)).thenReturn(false);
        when(sellerRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(encoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        SellerJoinResponse response = sellerService.join(request);

        verify(sellerRepository).save(any(Seller.class));
        // then
        assertEquals(COMPANY_NAME, response.getCompanyName());
    }

    @Test
    @DisplayName("회원가입 실패 - 사업자 등록 번호 중복")
    void join_fail_duplicate_businessRegNum() {
        // given
        SellerJoinRequest request = new SellerJoinRequest(USERNAME, PASSWORD, COMPANY_NAME,
                BUSINESS_REG_NUM, PHONE_NUMBER, ADDRESS, EMAIL);

        // when
        when(sellerRepository.existsByBusinessRegNum(BUSINESS_REG_NUM)).thenReturn(true);

        // expect
        AppException ex = assertThrows(AppException.class, () -> sellerService.join(request));
        assertEquals(ErrorCode.DUPLICATE_BUSINESS_REG_NUM, ex.getErrorCode());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 username")
    void join_fail_duplicate_username() {
        // given
        SellerJoinRequest request = new SellerJoinRequest(USERNAME, PASSWORD, COMPANY_NAME,
                BUSINESS_REG_NUM, PHONE_NUMBER, ADDRESS, EMAIL);

        // when
        when(sellerRepository.existsByBusinessRegNum(BUSINESS_REG_NUM)).thenReturn(false);
        when(sellerRepository.existsByUsername(USERNAME)).thenReturn(true);

        // expect
        AppException ex = assertThrows(AppException.class, () -> sellerService.join(request));
        assertEquals(ErrorCode.DUPLICATE_USERNAME, ex.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void login_fail_invalidPassword() {
        // given
        SellerLoginRequest request = new SellerLoginRequest(USERNAME, "wrong");

        // when
        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockSeller));
        when(mockSeller.getPassword()).thenReturn(ENCODED_PASSWORD);
        when(encoder.matches("wrong", ENCODED_PASSWORD)).thenReturn(false);

        // expect
        AppException ex = assertThrows(AppException.class, () -> sellerService.login(request));
        assertEquals(ErrorCode.INVALID_PASSWORD, ex.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - username 없음")
    void login_fail_userNotFound() {
        // given
        SellerLoginRequest request = new SellerLoginRequest(USERNAME, PASSWORD);

        // when
        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // expect
        AppException ex = assertThrows(AppException.class, () -> sellerService.login(request));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void delete_success() {
        Long id = 1L;

        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockSeller));
        when(mockSeller.getId()).thenReturn(id);

        sellerService.delete(id, USERNAME);
        verify(sellerRepository).delete(mockSeller);
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 인가 없음")
    void delete_fail_invalidPermission() {

        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockSeller));
        when(mockSeller.getId()).thenReturn(1L);
        when(mockSeller.getUserRole()).thenReturn(UserRole.SELLER); // Not ADMIN

        // then
        // Not matching ID
        AppException ex = assertThrows(AppException.class, () -> sellerService.delete(2L, USERNAME));
        assertEquals(ErrorCode.INVALID_PERMISSION, ex.getErrorCode());
    }

    @Test
    @DisplayName("회원정보 수정 성공")
    void update_success() {

        Seller seller = Seller.builder()
                .id(1L)
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .companyName(COMPANY_NAME)
                .phoneNumber(PHONE_NUMBER)
                .address(ADDRESS)
                .email(EMAIL)
                .userRole(UserRole.SELLER)
                .build();

        SellerUpdateRequest request = new SellerUpdateRequest("changedUsername", COMPANY_NAME, PHONE_NUMBER, ADDRESS, "newEmail@example.com");

        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(seller));

        // 변경하려는 email이나 username이 중복 안됨
        when(sellerRepository.findByUsername("changedUsername")).thenReturn(Optional.empty());
        when(sellerRepository.findByEmail("newEmail@example.com")).thenReturn(Optional.empty());

        SellerResponse response = sellerService.update(1L, USERNAME, request);

        Assertions.assertEquals("changedUsername", response.getUsername());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 인가 없음")
    void update_fail_invalidPermission() {
        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockSeller));
        when(mockSeller.getId()).thenReturn(2L); // Not matching ID
        when(mockSeller.getUserRole()).thenReturn(UserRole.SELLER);

        SellerUpdateRequest request = new SellerUpdateRequest(USERNAME, COMPANY_NAME, PHONE_NUMBER, ADDRESS, EMAIL);

        AppException ex = assertThrows(AppException.class, () -> sellerService.update(1L, USERNAME, request));
        assertEquals(ErrorCode.INVALID_PERMISSION, ex.getErrorCode());
    }
}

