package menu.menuapi.repository;

import menu.menuapi.model.HealthRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthRestrictionRepository extends JpaRepository<HealthRestriction, Long> {
    HealthRestriction findByName(String restrictionName);
}
