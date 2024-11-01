package menu.menuapi.controller;

import menu.menuapi.service.MenuCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // Requires ADMIN role for all endpoints in this controller
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final MenuCleanupService menuCleanupService;

    // Rate limiting configuration
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int RATE_LIMIT = 5; // requests
    private final int TIME_WINDOW = 1; // hour

    public AdminController(MenuCleanupService menuCleanupService) {
        this.menuCleanupService = menuCleanupService;
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(RATE_LIMIT,
                Refill.intervally(RATE_LIMIT, Duration.ofHours(TIME_WINDOW)));
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean checkRateLimit(String username) {
        Bucket bucket = buckets.computeIfAbsent(username, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

    @GetMapping("/duplicate-stats")
    public ResponseEntity<?> getDuplicateStats(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();

        // Rate limiting check
        if (!checkRateLimit(username)) {
            logger.warn("Rate limit exceeded for user {} accessing duplicate-stats", username);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Please try again later.");
        }

        try {
            logger.info("User {} requesting duplicate stats from IP {}",
                    username, request.getRemoteAddr());

            Map<String, Object> stats = menuCleanupService.getDuplicateStatistics();

            logger.debug("Successfully retrieved duplicate stats for user {}", username);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error retrieving duplicate stats for user {}: {}",
                    username, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving statistics");
        }
    }

    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<?> runOneTimeCleanup(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();

        // Rate limiting check
        if (!checkRateLimit(username)) {
            logger.warn("Rate limit exceeded for user {} accessing cleanup-duplicates",
                    username);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Please try again later.");
        }

        try {
            logger.info("User {} initiating duplicate cleanup from IP {}",
                    username, request.getRemoteAddr());

            menuCleanupService.runOneTimeCleanup();

            logger.info("Duplicate cleanup completed successfully by user {}", username);
            return ResponseEntity.ok("Cleanup completed successfully");

        } catch (Exception e) {
            logger.error("Error during duplicate cleanup initiated by user {}: {}",
                    username, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during cleanup operation");
        }
    }
}