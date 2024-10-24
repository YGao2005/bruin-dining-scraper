package menu.menuapi.service;

import menu.menuapi.model.*;
import menu.menuapi.repository.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import menu.menuapi.DTO.MenuItemDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    private final WebsiteHealthMonitor healthMonitor;

    private static final Logger logger = LoggerFactory.getLogger(MenuScrapingService.class);

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 500;

    public MenuScrapingService(MealPeriodRepository mealPeriodRepository,
                               MenuItemRepository menuItemRepository,
                               RestaurantRepository restaurantRepository,
                               MenuItemInfoRepository menuItemInfoRepository,
                               HealthRestrictionRepository healthRestrictionRepository,
                               SectionRepository sectionRepository,
                               ThemeRepository themeRepository,
                               ThemeInfoRepository themeInfoRepository,
                               WebsiteHealthMonitor healthMonitor) {
        this.mealPeriodRepository = mealPeriodRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemInfoRepository = menuItemInfoRepository;
        this.healthRestrictionRepository = healthRestrictionRepository;
        this.sectionRepository = sectionRepository;
        this.themeRepository = themeRepository;
        this.themeInfoRepository = themeInfoRepository;
        this.healthMonitor = healthMonitor;
    }

    @Async
    public CompletableFuture<Void> scrapeMenuDataForUpcomingWeekAsync(String menuPeriodName) {
        logger.info("Starting weekly menu scraping for period: {}", menuPeriodName);
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.plusDays(6);

        while (!currentDate.isAfter(endDate)) {
            try {
                scrapeAndSaveMenuData(menuPeriodName, currentDate);
            } catch (Exception e) {
                logger.error("Failed to scrape menu for date {} and period {}: {}",
                        currentDate, menuPeriodName, e.getMessage());
            }
            currentDate = currentDate.plusDays(1);
        }

        logger.info("Completed weekly menu scraping for period: {}", menuPeriodName);
        return CompletableFuture.completedFuture(null);
    }

    public void scrapeAndSaveMenuData(String menuPeriodName, LocalDate date) {
        String url = buildMenuUrl(date, menuPeriodName);
        logger.info("Starting scraping for URL: {}", url);

        try {
            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .get();

            // Log the basic structure to help identify changes
            logger.debug("Page structure overview:");
            logger.debug("Restaurant headers found: {}", doc.select("h3.col-header").size());
            logger.debug("Menu sections found: {}", doc.select("li.sect-item").size());
            logger.debug("Menu items found: {}", doc.select("a.recipelink").size());

            // Extract menu periods
            MealPeriod mealPeriod = processMealPeriod(menuPeriodName);

            // Process restaurants
            Elements restaurantElements = doc.select("h3.col-header");
            if (restaurantElements.isEmpty()) {
                logger.warn("No restaurants found on page. HTML structure might have changed.");
                logger.debug("Current page HTML: {}", doc.html());
                return;
            }

            processRestaurants(restaurantElements, mealPeriod, date);

            // Process themes
            processThemes(doc, mealPeriod, date);

            logger.info("Successfully scraped menu data for {} on {}", menuPeriodName, date);

        } catch (IOException e) {
            logger.error("Failed to connect to or parse URL: {} - Error: {}", url, e.getMessage());
            logger.debug("Stack trace:", e);
        } catch (Exception e) {
            logger.error("Unexpected error during scraping: {}", e.getMessage());
            logger.debug("Stack trace:", e);
        }
    }

    private MealPeriod processMealPeriod(String menuPeriodName) {
        logger.debug("Processing meal period: {}", menuPeriodName);
        MealPeriod existingMealPeriod = mealPeriodRepository.findByPeriodName(menuPeriodName);
        if (existingMealPeriod != null) {
            return existingMealPeriod;
        }
        MealPeriod newPeriod = new MealPeriod(menuPeriodName);
        mealPeriodRepository.save(newPeriod);
        return newPeriod;
    }

    private void processRestaurants(Elements restaurantElements, MealPeriod mealPeriod, LocalDate date) {
        for (Element restaurantElement : restaurantElements) {
            String restaurantName = restaurantElement.text().trim();
            logger.debug("Processing restaurant: {}", restaurantName);

            Restaurant restaurant = processRestaurant(restaurantName);

            Elements sectionElements = restaurantElement.parent().select("li.sect-item");
            logger.debug("Found {} sections for restaurant {}", sectionElements.size(), restaurantName);

            for (Element sectionElement : sectionElements) {
                if (!sectionElement.text().contains("Theme of the Day")) {
                    processSection(sectionElement, restaurant, mealPeriod, date);
                }
            }
        }
    }

    private void processSection(Element sectionElement, Restaurant restaurant, MealPeriod mealPeriod, LocalDate date) {
        String sectionName = sectionElement.ownText().trim();
        logger.debug("Processing section: {}", sectionName);

        Section section = processOrCreateSection(sectionName);

        Elements menuItemElements = sectionElement.select("span.tooltip-target-wrapper");
        logger.debug("Found {} menu items in section {}", menuItemElements.size(), sectionName);

        for (Element menuItemElement : menuItemElements) {
            try {
                processMenuItem(menuItemElement, restaurant, section, mealPeriod, date);
            } catch (Exception e) {
                logger.error("Error processing menu item in section {}: {}", sectionName, e.getMessage());
            }
        }
    }

    private void processMenuItem(Element menuItemElement, Restaurant restaurant, Section section,
                                 MealPeriod mealPeriod, LocalDate date) {
        Element anchorElement = menuItemElement.selectFirst("a.recipelink");
        if (anchorElement == null) {
            logger.warn("No recipe link found for menu item");
            return;
        }

        String itemName = anchorElement.text().trim();
        logger.debug("Processing menu item: {}", itemName);

        MenuItemDTO menuItemDTO = createMenuItemDTO(menuItemElement, restaurant, section, mealPeriod, date);
        saveMenuItemDTO(menuItemDTO, extractHealthRestrictions(menuItemElement));
    }

    private Restaurant processRestaurant(String restaurantName) {
        logger.debug("Processing restaurant: {}", restaurantName);
        try {
            Restaurant existingRestaurant = restaurantRepository.findByRestaurantName(restaurantName);
            if (existingRestaurant != null) {
                logger.debug("Found existing restaurant: {}", restaurantName);
                return existingRestaurant;
            }

            Restaurant newRestaurant = new Restaurant(restaurantName);
            restaurantRepository.save(newRestaurant);
            logger.info("Created new restaurant: {}", restaurantName);
            return newRestaurant;
        } catch (Exception e) {
            logger.error("Error processing restaurant {}: {}", restaurantName, e.getMessage());
            throw e;
        }
    }

    private Section processOrCreateSection(String sectionName) {
        logger.debug("Processing section: {}", sectionName);
        try {
            Section existingSection = sectionRepository.findByName(sectionName);
            if (existingSection != null) {
                logger.debug("Found existing section: {}", sectionName);
                return existingSection;
            }

            Section newSection = new Section(sectionName);
            sectionRepository.save(newSection);
            logger.info("Created new section: {}", sectionName);
            return newSection;
        } catch (Exception e) {
            logger.error("Error processing section {}: {}", sectionName, e.getMessage());
            throw e;
        }
    }

    private void processThemes(Document doc, MealPeriod mealPeriod, LocalDate date) {
        logger.debug("Processing themes for date: {} and meal period: {}", date, mealPeriod.getName());
        try {
            Elements themeElements = doc.select("li.sect-item:contains(Theme of the Day)");
            logger.debug("Found {} theme elements", themeElements.size());

            for (Element themeElement : themeElements) {
                Element themeLinkElement = themeElement.selectFirst("a.recipelink");
                if (themeLinkElement != null) {
                    String themeName = themeLinkElement.text().trim();
                    logger.debug("Processing theme: {}", themeName);

                    Theme menuTheme = themeRepository.findByThemeName(themeName);
                    if (menuTheme == null) {
                        menuTheme = new Theme(themeName);
                        themeRepository.save(menuTheme);
                        logger.info("Created new theme: {}", themeName);
                    }

                    ThemeInfo themeInfo = themeInfoRepository.findByDateAndMealPeriod(date, mealPeriod);
                    if (themeInfo == null) {
                        themeInfo = new ThemeInfo(menuTheme, mealPeriod, date);
                        themeInfoRepository.save(themeInfo);
                        logger.info("Created new theme info for theme: {} on date: {}", themeName, date);
                    }
                } else {
                    logger.warn("Theme link element not found in theme section");
                }
            }
        } catch (Exception e) {
            logger.error("Error processing themes: {}", e.getMessage());
            logger.debug("Stack trace:", e);
        }
    }

    private MenuItemDTO createMenuItemDTO(Element menuItemElement, Restaurant restaurant,
                                          Section section, MealPeriod mealPeriod, LocalDate date) {
        logger.debug("Creating MenuItemDTO for restaurant: {} and section: {}",
                restaurant.getName(), section.getName());
        try {
            Element anchorElement = menuItemElement.selectFirst("a.recipelink");
            if (anchorElement == null) {
                throw new IllegalArgumentException("No recipe link found in menu item element");
            }

            String itemName = anchorElement.text().trim();
            String nutritionalLink = anchorElement.attr("href").trim();

            MenuItemDTO menuItemDTO = new MenuItemDTO();
            menuItemDTO.setItemName(itemName);
            menuItemDTO.setNutritionalLink(nutritionalLink);
            menuItemDTO.setMealPeriodName(mealPeriod.getName());
            menuItemDTO.setRestaurantName(restaurant.getName());
            menuItemDTO.setDate(date);
            menuItemDTO.setSectionName(section.getName());

            // Extract and set health restrictions
            List<String> healthRestrictions = extractHealthRestrictions(menuItemElement);
            menuItemDTO.setHealthRestrictions(buildListOfHealthRestrictions(healthRestrictions));

            logger.debug("Created MenuItemDTO for item: {}", itemName);
            return menuItemDTO;
        } catch (Exception e) {
            logger.error("Error creating MenuItemDTO: {}", e.getMessage());
            throw e;
        }
    }

    private List<String> extractHealthRestrictions(Element menuItemElement) {
        logger.debug("Extracting health restrictions from menu item");
        List<String> healthRestrictions = new ArrayList<>();
        try {
            Elements healthRestrictionElements = menuItemElement.select("img.webcode-16px");
            logger.debug("Found {} health restriction elements", healthRestrictionElements.size());

            for (Element healthRestrictionElement : healthRestrictionElements) {
                String healthRestriction = healthRestrictionElement.attr("alt").trim();
                if (!healthRestriction.isEmpty()) {
                    healthRestrictions.add(healthRestriction);
                    logger.debug("Found health restriction: {}", healthRestriction);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting health restrictions: {}", e.getMessage());
        }
        return healthRestrictions;
    }

    public void saveMenuItemDTO(MenuItemDTO menuItemDTO, List<String> healthRestrictions) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                saveMenuItemDTOInternal(menuItemDTO, healthRestrictions);
                return; // Success, exit the method
            } catch (Exception e) {
                retryCount++;
                if (retryCount == MAX_RETRIES) {
                    logger.error("Final attempt failed saving MenuItemDTO {}: {}",
                            menuItemDTO.getItemName(), e.getMessage());
                    throw e;
                }
                logger.warn("Attempt {} failed saving MenuItemDTO {}: {}. Retrying in {} ms...",
                        retryCount, menuItemDTO.getItemName(), e.getMessage(), RETRY_DELAY_MS);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry delay", ie);
                }
            }
        }
    }

    private void saveMenuItemDTOInternal(MenuItemDTO menuItemDTO, List<String> healthRestrictions) {
        logger.debug("Attempting to save MenuItemDTO: {} for restaurant: {}",
                menuItemDTO.getItemName(), menuItemDTO.getRestaurantName());

        Restaurant restaurant = restaurantRepository.findByRestaurantName(menuItemDTO.getRestaurantName());
        if (restaurant == null) {
            throw new IllegalStateException("Restaurant not found: " + menuItemDTO.getRestaurantName());
        }

        // Find items with the same name in the same restaurant
        List<MenuItem> existingItems = menuItemRepository
                .findAllByItemNameAndRestaurantId(menuItemDTO.getItemName(), restaurant.getId());

        MenuItem menuItem;
        if (existingItems.isEmpty()) {
            // If no existing items in this restaurant, create new one
            logger.debug("Creating new menu item: {} for restaurant: {}",
                    menuItemDTO.getItemName(), restaurant.getName());

            menuItem = new MenuItem(
                    menuItemDTO.getItemName(),
                    restaurant,
                    buildListOfHealthRestrictions(healthRestrictions)
            );
            menuItem.setNutritionalLink(menuItemDTO.getNutritionalLink());
            menuItem.setSection(sectionRepository.findByName(menuItemDTO.getSectionName()));
            menuItemRepository.save(menuItem);
        } else {
            // Use the first item if multiple exist in the same restaurant
            menuItem = existingItems.get(0);

            // If we have duplicates within the same restaurant, clean them up
            if (existingItems.size() > 1) {
                logger.info("Found {} duplicate menu items for {} in restaurant {}. Using first instance and cleaning up.",
                        existingItems.size(), menuItemDTO.getItemName(), restaurant.getName());
                cleanupDuplicateMenuItems(existingItems, menuItem);
            }

            // Update the existing item's information if needed
            boolean needsUpdate = false;

            if (!menuItem.getNutritionalLink().equals(menuItemDTO.getNutritionalLink())) {
                menuItem.setNutritionalLink(menuItemDTO.getNutritionalLink());
                needsUpdate = true;
            }

            Section newSection = sectionRepository.findByName(menuItemDTO.getSectionName());
            if (!menuItem.getSection().equals(newSection)) {
                menuItem.setSection(newSection);
                needsUpdate = true;
            }

            // Update health restrictions if they've changed
            List<HealthRestriction> newRestrictions = buildListOfHealthRestrictions(healthRestrictions);
            if (!menuItem.getHealthRestrictions().equals(newRestrictions)) {
                menuItem.setHealthRestrictions(newRestrictions);
                needsUpdate = true;
            }

            if (needsUpdate) {
                menuItemRepository.save(menuItem);
                logger.debug("Updated existing menu item: {} in restaurant {}",
                        menuItem.getItemName(), restaurant.getName());
            }
        }

        // Check for existing MenuItemInfo
        MenuItemInfo existingMenuItemInfo = menuItemInfoRepository
                .findByMenuItemAndMealPeriodAndDate(
                        menuItem,
                        mealPeriodRepository.findByPeriodName(menuItemDTO.getMealPeriodName()),
                        menuItemDTO.getDate()
                );

        if (existingMenuItemInfo == null) {
            MenuItemInfo menuItemInfo = new MenuItemInfo(
                    menuItem,
                    mealPeriodRepository.findByPeriodName(menuItemDTO.getMealPeriodName()),
                    menuItemDTO.getDate()
            );
            menuItemInfoRepository.save(menuItemInfo);
            logger.debug("Created new MenuItemInfo for {} in restaurant {} on {}",
                    menuItem.getItemName(), restaurant.getName(), menuItemDTO.getDate());
        }
    }

    private void cleanupDuplicateMenuItems(List<MenuItem> duplicates, MenuItem keepItem) {
        logger.info("Starting cleanup of duplicate menu items for: {} in restaurant: {}",
                keepItem.getItemName(), keepItem.getRestaurant().getName());

        try {
            // Keep the first item and remove others only if they're from the same restaurant
            for (int i = 1; i < duplicates.size(); i++) {
                MenuItem duplicate = duplicates.get(i);

                // Only clean up if it's from the same restaurant
                if (duplicate.getRestaurant().getId().equals(keepItem.getRestaurant().getId())) {
                    // Update any MenuItemInfo references to point to the item we're keeping
                    List<MenuItemInfo> menuItemInfos = menuItemInfoRepository.findAllByMenuItem(duplicate);
                    for (MenuItemInfo info : menuItemInfos) {
                        info.setMenuItem(keepItem);
                        menuItemInfoRepository.save(info);
                    }

                    // Then delete the duplicate
                    menuItemRepository.delete(duplicate);
                    logger.debug("Removed duplicate menu item ID: {} for {} in restaurant {}",
                            duplicate.getId(), duplicate.getItemName(), duplicate.getRestaurant().getName());
                }
            }
        } catch (Exception e) {
            logger.error("Error during duplicate cleanup: {}", e.getMessage());
            logger.debug("Stack trace:", e);
        }
    }

    private String buildMenuUrl(LocalDate date, String menuPeriodName) {
        // Format the date in the required format for the URL
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Construct and return the URL with the formatted date and menu period name
        return "https://menu.dining.ucla.edu/Menus/" + formattedDate + "/" + menuPeriodName;
    }

    public List<HealthRestriction> buildListOfHealthRestrictions(List<String> healthRestrictionNames){
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