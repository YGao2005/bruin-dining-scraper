package menu.menuapi.service;

import menu.menuapi.model.MenuItem;
import menu.menuapi.model.MenuItemInfo;
import menu.menuapi.repository.MenuItemRepository;
import menu.menuapi.repository.MenuItemInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(MenuCleanupService.class);

    private final MenuItemRepository menuItemRepository;
    private final MenuItemInfoRepository menuItemInfoRepository;

    public MenuCleanupService(MenuItemRepository menuItemRepository,
                              MenuItemInfoRepository menuItemInfoRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuItemInfoRepository = menuItemInfoRepository;
    }

    @Transactional
    public void runOneTimeCleanup() {
        logger.info("Starting one-time cleanup of duplicate menu items");
        try {
            int totalDuplicatesCleaned = cleanupDuplicateMenuItems();
            logger.info("One-time cleanup completed. Removed {} duplicate items", totalDuplicatesCleaned);
        } catch (Exception e) {
            logger.error("Error during one-time cleanup: {}", e.getMessage());
            logger.debug("Stack trace:", e);
            throw e;
        }
    }

    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM every day
    @Transactional
    public void scheduledCleanup() {
        logger.info("Starting scheduled cleanup of duplicate menu items");
        try {
            int totalDuplicatesCleaned = cleanupDuplicateMenuItems();
            logger.info("Scheduled cleanup completed. Removed {} duplicate items", totalDuplicatesCleaned);
        } catch (Exception e) {
            logger.error("Error during scheduled cleanup: {}", e.getMessage());
            logger.debug("Stack trace:", e);
        }
    }

    private int cleanupDuplicateMenuItems() {
        int totalDuplicatesRemoved = 0;

        // Get all menu items
        List<MenuItem> allItems = menuItemRepository.findAll();
        logger.debug("Found {} total menu items", allItems.size());

        // Group items by name and restaurant
        Map<String, List<MenuItem>> duplicateGroups = allItems.stream()
                .collect(Collectors.groupingBy(item ->
                        item.getItemName() + "|" + item.getRestaurant().getName()
                ));

        // Process each group that has duplicates
        for (Map.Entry<String, List<MenuItem>> entry : duplicateGroups.entrySet()) {
            List<MenuItem> items = entry.getValue();
            if (items.size() > 1) {
                int removedCount = cleanupDuplicateGroup(items);
                totalDuplicatesRemoved += removedCount;
            }
        }

        return totalDuplicatesRemoved;
    }

    private int cleanupDuplicateGroup(List<MenuItem> duplicates) {
        if (duplicates.size() <= 1) {
            return 0;
        }

        // Sort by ID to always keep the oldest entry
        duplicates.sort(Comparator.comparing(MenuItem::getId));
        MenuItem keepItem = duplicates.get(0);

        logger.debug("Processing duplicate group for item: {}. Found {} duplicates",
                keepItem.getItemName(), duplicates.size() - 1);

        int removedCount = 0;
        for (int i = 1; i < duplicates.size(); i++) {
            MenuItem duplicate = duplicates.get(i);
            try {
                // Update MenuItemInfo references
                List<MenuItemInfo> menuItemInfos = menuItemInfoRepository.findAllByMenuItem(duplicate);
                for (MenuItemInfo info : menuItemInfos) {
                    info.setMenuItem(keepItem);
                    menuItemInfoRepository.save(info);
                }

                // Delete the duplicate
                menuItemRepository.delete(duplicate);
                removedCount++;
                logger.debug("Removed duplicate menu item ID: {} for {}",
                        duplicate.getId(), duplicate.getItemName());

            } catch (Exception e) {
                logger.error("Error cleaning up duplicate menu item {}: {}",
                        duplicate.getId(), e.getMessage());
            }
        }

        return removedCount;
    }

    // Method to get statistics about potential duplicates
    public Map<String, Object> getDuplicateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<MenuItem> allItems = menuItemRepository.findAll();

        Map<String, List<MenuItem>> duplicateGroups = allItems.stream()
                .collect(Collectors.groupingBy(item ->
                        item.getItemName() + "|" + item.getRestaurant().getName()
                ));

        long totalDuplicateGroups = duplicateGroups.values().stream()
                .filter(list -> list.size() > 1)
                .count();

        long totalDuplicateItems = duplicateGroups.values().stream()
                .filter(list -> list.size() > 1)
                .mapToLong(list -> list.size() - 1)
                .sum();

        stats.put("totalItems", allItems.size());
        stats.put("duplicateGroups", totalDuplicateGroups);
        stats.put("duplicateItems", totalDuplicateItems);

        return stats;
    }
}