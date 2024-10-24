package menu.menuapi.controller;

import menu.menuapi.service.WebsiteHealthMonitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scraper/status")
public class ScraperStatusController {
    private final WebsiteHealthMonitor healthMonitor;

    public ScraperStatusController(WebsiteHealthMonitor healthMonitor) {
        this.healthMonitor = healthMonitor;
    }

    @GetMapping
    public Map<String, Object> getStatus() {
        // Use HashMap instead of Map.of() to allow null values
        Map<String, Object> status = new HashMap<>();

        // Add values with null checks
        status.put("failedRequests", healthMonitor.getFailedRequestCount());

        LocalDateTime lastSuccess = healthMonitor.getLastSuccessfulRequest();
        status.put("lastSuccess", lastSuccess != null ? lastSuccess.toString() : "No successful requests yet");

        LocalDateTime firstFailed = healthMonitor.getFirstFailedRequest();
        status.put("firstFailed", firstFailed != null ? firstFailed.toString() : "No failed requests yet");

        status.put("isBlocked", healthMonitor.isLikelyBlocked());

        return status;
    }
}