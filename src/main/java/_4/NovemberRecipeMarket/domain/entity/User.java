package _4.NovemberRecipeMarket.domain.entity;

import _4.NovemberRecipeMarket.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET deleted_date = current_timestamp WHERE user_id = ?")
@Where(clause = "deleted_date is NULL")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String username;
    private String password;
    private String name;

    private String birthdate;
    private String phoneNumber;
    @Column(unique = true)
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public void updateUser(String password, String name, String address,
                           String email,String phoneNumber, String birthdate) {
        this.password = password;
        this.name = name;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
    }

}
