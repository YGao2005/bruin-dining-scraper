package menu.menuapi.controller;

import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.DTO.RestaurantMenuFormatDTO;
import menu.menuapi.model.MenuItem;
import menu.menuapi.DTO.MenuItemSearchDTO;
import org.springframework.web.bind.annotation.*;
import menu.menuapi.service.MenuScrapingService;
import menu.menuapi.service.MenuSearchService;
import menu.menuapi.service.MenuService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuScrapingService menuScrapingService;

    private final MenuService menuService;

    private final MenuSearchService menuSearchService;

    public MenuController(MenuScrapingService menuScrapingService, MenuService menuService, MenuSearchService menuSearchService) {
        this.menuScrapingService = menuScrapingService;
        this.menuService = menuService;
        this.menuSearchService = menuSearchService;
    }

    // Add a new endpoint to scrape menu data
    @PostMapping("/scrape")
    public void scrapeMenuData() {
        menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Breakfast");
        menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Lunch");
        menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Dinner");
    }

    // Add a new endpoint to get all menu item names
    @GetMapping("/items")
    public List<String> getAllMenuItemNames() {
        return menuService.getAllMenuItemNames();
    }

    @GetMapping("/search")
    public List<MenuItemSearchDTO> searchMenu(@RequestParam String query) {
        List<MenuItemSearchDTO> menuItems = menuSearchService.searchMenuItems(query);
        return menuItems;
    }

    @GetMapping("/gettheme")
    public String getTheme(@RequestParam LocalDate date, @RequestParam String mealPeriod) {
        return menuSearchService.getThemeFromDateAndMealPeriod(date, mealPeriod);
    }

    @GetMapping("/getallitemsbyid")
    public List<MenuItemDTO> getMenuItemById(@RequestParam Long id) {
        return menuSearchService.getMenuItemById(id);
    }

    @GetMapping("/getallitemsbyname")
    public List<MenuItemDTO> getMenuItemByName(@RequestParam String name) {
        return menuSearchService.getMenuItemByName(name);
    }

    @GetMapping("/getmenuformatsbydateandmealperiod")
    public List<RestaurantMenuFormatDTO> getMenuFormatsByDate(@RequestParam LocalDate date, @RequestParam String mealPeriod) {
            return menuSearchService.getMenuFormatsByDateAndMealPeriod(date, mealPeriod);
    }
}

