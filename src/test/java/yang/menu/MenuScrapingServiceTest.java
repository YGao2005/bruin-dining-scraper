package yang.menu;

import menu.menuapi.DTO.MenuItemDTO;
import menu.menuapi.model.*;
import menu.menuapi.repository.*;
import menu.menuapi.scheduler.MenuScrapingScheduler;
import menu.menuapi.service.MenuScrapingService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


import java.util.concurrent.CompletableFuture;
/*
@ExtendWith(MockitoExtension.class)
class MenuScrapingServiceTest {

    @Mock
    private MealPeriodRepository mealPeriodRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private MenuItemInfoRepository menuItemInfoRepository;
    @Mock
    private HealthRestrictionRepository healthRestrictionRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ThemeInfoRepository themeInfoRepository;

    @InjectMocks
    private MenuScrapingService menuScrapingService;

    private Document mockDocument;
    private String sampleHtml;

    @BeforeEach
    void setUp() {
        // Sample HTML structure similar to the actual website
        sampleHtml = """
            <div>
                <h3 class="col-header">De Neve</h3>
                <div>
                    <li class="sect-item">Grill
                        <span class="tooltip-target-wrapper">
                            <a class="recipelink" href="/recipes/123">Burger</a>
                            <img class="webcode-16px" alt="Vegan" src="/icons/vegan.png"/>
                        </span>
                    </li>
                    <li class="sect-item">Theme of the Day
                        <a class="recipelink">Italian Night</a>
                    </li>
                </div>
            </div>
            """;
        mockDocument = Jsoup.parse(sampleHtml);

        // Set up default mock behaviors
        when(mealPeriodRepository.findByPeriodName(anyString()))
                .thenReturn(new MealPeriod("Lunch"));
        when(restaurantRepository.findByRestaurantName(anyString()))
                .thenReturn(new Restaurant("De Neve"));
        when(sectionRepository.findByName(anyString()))
                .thenReturn(new Section("Grill"));
    }

    @Test
    void testScrapeMenuDataForUpcomingWeekAsync() throws IOException {
        // Mock Jsoup to return empty document to minimize processing
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()).get())
                    .thenReturn(Jsoup.parse("<html><body></body></html>"));

            // Execute the async scraping
            CompletableFuture<Void> future = menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Lunch");

            // Wait for completion
            future.join();

            // Verify that scrapeAndSaveMenuData was called at least once for each day
            // Instead of verifying exact number of repository calls, verify the method was called
            verify(mealPeriodRepository, atLeast(7)).findByPeriodName("Lunch");
        }
    }

    @Test
    void testScrapeAndSaveMenuData() throws IOException {
        // Mock dependencies for a single scrape
        MealPeriod mockMealPeriod = new MealPeriod("Lunch");
        Restaurant mockRestaurant = new Restaurant("De Neve");
        Section mockSection = new Section("Grill");
        MenuItem mockMenuItem = new MenuItem("Burger", mockRestaurant, Arrays.asList());

        when(mealPeriodRepository.findByPeriodName(anyString())).thenReturn(mockMealPeriod);
        when(restaurantRepository.findByRestaurantName(anyString())).thenReturn(mockRestaurant);
        when(sectionRepository.findByName(anyString())).thenReturn(mockSection);
        when(menuItemRepository.findByItemNameAndRestaurant_RestaurantName(anyString(), anyString()))
                .thenReturn(null);

        // Mock Jsoup connection
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()).get())
                    .thenReturn(mockDocument);

            // Execute the method
            menuScrapingService.scrapeAndSaveMenuData("Lunch", LocalDate.now());

            // Verify essential interactions without specifying exact counts
            verify(menuItemRepository, atLeastOnce()).save(any(MenuItem.class));
            verify(menuItemInfoRepository, atLeastOnce()).save(any(MenuItemInfo.class));
            verify(themeRepository, atLeastOnce()).findByThemeName("Italian Night");
        }
    }

    @Test
    void testHandleConnectionError() {
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()).get())
                    .thenThrow(new IOException("Connection failed"));

            // The method should handle the exception gracefully
            assertDoesNotThrow(() ->
                    menuScrapingService.scrapeAndSaveMenuData("Lunch", LocalDate.now())
            );
        }
    }

    @Test
    void testSaveMenuItemDTO() {
        // Set up test data
        MealPeriod mealPeriod = new MealPeriod("Lunch");
        Restaurant restaurant = new Restaurant("De Neve");
        Section section = new Section("Grill");
        MenuItem menuItem = new MenuItem("Burger", restaurant, Arrays.asList());
        menuItem.setSection(section);

        when(mealPeriodRepository.findByPeriodName(anyString())).thenReturn(mealPeriod);
        when(restaurantRepository.findByRestaurantName(anyString())).thenReturn(restaurant);
        when(sectionRepository.findByName(anyString())).thenReturn(section);
        when(menuItemRepository.findByItemNameAndRestaurant_RestaurantName(anyString(), anyString()))
                .thenReturn(null);

        MenuItemDTO dto = new MenuItemDTO();
        dto.setItemName("Burger");
        dto.setRestaurantName("De Neve");
        dto.setMealPeriodName("Lunch");
        dto.setDate(LocalDate.now());
        dto.setSectionName("Grill");

        // Execute the save operation
        menuScrapingService.saveMenuItemDTO(dto, Arrays.asList());

        // Verify essential saves occurred
        verify(menuItemRepository).save(any(MenuItem.class));
        verify(menuItemInfoRepository).save(any(MenuItemInfo.class));
    }
}

@ExtendWith(MockitoExtension.class)
class MenuScrapingSchedulerTest {

    @Mock
    private MenuScrapingService menuScrapingService;

    @InjectMocks
    private MenuScrapingScheduler menuScrapingScheduler;

    @Test
    void testScrapeMenuDataPeriodically() {
        // Execute the scheduler
        menuScrapingScheduler.scrapeMenuDataPeriodically();

        // Verify that the scraping service was called for each meal period
        verify(menuScrapingService).scrapeMenuDataForUpcomingWeekAsync("Breakfast");
        verify(menuScrapingService).scrapeMenuDataForUpcomingWeekAsync("Lunch");
        verify(menuScrapingService).scrapeMenuDataForUpcomingWeekAsync("Dinner");
    }
}
*/
