package menu.menuapi.service;

import menu.menuapi.model.*;
import menu.menuapi.repository.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import menu.menuapi.DTO.MenuItemDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MenuScrapingService {

    private final MealPeriodRepository mealPeriodRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    private final MenuItemInfoRepository menuItemInfoRepository;

    private final HealthRestrictionRepository healthRestrictionRepository;

    private final SectionRepository sectionRepository;

    private final ThemeRepository themeRepository;

    private final ThemeInfoRepository themeInfoRepository;

    public MenuScrapingService(MealPeriodRepository mealPeriodRepository,
                               MenuItemRepository menuItemRepository,
                               RestaurantRepository restaurantRepository,
                               MenuItemInfoRepository menuItemInfoRepository,
                               HealthRestrictionRepository healthRestrictionRepository,
                               SectionRepository sectionRepository,
                               ThemeRepository themeRepository,
                               ThemeInfoRepository themeInfoRepository) {
        this.mealPeriodRepository = mealPeriodRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemInfoRepository = menuItemInfoRepository;
        this.healthRestrictionRepository = healthRestrictionRepository;
        this.sectionRepository = sectionRepository;
        this.themeRepository = themeRepository;
        this.themeInfoRepository = themeInfoRepository;
    }

    public void scrapeMenuDataForUpcomingWeek(String menuPeriodName) {
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.plusDays(6);

        // Use parallel stream to scrape data for multiple days concurrently
        List<LocalDate> dates = new ArrayList<>();
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        // Leverage parallelism
        dates.parallelStream().forEach(date -> scrapeAndSaveMenuData(menuPeriodName, date));
    }

    private Map<String, MealPeriod> mealPeriodCache = new ConcurrentHashMap<>();
    private Map<String, Restaurant> restaurantCache = new ConcurrentHashMap<>();
    private Map<String, Section> sectionCache = new ConcurrentHashMap<>();

    public void scrapeAndSaveMenuData(String menuPeriodName, LocalDate date) {
        try {
            // Construct URL based on current date and menu period name
            String url = buildMenuUrl(date, menuPeriodName);

            // Connect to the website and fetch the HTML content
            Document doc = Jsoup.connect(url).get();

            // Extract menu periods
            MealPeriod existingMealPeriod = mealPeriodRepository.findByPeriodName(menuPeriodName);
            MealPeriod mealPeriod;
            if (existingMealPeriod != null) {
                mealPeriod = existingMealPeriod; // Use existing meal period
            } else {
                mealPeriod = new MealPeriod(menuPeriodName); // Create new meal period
            }

            mealPeriodRepository.save(mealPeriod);

            List<MenuItem> menuItemsToSave = new ArrayList<>();
            List<MenuItemInfo> menuItemInfosToSave = new ArrayList<>();

            // Extract restaurants and menu items
            Elements restaurantElements = doc.select("h3.col-header");
            for (Element restaurantElement : restaurantElements) {
                String restaurantName = restaurantElement.text().trim();

                // Check if the restaurant already exists in the database
                Restaurant restaurant = restaurantCache.computeIfAbsent(restaurantName, k -> {
                    Restaurant newRestaurant = new Restaurant(k);
                    restaurantRepository.save(newRestaurant);
                    return newRestaurant;
                });

                Elements sectionElements = restaurantElement.parent().select("li.sect-item");
                for(Element sectionElement : sectionElements) {
                    String sectionName = sectionElement.ownText().trim();
                    // Retrieve from cache or create a new instance
                    Section section = sectionCache.computeIfAbsent(sectionName, k -> {
                        Section newSection = new Section(k);
                        sectionRepository.save(newSection);
                        return newSection;
                    });
                    // Select all a.recipelink elements within the same parent container as the restaurant element
                    Elements menuItemElements = sectionElement.select("span.tooltip-target-wrapper");
                    for (Element menuItemElement : menuItemElements) {
                        Element anchorElement = menuItemElement.selectFirst("a.recipelink");
                        String itemName = anchorElement.text().trim();
                        String nutritionalLink = anchorElement.attr("href").trim();

                        // Extract health restrictions
                        Elements healthRestrictionElements = menuItemElement.select("img.webcode-16px");
                        List<String> healthRestrictions = new ArrayList<>();
                        for (Element healthRestrictionElement : healthRestrictionElements) {
                            String healthRestriction = healthRestrictionElement.attr("alt").trim();
                            healthRestrictions.add(healthRestriction);
                        }

                        // Create MenuItemDTO with the extracted data
                        MenuItemDTO menuItemDTO = new MenuItemDTO();
                        menuItemDTO.setItemName(itemName);
                        menuItemDTO.setNutritionalLink(nutritionalLink);
                        menuItemDTO.setMealPeriodName(mealPeriod.getName());
                        menuItemDTO.setRestaurantName(restaurantName);
                        menuItemDTO.setDate(date);
                        menuItemDTO.setHealthRestrictions(buildListOfHealthRestrictions(healthRestrictions));
                        menuItemDTO.setSectionName(sectionName);

                        // Save MenuItemDTO
                        saveMenuItemDTO(menuItemDTO, healthRestrictions);
                        if (shouldSkipMenuItem(menuItemDTO)) {
                            continue;
                        }

                        MenuItem menuItem = menuItemRepository.findByItemName(menuItemDTO.getItemName());
                        if (menuItem == null) {
                            menuItem = new MenuItem(menuItemDTO.getItemName(), restaurant);
                            menuItem.setNutritionalLink(menuItemDTO.getNutritionalLink());
                            menuItem.setHealthRestrictions(buildListOfHealthRestrictions(healthRestrictions));
                            menuItem.setSection(section);
                            menuItemsToSave.add(menuItem);
                        }

                        MenuItemInfo menuItemInfo = new MenuItemInfo(menuItem, mealPeriod, menuItemDTO.getDate());
                        menuItemInfosToSave.add(menuItemInfo);
                    }
                }
            }
            System.out.println("Menu data scraped and saved successfully for " + date);

            // Extract menu theme
            Elements themeElements = doc.select("li.sect-item:contains(Theme of the Day)");
            for (Element themeElement : themeElements) {
                Element themeLinkElement = themeElement.selectFirst("a.recipelink");
                if (themeLinkElement != null) {
                    String themeName = themeLinkElement.text().trim();
                    System.out.println("Theme of the day: " + themeName);

                    Theme menuTheme = themeRepository.findByThemeName(themeName);
                    if (menuTheme == null) {
                        menuTheme = new Theme(themeName); // Create new theme
                        themeRepository.save(menuTheme);
                    }

                    ThemeInfo themeInfo = new ThemeInfo(menuTheme, mealPeriod, date);
                    themeInfoRepository.save(themeInfo);
                }
            }

            menuItemRepository.saveAll(menuItemsToSave);
            menuItemInfoRepository.saveAll(menuItemInfosToSave);
            menuItemRepository.flush();
            menuItemInfoRepository.flush();

        } catch (IOException e) {
            System.err.println("Error scraping menu data for " + menuPeriodName + " " + date + ": " + e.getMessage());
        }
    }

    private boolean shouldSkipMenuItem(MenuItemDTO menuItemDTO) {
        // Check if the MenuItem already exists in the database
        MenuItem menuItem = menuItemRepository.findByItemName(menuItemDTO.getItemName());
        if (menuItem != null) {
            return true; // Skip if the menu item exists
        }
        return false;
    }

    private void saveMenuItemDTO(MenuItemDTO menuItemDTO, List<String> healthRestrictions) {
        // Check if the MenuItem already exists in the database
        MenuItem menuItem = menuItemRepository.findByItemName(menuItemDTO.getItemName());
        if (menuItem == null) {
            // If not, create a new MenuItem
            menuItem = new MenuItem(menuItemDTO.getItemName(), restaurantRepository.findByRestaurantName(menuItemDTO.getRestaurantName()));
            menuItem.setNutritionalLink(menuItemDTO.getNutritionalLink());
            menuItem.setHealthRestrictions(buildListOfHealthRestrictions(healthRestrictions));
            menuItem.setSection(sectionRepository.findByName(menuItemDTO.getSectionName()));
            // Save MenuItem
            menuItemRepository.save(menuItem);
        }

        MenuItemInfo existingMenuItemInfo = menuItemInfoRepository.findByMenuItemAndMealPeriodAndDate(
                menuItem,
                mealPeriodRepository.findByPeriodName(menuItemDTO.getMealPeriodName()),
                menuItemDTO.getDate()
        );

        if (existingMenuItemInfo == null) {
            // If not, create a new MenuItemInfo
            MenuItemInfo menuItemInfo = new MenuItemInfo(menuItem, mealPeriodRepository.findByPeriodName(menuItemDTO.getMealPeriodName()), menuItemDTO.getDate());
            // Save MenuItemInfo
            menuItemInfoRepository.save(menuItemInfo);
        }
    }


    private String buildMenuUrl(LocalDate date, String menuPeriodName) {
        // Format the date in the required format for the URL
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Construct and return the URL with the formatted date and menu period name
        return "https://menu.dining.ucla.edu/Menus/" + formattedDate + "/" + menuPeriodName;
    }

    private List<HealthRestriction> buildListOfHealthRestrictions(List<String> healthRestrictionNames){
        List<HealthRestriction> restrictions = new ArrayList<>();
        for (String restrictionName : healthRestrictionNames) {
            // Check if the health restriction already exists in the database
            HealthRestriction existingRestriction = healthRestrictionRepository.findByName(restrictionName);
            if (existingRestriction != null) {
                restrictions.add(existingRestriction); // If exists, add the existing restriction
            } else {
                // If not, create a new health restriction
                HealthRestriction newRestriction = new HealthRestriction(restrictionName);
                healthRestrictionRepository.save(newRestriction); // Save the new restriction
                restrictions.add(newRestriction); // Add the new restriction to the list
            }
        }
        return restrictions;
    }
}
