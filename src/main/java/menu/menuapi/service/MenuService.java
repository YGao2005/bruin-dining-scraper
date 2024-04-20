package menu.menuapi.service;

import menu.menuapi.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import menu.menuapi.model.MenuItem;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<String> getAllMenuItemNames() {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        return menuItems.stream()
                .map(MenuItem::getItemName)
                .collect(Collectors.toList());
    }
}
