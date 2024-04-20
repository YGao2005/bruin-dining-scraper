package menu.menuapi.model;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class MealPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String periodName;

    @OneToMany(mappedBy = "mealPeriod", cascade = CascadeType.ALL)
    private List<MenuItemInfo> menuItemInfoList;

    public MealPeriod() {
    }

    public MealPeriod(String mealPeriodName) {
        this.periodName = mealPeriodName;
    }

    public String getName() {
        return periodName;
    }

    // Getters and setters
}
