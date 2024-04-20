package menu.menuapi.model;


import jakarta.persistence.*;
import java.util.List;

@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    // Nutritional information
    private String nutritionalLink;


    @ManyToOne
    private Restaurant restaurant;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL)
    private List<MenuItemInfo> menuItemInfoList;

    public MenuItem() {
    }

    public MenuItem(String itemName, Restaurant restaurant) {
        this.itemName = itemName;
        this.restaurant = restaurant;
    }

    // Getters and setters for itemName, mealPeriod, and restaurant

    public String getNutritionalLink() {
        return nutritionalLink;
    }

    public void setNutritionalLink(String nutritionalLink) {
        this.nutritionalLink = nutritionalLink;
    }

    // Getters and setters
    public String getItemName() {
        return itemName;
    }

    public Long getId() {
        return id;
    }


    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<MenuItemInfo> getMenuItemInfoList() {
        return menuItemInfoList;
    }
}
