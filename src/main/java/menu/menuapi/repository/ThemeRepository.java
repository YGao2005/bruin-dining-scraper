package menu.menuapi.repository;

import menu.menuapi.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Theme findByThemeName(String theme);
}