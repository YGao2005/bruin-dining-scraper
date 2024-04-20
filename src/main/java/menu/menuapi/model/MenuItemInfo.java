package menu.menuapi.model;

import jakarta.persistence.*;


import java.time.LocalDate;

@Entity
public class MenuItemInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MenuItem menuItem;

    @ManyToOne
    private MealPeriod mealPeriod;

    private LocalDate date;

    // Constructors, getters, and setters

    public MenuItemInfo() {
    }

    public MenuItemInfo(MenuItem menuItem, MealPeriod mealPeriod, LocalDate date) {
        this.menuItem = menuItem;
        this.mealPeriod = mealPeriod;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public MealPeriod getMealPeriod() {
        return mealPeriod;
    }

    public void setMealPeriod(MealPeriod mealPeriod) {
        this.mealPeriod = mealPeriod;
    }

    public LocalDate getDate() {
        return date;
    }
}
