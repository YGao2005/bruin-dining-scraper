package menu.menuapi.repository;

import menu.menuapi.model.MealPeriod;
import menu.menuapi.model.MenuItem;
import menu.menuapi.model.MenuItemInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MenuItemInfoRepository extends JpaRepository<MenuItemInfo, Long> {

    MenuItemInfo findByMenuItemAndMealPeriodAndDate(MenuItem menuItem, MealPeriod byPeriodName, LocalDate date);

    List<MenuItemInfo> findAllByMenuItem(MenuItem menuItem);

    List<MenuItemInfo> findAllByDateAndMealPeriod(LocalDate date, MealPeriod mealPeriod);

    List<MenuItemInfo> findAllByMenuItemAndDateBetween(MenuItem menuItem, LocalDate startDate, LocalDate endDate);
}
