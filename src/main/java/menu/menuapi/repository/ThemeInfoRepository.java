package menu.menuapi.repository;

import menu.menuapi.model.MealPeriod;
import menu.menuapi.model.ThemeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ThemeInfoRepository extends JpaRepository<ThemeInfo, Long> {

    ThemeInfo findByDateAndMealPeriod(LocalDate date, MealPeriod mealPeriod);
}