package menu.menuapi.controller;

import menu.menuapi.service.MenuCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final MenuCleanupService menuCleanupService;

    public AdminController(MenuCleanupService menuCleanupService) {
        this.menuCleanupService = menuCleanupService;
    }

    @GetMapping("/duplicate-stats")
    public Map<String, Object> getDuplicateStats() {
        return menuCleanupService.getDuplicateStatistics();
    }

    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<String> runOneTimeCleanup() {
        menuCleanupService.runOneTimeCleanup();
        return ResponseEntity.ok("Cleanup completed successfully");
    }
}