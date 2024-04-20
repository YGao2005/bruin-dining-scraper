package menu.menuapi.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import menu.menuapi.service.MenuScrapingService;

@Component
public class MenuScrapingScheduler {

    private final MenuScrapingService menuScrapingService;

    public MenuScrapingScheduler(MenuScrapingService menuScrapingService) {
        this.menuScrapingService = menuScrapingService;
    }

    @Scheduled(cron = "0 0 0 * * *") // At midnight every day
    public void scrapeMenuDataPeriodically() {
        menuScrapingService.scrapeMenuDataForUpcomingWeek("Breakfast");
        menuScrapingService.scrapeMenuDataForUpcomingWeek("Lunch");
        menuScrapingService.scrapeMenuDataForUpcomingWeek("Dinner");
    }
}
