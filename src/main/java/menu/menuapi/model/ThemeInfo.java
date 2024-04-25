package menu.menuapi.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ThemeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private MealPeriod mealPeriod;

    private LocalDate date;

    public ThemeInfo() {
    }

    public ThemeInfo(Theme theme, MealPeriod mealPeriod, LocalDate date) {
        this.theme = theme;
        this.mealPeriod = mealPeriod;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
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

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
