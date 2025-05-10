package _4.NovemberRecipeMarket.domain.entity;

import _4.NovemberRecipeMarket.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE seller SET deleted_date = current_timestamp WHERE seller_id = ?")
@Where(clause = "deleted_date IS NULL")
public class Seller extends BaseEntity{

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long id;
    private String username;
    private String password;
    private String companyName;
    private String businessRegNum;
    private String phoneNumber;
    private String address;
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public Seller(String username, String password, String companyName, String businessRegNum, String phoneNumber,
                  String address, String email) {
        this.username = username;
        this.password = password;
        this.companyName = companyName;
        this.businessRegNum = businessRegNum;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
        this.userRole = UserRole.READY;
    }

    public void updateSellerInfo(String username, String companyName,
                                 String phoneNumber, String address, String email) {
        this.username = username;
        this.companyName = companyName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateStatus(String status) {
        if (status.equals("pass")) {
            userRole = UserRole.SELLER;
        }
        if (status.equals("reject")){
            userRole = UserRole.REJECT;
        }
    }
}
