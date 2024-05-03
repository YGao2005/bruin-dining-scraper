package menu.menuapi.scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import menu.menuapi.service.MenuScrapingService;

@Component
public class MenuScrapingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MenuScrapingScheduler.class);

    private final MenuScrapingService menuScrapingService;

    public MenuScrapingScheduler(MenuScrapingService menuScrapingService) {
        this.menuScrapingService = menuScrapingService;
    }

    @Scheduled(cron = "0 0 3 * * *") // At midnight every day
    public void scrapeMenuDataPeriodically() {
        logger.info("Starting scheduled menu scraping...");

        // Log a message for each section being scraped
        logger.debug("Scraping menu data for Breakfast...");
        menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Breakfast");

        logger.debug("Scraping menu data for Lunch...");
        menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Lunch");

        logger.debug("Scraping menu data for Dinner...");
        menuScrapingService.scrapeMenuDataForUpcomingWeekAsync("Dinner");

        logger.info("Scheduled menu scraping completed.");
    }
}
