package menu.menuapi.repository;

import menu.menuapi.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
    Section findByName(String sectionName);
}