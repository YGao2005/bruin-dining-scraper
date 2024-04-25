package menu.menuapi.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class HealthRestriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "healthRestrictions")
    private List<MenuItem> menuItems;

    // Constructors, getters, and setters

    public HealthRestriction() {
    }

    public HealthRestriction(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
