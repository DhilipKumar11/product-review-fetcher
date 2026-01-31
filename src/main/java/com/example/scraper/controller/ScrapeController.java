package com.example.scraper.controller;

import com.example.scraper.model.Review;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ScrapeController {

    private WebDriver driver;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Initialize driver in a background thread to avoid blocking application
        // startup
        new Thread(() -> {
            setupDriver();
            System.out.println("Driver initialization completed in background.");
        }).start();
    }

    private void setupDriver() {
        if (driver != null) {
            return;
        }
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.EAGER);
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--blink-settings=imagesEnabled=false");

        // Window size to avoid mobile detection
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");

        // Anti-detection settings
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", java.util.Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // Additional anti-bot measures
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=IsolateOrigins,site-per-process");
        options.addArguments("--lang=en-US,en;q=0.9");
        options.addArguments("--accept-lang=en-US,en;q=0.9");

        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        driver = new ChromeDriver(options);

        // Set script timeout
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
    }

    @GetMapping("/scrape")
    public List<Review> scrape(@RequestParam String url) {
        List<Review> reviews = new ArrayList<>();

        try {
            // Ensure driver is ready (in case it crashed and was nullified)
            setupDriver();

            // Set explicit page load timeout
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

            System.out.println("Processing URL: " + url);

            String currentUrl = url;
            try {
                driver.get(url);
                // Get the final URL after redirects
                currentUrl = driver.getCurrentUrl();
                System.out.println("Resolved URL: " + currentUrl);
            } catch (org.openqa.selenium.TimeoutException e) {
                System.out.println("Timeout on initial navigation. Attempting to stop page load and continue...");
                // Stop the page load
                driver.navigate().to("javascript:window.stop();");
                currentUrl = driver.getCurrentUrl();
                System.out.println("Current URL after timeout: " + currentUrl);

                // If we got redirected to Amazon, try to extract product ID and go directly to
                // reviews
                if (currentUrl.contains("amazon.") || currentUrl.contains("amzn.")) {
                    String productId = extractAmazonProductId(currentUrl);
                    if (productId != null) {
                        String reviewsUrl = buildAmazonReviewsUrl(currentUrl, productId);
                        System.out.println("Navigating directly to reviews page: " + reviewsUrl);
                        try {
                            driver.get(reviewsUrl);
                            currentUrl = driver.getCurrentUrl();
                        } catch (org.openqa.selenium.TimeoutException e2) {
                            System.out.println("Timeout on reviews page too. Stopping and continuing...");
                            driver.navigate().to("javascript:window.stop();");
                            currentUrl = driver.getCurrentUrl();
                        }
                    }
                }
            }

            // Wait for reviews to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> reviewElements = new ArrayList<>();

            if (currentUrl.contains("amazon.") || currentUrl.contains("amzn.")) {
                try {
                    // Wait for EITHER the standard selector OR the mobile/alternative selector
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions
                                    .presenceOfElementLocated(By.cssSelector("span[data-hook='review-body']")),
                            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".review-text-content span"))));

                    // Try to find elements with the primary selector first
                    reviewElements = driver.findElements(By.cssSelector("span[data-hook='review-body']"));

                    // If none found, try the secondary selector
                    if (reviewElements.isEmpty()) {
                        reviewElements = driver.findElements(By.cssSelector(".review-text-content span"));
                    }
                } catch (Exception e) {
                    System.out.println("Timeout waiting for Amazon review selectors.");
                }
            } else if (currentUrl.contains("flipkart.")) {
                try {
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.t-ZTKy")),
                            ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.ZmyHeo"))));

                    reviewElements = driver.findElements(By.cssSelector("div.t-ZTKy"));
                    if (reviewElements.isEmpty()) {
                        reviewElements = driver.findElements(By.cssSelector("div.ZmyHeo"));
                    }
                } catch (Exception e) {
                    System.out.println("Timeout waiting for Flipkart review selectors.");
                }
            } else {
                reviews.add(new Review("Error: Unsupported website: " + currentUrl));
                return reviews;
            }

            System.out.println("Found " + reviewElements.size() + " reviews.");

            for (WebElement el : reviewElements) {
                String text = el.getText()
                        .replaceAll("(?i)Read more", "")
                        .trim();

                if (!text.isEmpty()) {
                    reviews.add(new Review(text));
                }
            }

            if (reviews.isEmpty()) {
                String title = driver.getTitle();
                System.out.println("No reviews found. Page title: " + title);
            }

        } catch (Exception e) {
            e.printStackTrace();
            reviews.add(new Review("Error: " + e.getMessage()));

            if (e.getMessage() != null && (e.getMessage().contains("Session") || e.getMessage().contains("died"))) {
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception ignored) {
                    }
                    driver = null;
                }
            }
        }

        return reviews;
    }

    private String extractAmazonProductId(String url) {
        // Extract ASIN (Amazon Standard Identification Number) from URL
        // Patterns: /dp/ASIN, /product/ASIN, /gp/product/ASIN
        try {
            if (url.contains("/dp/")) {
                int start = url.indexOf("/dp/") + 4;
                int end = url.indexOf("/", start);
                if (end == -1)
                    end = url.indexOf("?", start);
                if (end == -1)
                    end = url.length();
                return url.substring(start, end);
            } else if (url.contains("/product/")) {
                int start = url.indexOf("/product/") + 9;
                int end = url.indexOf("/", start);
                if (end == -1)
                    end = url.indexOf("?", start);
                if (end == -1)
                    end = url.length();
                return url.substring(start, end);
            } else if (url.contains("/gp/product/")) {
                int start = url.indexOf("/gp/product/") + 12;
                int end = url.indexOf("/", start);
                if (end == -1)
                    end = url.indexOf("?", start);
                if (end == -1)
                    end = url.length();
                return url.substring(start, end);
            }
        } catch (Exception e) {
            System.out.println("Error extracting product ID: " + e.getMessage());
        }
        return null;
    }

    private String buildAmazonReviewsUrl(String originalUrl, String productId) {
        // Build the reviews page URL based on the domain
        String domain = "amazon.in"; // default

        if (originalUrl.contains("amazon.com")) {
            domain = "amazon.com";
        } else if (originalUrl.contains("amazon.co.uk")) {
            domain = "amazon.co.uk";
        } else if (originalUrl.contains("amazon.in")) {
            domain = "amazon.in";
        } else if (originalUrl.contains("amazon.de")) {
            domain = "amazon.de";
        }

        return "https://www." + domain + "/product-reviews/" + productId;
    }
}
