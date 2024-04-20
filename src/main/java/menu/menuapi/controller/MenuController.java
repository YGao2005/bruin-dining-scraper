package menu.menuapi.controller;

import org.springframework.web.bind.annotation.*;
import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.service.MenuScrapingService;
import menu.menuapi.service.MenuSearchService;
import menu.menuapi.service.MenuService;

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
        menuScrapingService.scrapeMenuDataForUpcomingWeek("Breakfast");
        menuScrapingService.scrapeMenuDataForUpcomingWeek("Lunch");
        menuScrapingService.scrapeMenuDataForUpcomingWeek("Dinner");
    }

    // Add a new endpoint to get all menu item names
    @GetMapping("/items")
    public List<String> getAllMenuItemNames() {
        return menuService.getAllMenuItemNames();
    }

    @GetMapping("/search")
    public List<MenuItemDTO> searchMenu(@RequestParam String query) {
        List<MenuItemDTO> menuItems = menuSearchService.searchMenuItems(query);
        return menuItems;
    }
}
