package _4.NovemberRecipeMarket.domain.dto.user;

import _4.NovemberRecipeMarket.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String username;
    private String name;

    private String birthdate;
    private String phoneNumber;

    private String email;
    private String address;
    private UserRole userRole;

}
