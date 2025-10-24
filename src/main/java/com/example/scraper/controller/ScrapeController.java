package com.example.scraper.controller;

import com.example.scraper.model.Review;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
@RestController
@CrossOrigin
@RequestMapping("/api")
public class ScrapeController {

    @GetMapping("/scrape")
    public List<Review> scrape(@RequestParam String url) {
        List<Review> reviews = new ArrayList<>();
        try {
            // Expand short links like amzn.in/d/...
            if (url.contains("amzn.in")) {
                url = Jsoup.connect(url).followRedirects(true).get().location();
            }
             Connection connection = Jsoup.connect(url)
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
             + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    .referrer("https://www.google.com/")
    .header("Accept-Language", "en-US,en;q=0.9")
    .header("Accept-Encoding", "gzip, deflate")
    .timeout(20000)  // 20 seconds timeout
    .followRedirects(true);

         Document doc = connection.get();

            Elements reviewElements;

            if (url.contains("amazon.")) {
                reviewElements = doc.select(".review-text-content span");
            } else if (url.contains("flipkart.")) {
                reviewElements = doc.select("div._6K-7Co");
            } else {
                throw new IOException("Unsupported website");
            }

            reviewElements.forEach(el -> {
                String text = el.text()
                        .replaceAll("(?i)Read more", "") // remove "Read more"
                        .trim();

                if (!text.isEmpty()) {
                    reviews.add(new Review(text));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            reviews.add(new Review("Error: " + e.getMessage()));
        }

        return reviews;
    }
}
