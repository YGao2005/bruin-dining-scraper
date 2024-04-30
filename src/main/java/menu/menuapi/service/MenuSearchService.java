package menu.menuapi.service;

import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.DTO.MenuItemSearchDTO;
import menu.menuapi.DTO.RestaurantMenuFormatDTO;
import menu.menuapi.exception.MenuItemNotFoundException;
import menu.menuapi.model.*;
import menu.menuapi.repository.MealPeriodRepository;
import menu.menuapi.repository.MenuItemInfoRepository;
import menu.menuapi.repository.MenuItemRepository;
import menu.menuapi.repository.ThemeInfoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuSearchService {

    private final MenuItemRepository menuItemRepository;

    private final ThemeInfoRepository themeInfoRepository;

    private final MealPeriodRepository mealPeriodRepository;

    private final MenuItemInfoRepository menuItemInfoRepository;
    public MenuSearchService(MenuItemRepository menuItemRepository,
                             ThemeInfoRepository themeInfoRepository,
                             MealPeriodRepository mealPeriodRepository,
                             MenuItemInfoRepository menuItemInfoRepository) {
        this.menuItemRepository = menuItemRepository;
        this.themeInfoRepository = themeInfoRepository;
        this.mealPeriodRepository = mealPeriodRepository;
        this.menuItemInfoRepository = menuItemInfoRepository;
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

    public String getThemeFromDateAndMealPeriod(LocalDate date, String mealPeriodName) {
        ThemeInfo themeInfo = themeInfoRepository.findByDateAndMealPeriod(date, mealPeriodRepository.findByPeriodName(mealPeriodName));
        if (themeInfo != null) {
            return themeInfo.getTheme().getThemeName();
        } else {
            return null; // Theme not found for the given date and meal period
        }
    }

    public List<MenuItemDTO> getMenuItemById(Long menuItemId) {
        // Perform database query to retrieve menu item by ID
        List<MenuItemDTO> menuItemDTOs = new ArrayList<>();
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        List<MenuItemInfo> menuItemInfos = menuItemInfoRepository.findAllByMenuItem(menuItem);
        for (MenuItemInfo itemInfo : menuItemInfos) {
            menuItemDTOs.add(new MenuItemDTO(menuItem, itemInfo));
        }
        return menuItemDTOs;
    }

    public List<MenuItemDTO> getMenuItemByName(String menuItemName) {
        List<MenuItemDTO> menuItemDTOs = new ArrayList<>();
        MenuItem menuItem = menuItemRepository.findByItemName(menuItemName);
        List<MenuItemInfo> menuItemInfos = menuItemInfoRepository.findAllByMenuItem(menuItem);
        for (MenuItemInfo itemInfo : menuItemInfos) {
            menuItemDTOs.add(new MenuItemDTO(menuItem, itemInfo));
        }
        // Map MenuItem entity to DTO (Data Transfer Object) to return to the user
        return menuItemDTOs;
    }

    public List<RestaurantMenuFormatDTO> getMenuFormatsByDateAndMealPeriod(LocalDate date, String mealPeriodName) {
        List<RestaurantMenuFormatDTO> restaurantMenuFormatDTOs = new ArrayList<>();

        RestaurantMenuFormatDTO deNeveMenu = new RestaurantMenuFormatDTO("De Neve", mealPeriodName);
        RestaurantMenuFormatDTO bPlateMenu = new RestaurantMenuFormatDTO("Bruin Plate", mealPeriodName);
        RestaurantMenuFormatDTO epicuriaMenu = new RestaurantMenuFormatDTO("Epicuria", mealPeriodName);

        MealPeriod mealPeriod = mealPeriodRepository.findByPeriodName(mealPeriodName);
        List<MenuItemInfo> menuItems = menuItemInfoRepository.findAllByDateAndMealPeriod(date, mealPeriod);
        for (MenuItemInfo menuItemInfo : menuItems) {
            String restaurantName = menuItemInfo.getMenuItem().getRestaurant().getName();
            String sectionName = menuItemInfo.getMenuItem().getSection().getName();
            String menuItemName = menuItemInfo.getMenuItem().getItemName();
            List<String> healthRestrictions = menuItemInfo.getMenuItem().getHealthRestrictionNames();

            switch(restaurantName) {
                case "De Neve":
                    deNeveMenu.addMenuItem(sectionName, menuItemName, healthRestrictions);
                    break;
                case "Bruin Plate":
                    bPlateMenu.addMenuItem(sectionName, menuItemName, healthRestrictions);
                    break;
                case "Epicuria":
                    epicuriaMenu.addMenuItem(sectionName, menuItemName, healthRestrictions);
                    break;
            }
        }

        restaurantMenuFormatDTOs.add(deNeveMenu);
        restaurantMenuFormatDTOs.add(bPlateMenu);
        restaurantMenuFormatDTOs.add(epicuriaMenu);

        return restaurantMenuFormatDTOs;
    }
}
