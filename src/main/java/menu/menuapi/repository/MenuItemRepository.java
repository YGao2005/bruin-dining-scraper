package menu.menuapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import menu.menuapi.model.MenuItem;
import menu.menuapi.model.Restaurant;

import java.time.LocalDate;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    MenuItem findByRestaurantAndItemName(Restaurant restaurant, String itemName);

    List<MenuItem> findAllByItemNameContainingIgnoreCase(String query);

    MenuItem findByItemName(String itemName);

    List<MenuItem> findAllById(Long id);

    List<MenuItem> findAllByItemNameAndRestaurantId(String itemName, Long restaurantId);

    MenuItem findByItemNameAndRestaurant_RestaurantName(String itemName, String restaurantName);
    List<MenuItem> findAllByItemNameAndRestaurant_RestaurantName(String itemName, String restaurantName);

    List<MenuItem> findAllByItemName(String menuItemName);
}
