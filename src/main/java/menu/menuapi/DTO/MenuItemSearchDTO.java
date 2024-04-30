package menu.menuapi.DTO;

import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.model.HealthRestriction;
import menu.menuapi.model.MenuItem;
import menu.menuapi.repository.HealthRestrictionRepository;

import java.util.ArrayList;
import java.util.List;

public class MenuItemSearchDTO {

    private String menuItemName;
    private String restaurantName;

    private long menuItemId;

    private List<String> healthRestrictions = new ArrayList<>();
    public MenuItemSearchDTO(MenuItem menuItem) {
        this.menuItemName = menuItem.getItemName();
        this.restaurantName = menuItem.getRestaurant().getName();
        this.menuItemId = menuItem.getId();
        for(HealthRestriction healthRestriction : menuItem.getHealthRestrictions()) {
            this.healthRestrictions.add(healthRestriction.getName());
        }
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<String> getHealthRestrictions() {
        return healthRestrictions;
    }

    public void setHealthRestrictions(List<String> healthRestrictions) {
        this.healthRestrictions = healthRestrictions;
    }

    public long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(long menuItemId) {
        this.menuItemId = menuItemId;
    }
}
