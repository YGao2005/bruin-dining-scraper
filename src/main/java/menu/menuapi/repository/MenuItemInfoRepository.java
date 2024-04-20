package menu.menuapi.repository;

import menu.menuapi.model.MenuItemInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemInfoRepository extends JpaRepository<MenuItemInfo, Long> {

}
