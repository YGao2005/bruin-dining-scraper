package menu.menuapi.DTO;

import menu.menuapi.model.MenuItem;
import menu.menuapi.model.MenuItemInfo;

import java.time.LocalDate;

public class MenuItemDTO {
    private Long id;
    private String itemName;
    private String nutritionalLink;
    private String mealPeriodName;
    private String restaurantName;

    private LocalDate date;

    public MenuItemDTO() {
    }

    public MenuItemDTO(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.itemName = menuItem.getItemName();
        this.nutritionalLink = menuItem.getNutritionalLink();
        this.restaurantName = menuItem.getRestaurant().getName();
        // Set default or null values for mealPeriodName, restaurantName, and date
        this.mealPeriodName = null;
        this.date = null;
    }


    public MenuItemDTO(MenuItem menuItem, MenuItemInfo menuItemInfo) {
        this.id = menuItem.getId();
        this.itemName = menuItem.getItemName();
        this.nutritionalLink = menuItem.getNutritionalLink();
        this.restaurantName = menuItem.getRestaurant().getName();

        this.mealPeriodName = menuItemInfo.getMealPeriod().getName();
        this.date = menuItemInfo.getDate();
    }

    // Getters and setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getNutritionalLink() {
        return nutritionalLink;
    }

    public void setNutritionalLink(String nutritionalLink) {
        this.nutritionalLink = nutritionalLink;
    }

    public String getMealPeriodName() {
        return mealPeriodName;
    }

    public void setMealPeriodName(String mealPeriodName) {
        this.mealPeriodName = mealPeriodName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
}
