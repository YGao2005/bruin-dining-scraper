package menu.menuapi.service;

import menu.menuapi.repository.MealPeriodRepository;
import menu.menuapi.repository.MenuItemRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.model.MealPeriod;
import menu.menuapi.model.MenuItem;
import menu.menuapi.model.MenuItemInfo;
import menu.menuapi.model.Restaurant;
import menu.menuapi.repository.MenuItemInfoRepository;
import menu.menuapi.repository.RestaurantRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class MenuScrapingService {

    private final MealPeriodRepository mealPeriodRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    private final MenuItemInfoRepository menuItemInfoRepository;

    public MenuScrapingService(MealPeriodRepository mealPeriodRepository,
                               MenuItemRepository menuItemRepository,
                               RestaurantRepository restaurantRepository,
                               MenuItemInfoRepository menuItemInfoRepository) {
        this.mealPeriodRepository = mealPeriodRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemInfoRepository = menuItemInfoRepository;
    }

    public void scrapeMenuDataForUpcomingWeek(String menuPeriodName) {
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.plusDays(6); // Get the date for the end of the week

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            scrapeAndSaveMenuData(menuPeriodName, currentDate);
            currentDate = currentDate.plusDays(1); // Move to the next day
        }
    }

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

            // Extract restaurants and menu items
            Elements restaurantElements = doc.select("h3.col-header");
            for (Element restaurantElement : restaurantElements) {
                String restaurantName = restaurantElement.text().trim();

                // Check if the restaurant already exists in the database
                Restaurant existingRestaurant = restaurantRepository.findByRestaurantName(restaurantName);
                Restaurant restaurant;
                if (existingRestaurant != null) {
                    restaurant = existingRestaurant; // Use existing restaurant
                } else {
                    restaurant = new Restaurant(restaurantName); // Create new restaurant
                }

                restaurantRepository.save(restaurant);

                // Select all a.recipelink elements within the same parent container as the restaurant element
                Elements menuItemElements = restaurantElement.parent().select("a.recipelink");
                for (Element menuItemElement : menuItemElements) {
                    String itemName = menuItemElement.text().trim();
                    String nutritionalLink = menuItemElement.attr("href").trim();

                    // Create MenuItemDTO with the extracted data
                    MenuItemDTO menuItemDTO = new MenuItemDTO();
                    menuItemDTO.setItemName(itemName);
                    menuItemDTO.setNutritionalLink(nutritionalLink);
                    menuItemDTO.setMealPeriodName(mealPeriod.getName());
                    menuItemDTO.setRestaurantName(restaurantName);
                    menuItemDTO.setDate(date);

                    // Save MenuItemDTO
                    saveMenuItemDTO(menuItemDTO);
                }
            }

            System.out.println("Menu data scraped and saved successfully for " + date);
        } catch (IOException e) {
            System.err.println("Error scraping menu data for " + date + ": " + e.getMessage());
        }
    }

    private void saveMenuItemDTO(MenuItemDTO menuItemDTO) {
        // Check if the MenuItem already exists in the database
        MenuItem menuItem = menuItemRepository.findByItemName(menuItemDTO.getItemName());
        if (menuItem == null) {
            // If not, create a new MenuItem
            menuItem = new MenuItem(menuItemDTO.getItemName(), restaurantRepository.findByRestaurantName(menuItemDTO.getRestaurantName()));
            menuItem.setNutritionalLink(menuItemDTO.getNutritionalLink());
            // Save MenuItem
            menuItemRepository.save(menuItem);
        }

        // Create a new MenuItemInfo
        MenuItemInfo menuItemInfo = new MenuItemInfo(menuItem, mealPeriodRepository.findByPeriodName(menuItemDTO.getMealPeriodName()), menuItemDTO.getDate());
        // Save MenuItemInfo
        menuItemInfoRepository.save(menuItemInfo);
    }

    private String buildMenuUrl(LocalDate date, String menuPeriodName) {
        // Format the date in the required format for the URL
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Construct and return the URL with the formatted date and menu period name
        return "https://menu.dining.ucla.edu/Menus/" + formattedDate + "/" + menuPeriodName;
    }
}
