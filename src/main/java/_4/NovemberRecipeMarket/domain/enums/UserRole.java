package _4.NovemberRecipeMarket.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER("ROLE_USER"),
    SELLER("ROLE_SELLER"),
    READY("ROLE_READY"),
    REJECT("ROLE_REJECT"),
    ADMIN("ROLE_ADMIN");

    private String value;
}
