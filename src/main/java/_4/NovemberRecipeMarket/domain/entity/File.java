package _4.NovemberRecipeMarket.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    private String fileName;

    private String storedUrl;

    public File(String fileName, String storedUrl) {
        this.fileName = fileName;
        this.storedUrl = storedUrl;
    }
}
