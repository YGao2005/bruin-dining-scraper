package menu.menuapi.service;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WebsiteHealthMonitor {
    private static final Logger logger = LoggerFactory.getLogger(WebsiteHealthMonitor.class);

    private AtomicInteger failedRequestCount = new AtomicInteger(0);
    private LocalDateTime lastSuccessfulRequest;
    private LocalDateTime firstFailedRequest;

    private boolean hasAttemptedConnection = false;

    // Common HTTP status codes that might indicate rate limiting
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int FORBIDDEN = 403;
    private static final int SERVICE_UNAVAILABLE = 503;

    public boolean checkWebsiteAccess(String url) {
        try {
            Connection.Response response = Jsoup
                    .connect(url)
                    .method(Connection.Method.GET)
                    .timeout(10000) // 10 second timeout
                    .execute();

            Document doc = response.parse();

            // Check for specific response patterns that might indicate blocking
            boolean isBlocked = isBlockedResponse(response, doc);

            if (isBlocked) {
                handleBlockedAccess(url, response.statusCode());
                return false;
            }

            // Reset counters on successful request
            failedRequestCount.set(0);
            lastSuccessfulRequest = LocalDateTime.now();
            firstFailedRequest = null;

            logger.info("Successfully accessed website: {}", url);
            return true;

        } catch (HttpStatusException e) {
            handleHttpStatusException(url, e);
            return false;
        } catch (IOException e) {
            handleIOException(url, e);
            return false;
        }
    }

    private boolean isBlockedResponse(Connection.Response response, Document doc) {
        int statusCode = response.statusCode();
        String bodyText = doc.text().toLowerCase();

        // Check status codes
        if (statusCode == TOO_MANY_REQUESTS ||
                statusCode == FORBIDDEN ||
                statusCode == SERVICE_UNAVAILABLE) {
            return true;
        }

        // Check for common blocking indicators in response body
        return bodyText.contains("access denied") ||
                bodyText.contains("too many requests") ||
                bodyText.contains("rate limit exceeded") ||
                bodyText.contains("blocked") ||
                bodyText.contains("captcha") ||
                doc.select("form").stream()
                        .anyMatch(form -> form.html().toLowerCase().contains("captcha"));
    }

    private void handleBlockedAccess(String url, int statusCode) {
        failedRequestCount.incrementAndGet();
        if (firstFailedRequest == null) {
            firstFailedRequest = LocalDateTime.now();
        }

        logger.error("Access blocked to {}, Status Code: {}, Failed Request Count: {}, " +
                        "First Failed: {}, Last Success: {}",
                url, statusCode, failedRequestCount.get(),
                firstFailedRequest, lastSuccessfulRequest);
    }

    private void handleHttpStatusException(String url, HttpStatusException e) {
        failedRequestCount.incrementAndGet();
        if (firstFailedRequest == null) {
            firstFailedRequest = LocalDateTime.now();
        }

        String errorType = getErrorType(e.getStatusCode());
        logger.error("{} detected for URL: {}, Status Code: {}, Failed Request Count: {}, " +
                        "First Failed: {}, Last Success: {}",
                errorType, url, e.getStatusCode(), failedRequestCount.get(),
                firstFailedRequest, lastSuccessfulRequest);
    }

    private void handleIOException(String url, IOException e) {
        failedRequestCount.incrementAndGet();
        if (firstFailedRequest == null) {
            firstFailedRequest = LocalDateTime.now();
        }

        logger.error("Connection error for URL: {}, Error: {}, Failed Request Count: {}, " +
                        "First Failed: {}, Last Success: {}",
                url, e.getMessage(), failedRequestCount.get(),
                firstFailedRequest, lastSuccessfulRequest);
    }

    private String getErrorType(int statusCode) {
        return switch (statusCode) {
            case TOO_MANY_REQUESTS -> "Rate limiting";
            case FORBIDDEN -> "IP blocking";
            case SERVICE_UNAVAILABLE -> "Service unavailable";
            default -> "Unknown error";
        };
    }

    // Getters for monitoring state
    public int getFailedRequestCount() {
        return failedRequestCount.get();
    }

    public LocalDateTime getLastSuccessfulRequest() {
        return lastSuccessfulRequest;
    }

    public LocalDateTime getFirstFailedRequest() {
        return firstFailedRequest;
    }

    public boolean isLikelyBlocked() {
        return failedRequestCount.get() >= 5;
    }

    public boolean hasAttemptedConnection() {
        return hasAttemptedConnection;
    }
    public Map<String, Object> getDetailedStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("hasAttemptedConnection", hasAttemptedConnection);
        status.put("failedRequests", getFailedRequestCount());
        status.put("lastSuccess", getLastSuccessfulRequest() != null ?
                getLastSuccessfulRequest().toString() :
                "No successful requests yet");
        status.put("firstFailed", getFirstFailedRequest() != null ?
                getFirstFailedRequest().toString() :
                "No failed requests yet");
        status.put("isBlocked", isLikelyBlocked());
        status.put("currentStatus", getCurrentStatus());

        return status;
    }

    private String getCurrentStatus() {
        if (!hasAttemptedConnection) {
            return "NO_ATTEMPTS";
        }
        if (isLikelyBlocked()) {
            return "BLOCKED";
        }
        if (getFailedRequestCount() > 0) {
            return "EXPERIENCING_ISSUES";
        }
        if (getLastSuccessfulRequest() != null) {
            return "HEALTHY";
        }
        return "UNKNOWN";
    }

}