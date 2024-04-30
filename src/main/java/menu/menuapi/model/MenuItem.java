package menu.menuapi.model;


import jakarta.persistence.*;

import java.util.ArrayList;
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
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne
    private Restaurant restaurant;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL)
    private List<MenuItemInfo> menuItemInfoList;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_item_health_restrictions",
            joinColumns = @JoinColumn(name = "menu_item_id"),
            inverseJoinColumns = @JoinColumn(name = "health_restriction_id")
    )
    private List<HealthRestriction> healthRestrictions;

    public MenuItem() {
    }

    public MenuItem(String itemName, Restaurant restaurant, List<HealthRestriction> healthRestrictions) {
        this.itemName = itemName;
        this.restaurant = restaurant;
        this.healthRestrictions = healthRestrictions;
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

    public List<HealthRestriction> getHealthRestrictions() {
        return healthRestrictions;
    }

    public void setHealthRestrictions(List<HealthRestriction> healthRestrictions) {
        this.healthRestrictions = healthRestrictions;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<String> getHealthRestrictionNames() {
        List<String> healthRestrictionNames = new ArrayList<>();
        for(HealthRestriction healthRestriction : healthRestrictions) {
            healthRestrictionNames.add(healthRestriction.getName());
        }
        return healthRestrictionNames;
    }

}
