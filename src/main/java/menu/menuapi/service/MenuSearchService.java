package menu.menuapi.service;

import menu.menuapi.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.model.MenuItem;
import menu.menuapi.model.MenuItemInfo;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuSearchService {

    private final MenuItemRepository menuItemRepository;

    public MenuSearchService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<MenuItemDTO> searchMenuItems(String query) {
        if (query.length() <= 2) {
            throw new IllegalArgumentException("Query length must be greater than 2 characters.");
        }

        List<MenuItem> menuItems = menuItemRepository.findAllByItemNameContainingIgnoreCase(query);
        List<MenuItemDTO> menuItemDTOs = new ArrayList<>();

        for (MenuItem menuItem : menuItems) {
            List<MenuItemInfo> menuItemInfos = menuItem.getMenuItemInfoList();
            if (menuItemInfos != null && !menuItemInfos.isEmpty()) {
                for (MenuItemInfo menuItemInfo : menuItemInfos) {
                    MenuItemDTO menuItemDTO = new MenuItemDTO(menuItem, menuItemInfo);
                    menuItemDTOs.add(menuItemDTO);
                }
            } else {
                MenuItemDTO menuItemDTO = new MenuItemDTO(menuItem);
                menuItemDTOs.add(menuItemDTO);
            }
        }

        return menuItemDTOs;
    }
}
