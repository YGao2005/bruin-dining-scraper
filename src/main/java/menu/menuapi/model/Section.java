package menu.menuapi.model;

import jakarta.persistence.*;

@Entity
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Section(String sectionName) {
        this.name = sectionName;
    }

    public Section() {

    }

    // Constructor, getters, and setters
    public String getName() {
        return name;
    }
}
