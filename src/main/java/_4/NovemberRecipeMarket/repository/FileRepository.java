package _4.NovemberRecipeMarket.repository;

import _4.NovemberRecipeMarket.domain.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
