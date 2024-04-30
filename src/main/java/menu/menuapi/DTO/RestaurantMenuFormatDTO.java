package menu.menuapi.DTO;

import menu.menuapi.model.HealthRestriction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantMenuFormatDTO {
    private String restaurantName;

    private String mealPeriodName;
    private Map<String, List<Map<String, List<String>>>> menuSections = new HashMap<>();

    public RestaurantMenuFormatDTO(String restaurantName, String mealPeriodName) {
        this.restaurantName = restaurantName;
        this.mealPeriodName = mealPeriodName;
    }

    // Method to add a menu item to a specific section with its health restrictions
    public void addMenuItem(String section, String menuItem, List<String> healthRestrictions) {
        menuSections.putIfAbsent(section, new ArrayList<>());
        Map<String, List<String>> item = new HashMap<>();
        item.put(menuItem, healthRestrictions);
        menuSections.get(section).add(item);
    }

    // Getters and setters for all fields
    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public Map<String, List<Map<String, List<String>>>> getMenuSections() {
        return menuSections;
    }

    public void setMenuSections(Map<String, List<Map<String, List<String>>>> menuSections) {
        this.menuSections = menuSections;
    }

}
