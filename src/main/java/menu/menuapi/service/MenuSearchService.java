package menu.menuapi.service;

import menu.menuapi.model.*;
import menu.menuapi.repository.MealPeriodRepository;
import menu.menuapi.repository.MenuItemRepository;
import menu.menuapi.repository.ThemeInfoRepository;
import org.springframework.stereotype.Service;
import menu.menuapi.DTO.MenuItemDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MenuSearchService {

    private final MenuItemRepository menuItemRepository;

    private final ThemeInfoRepository themeInfoRepository;

    private final MealPeriodRepository mealPeriodRepository;
    public MenuSearchService(MenuItemRepository menuItemRepository,
                             ThemeInfoRepository themeInfoRepository,
                             MealPeriodRepository mealPeriodRepository) {
        this.menuItemRepository = menuItemRepository;
        this.themeInfoRepository = themeInfoRepository;
        this.mealPeriodRepository = mealPeriodRepository;
    }

    public List<MenuItemSearchDTO> searchMenuItems(String query){
        if (query.length() <= 3) {
            throw new IllegalArgumentException("Query length must be greater than 3 characters.");
        }
        List<MenuItemSearchDTO> menuItemSearchDTOs = new ArrayList<>();
        List<MenuItem> menuItems = menuItemRepository.findAllByItemNameContainingIgnoreCase(query);
        for (MenuItem menuItem : menuItems) {
            menuItemSearchDTOs.add(new MenuItemSearchDTO(menuItem));
        }
        return menuItemSearchDTOs;
    }

    /*
    public List<MenuItemDTO> searchMenuItems(String query) {
        if (query.length() <= 3) {
            throw new IllegalArgumentException("Query length must be greater than 3 characters.");
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
*/
    public String getThemeFromDateAndMealPeriod(LocalDate date, String mealPeriodName) {
        ThemeInfo themeInfo = themeInfoRepository.findByDateAndMealPeriod(date, mealPeriodRepository.findByPeriodName(mealPeriodName));
        if (themeInfo != null) {
            return themeInfo.getTheme().getThemeName();
        } else {
            return null; // Theme not found for the given date and meal period
        }
    }
}
