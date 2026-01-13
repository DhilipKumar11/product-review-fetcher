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

    @GetMapping("/scrape")
    public List<Review> scrape(@RequestParam String url) {
        List<Review> reviews = new ArrayList<>();
        WebDriver driver = null;

        try {
            // Setup ChromeDriver
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.EAGER); // Don't wait for all resources
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--blink-settings=imagesEnabled=false"); // Disable images
            options.addArguments(
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);

            System.out.println("Processing URL: " + url);

            // Handle Amazon URL adjustment for mobile if needed, but Desktop usually works
            // better with Selenium
            // However, sticking to the user's provided URL mostly, but cleaning it

            driver.get(url);

            // Get the final URL after redirects
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Resolved URL: " + currentUrl);

            // Wait for reviews to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> reviewElements;

            if (currentUrl.contains("amazon.") || currentUrl.contains("amzn.")) {
                try {
                    // Try waiting for the specific element
                    wait.until(ExpectedConditions
                            .presenceOfElementLocated(By.cssSelector("span[data-hook='review-body']")));
                    reviewElements = driver.findElements(By.cssSelector("span[data-hook='review-body']"));
                } catch (Exception e) {
                    // Fallback
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
                System.out.println("No reviews found. Page title: " + driver.getTitle());
            }

        } catch (Exception e) {
            e.printStackTrace();
            reviews.add(new Review("Error: " + e.getMessage()));
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return reviews;
    }
}
