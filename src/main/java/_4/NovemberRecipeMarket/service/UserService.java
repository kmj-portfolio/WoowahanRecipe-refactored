package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.domain.dto.user.*;
import _4.NovemberRecipeMarket.domain.entity.User;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.UserRepository;
import _4.NovemberRecipeMarket.security.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String secretKey;

    private long expiredTimeMs = 24 * 60 * 60 * 1000; // 토큰 유효시간 24시간

    // create user
    @Transactional
    public UserJoinResponse join(UserJoinRequest userJoinRequest) {

        userRepository.findByUsername(userJoinRequest.getUsername())
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATE_USERNAME);
                });

        userRepository.findByEmail(userJoinRequest.getEmail())
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATE_EMAIL);
                });

        User user = User.builder()
                .name(userJoinRequest.getName())
                .username(userJoinRequest.getUsername())
                .password(encoder.encode(userJoinRequest.getPassword()))
                .address(userJoinRequest.getAddress())
                .email(userJoinRequest.getEmail())
                .phoneNumber(userJoinRequest.getPhoneNumber())
                .birthdate(userJoinRequest.getBirthdate())
                .userRole(UserRole.USER)
                .build();

        userRepository.save(user);

        return new UserJoinResponse(user.getUsername(),
                String.format("%s님의 회원가입이 완료되었습니다.", user.getName()));
    }

    // login
    public String login(String username, String password) {

        User user = validateUserByUsername(username);

        if (!encoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return JwtTokenUtils.createToken(username,user.getUserRole().getValue(), secretKey, expiredTimeMs);
    }

    // update user
    @Transactional
    public UserResponse updateUser(UserUpdateRequest updateRequest, Long id, String username) {

        User user = validateUserById(id);
        validateUserByUsername(username);

        // email로 찾은 유저가 로그인 한 유저면 에외 발생시키지 않도록 하기
        userRepository.findByEmail(updateRequest.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(duplicateUser -> {
                    throw new AppException(ErrorCode.DUPLICATE_EMAIL);
                });


        if (!user.getUsername().equals(username) && user.getUserRole() != UserRole.ADMIN) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        user.updateUser(encoder.encode(updateRequest.getPassword()),
                updateRequest.getName(), updateRequest.getAddress(), updateRequest.getEmail(),
                updateRequest.getPhoneNumber(), updateRequest.getBirthDate());

        return toUserResponse(user);
    }

    // soft delete user
    @Transactional
    public UserDeleteResponse deleteUser(Long id, String username) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 본인이거나 ADMIN이면 삭제 가능
        if (!user.getUsername().equals(username) || user.getUserRole().equals(UserRole.ADMIN)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        return new UserDeleteResponse(id);
    }

    // 회원 마이페이지
    public UserResponse findMyPage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return toUserResponse(user);
    }

    public UserResponse getUserByUsername(String usernme) {
        User user =  validateUserByUsername(usernme);
        return toUserResponse(user);
    }

    // username이 존재하는지 확인
    private User validateUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
        return user;
    }

    // user id가 존재하는지 확인
    private User validateUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user;
    }

    // User -> UserResponse
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .birthdate(user.getBirthdate())
                .build();
    }
}
