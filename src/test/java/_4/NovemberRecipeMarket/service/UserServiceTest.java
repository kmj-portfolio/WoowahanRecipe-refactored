package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.user.UserJoinRequest;
import _4.NovemberRecipeMarket.domain.dto.user.UserJoinResponse;
import _4.NovemberRecipeMarket.domain.entity.User;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private User mockUser;

    private final String EMAIL = "email@example.com";
    private final String NAME = "name";
    private final String PASSWORD = "password";
    private final String USERNAME = "username";
    private final String BIRTHDATE = "1990-01-01";
    private final String ADDRESS = "address";
    private final String PHONE_NUMBER = "010-0000-0000";


    @Test
    void join_success() {
        // given
        UserJoinRequest request = new UserJoinRequest(
                USERNAME, PASSWORD, NAME, BIRTHDATE, PHONE_NUMBER, EMAIL, ADDRESS);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(encoder.encode(PASSWORD)).thenReturn("encodedPassword");

        // when
        UserJoinResponse response = userService.join(request);

        assertEquals(USERNAME, response.getUsername());
        assertEquals("name님의 회원가입이 완료되었습니다.", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void join_fail_duplicate_username() {
        // given
        UserJoinRequest request = new UserJoinRequest(
                USERNAME, PASSWORD, NAME, BIRTHDATE, PHONE_NUMBER, EMAIL, ADDRESS);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockUser));

        // expect
        AppException ex = assertThrows(AppException.class, () -> userService.join(request));
        assertEquals(ErrorCode.DUPLICATE_USERNAME, ex.getErrorCode());
    }

    @Test
    void join_fail_duplicate_email() {
        // given
        UserJoinRequest request = new UserJoinRequest(
                USERNAME, PASSWORD, NAME, BIRTHDATE, PHONE_NUMBER, EMAIL, ADDRESS);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));

        // expect
        AppException ex = assertThrows(AppException.class, () -> userService.join(request));
        assertEquals(ErrorCode.DUPLICATE_EMAIL, ex.getErrorCode());
    }

    @Test
    void login_invalidPassword() {

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(mockUser.getPassword()).thenReturn("hashedPassword");
        when(encoder.matches("wrong", "hashedPassword")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> userService.login("user", "wrong"));
        assertEquals(ErrorCode.INVALID_PASSWORD, ex.getErrorCode());
    }

}