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
        setupDriver();
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

        // Anti-detection settings
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", java.util.Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        driver = new ChromeDriver(options);
    }

    @GetMapping("/scrape")
    public List<Review> scrape(@RequestParam String url) {
        List<Review> reviews = new ArrayList<>();

        try {
            // Ensure driver is ready (in case it crashed and was nullified)
            setupDriver();

            System.out.println("Processing URL: " + url);
            driver.get(url);

            // Get the final URL after redirects
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Resolved URL: " + currentUrl);

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
}
