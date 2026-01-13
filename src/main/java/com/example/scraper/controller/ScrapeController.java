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
            setupDriver();

            System.out.println("Processing URL: " + url);
            driver.get(url);

            // Get the final URL after redirects
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Resolved URL: " + currentUrl);

            // Wait for reviews to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> reviewElements;

            if (currentUrl.contains("amazon.") || currentUrl.contains("amzn.")) {
                try {
                    wait.until(ExpectedConditions
                            .presenceOfElementLocated(By.cssSelector("span[data-hook='review-body']")));
                    reviewElements = driver.findElements(By.cssSelector("span[data-hook='review-body']"));
                } catch (Exception e) {
                    reviewElements = driver.findElements(By.cssSelector(".review-text-content span"));
                }
            } else if (currentUrl.contains("flipkart.")) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.t-ZTKy")));
                reviewElements = driver.findElements(By.cssSelector("div.t-ZTKy"));
                if (reviewElements.isEmpty()) {
                    reviewElements = driver.findElements(By.cssSelector("div.ZmyHeo"));
                }
            } else {
                throw new Exception("Unsupported website: " + currentUrl);
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

                // Debug: Take screenshot
                try {
                    java.io.File screenshot = ((org.openqa.selenium.TakesScreenshot) driver)
                            .getScreenshotAs(org.openqa.selenium.OutputType.FILE);
                    String filename = "debug_failure_" + System.currentTimeMillis() + ".png";
                    org.springframework.util.FileCopyUtils.copy(screenshot, new java.io.File(filename));
                    System.out.println("Saved debug screenshot to " + filename);
                } catch (Exception se) {
                    System.out.println("Failed to save screenshot: " + se.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            reviews.add(new Review("Error: " + e.getMessage()));

            // If logging shows session invalid, we might want to quit driver to force
            // restart next time
            if (e.getMessage().contains("Session") || e.getMessage().contains("died")) {
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception ignored) {
                    }
                    driver = null;
                }
            }
        }
        // Do NOT quit the driver here, keep it alive for next request

        return reviews;
    }
}
