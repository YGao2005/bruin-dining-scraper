package menu.menuapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import menu.menuapi.model.MealPeriod;

public interface MealPeriodRepository extends JpaRepository<MealPeriod, Long> {
    MealPeriod findByPeriodName(String mealPeriodName);
}
