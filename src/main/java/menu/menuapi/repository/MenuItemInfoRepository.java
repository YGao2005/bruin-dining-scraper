package menu.menuapi.repository;

import menu.menuapi.model.MealPeriod;
import menu.menuapi.model.MenuItem;
import menu.menuapi.model.MenuItemInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MenuItemInfoRepository extends JpaRepository<MenuItemInfo, Long> {

    MenuItemInfo findByMenuItemAndMealPeriodAndDate(MenuItem menuItem, MealPeriod byPeriodName, LocalDate date);
}
