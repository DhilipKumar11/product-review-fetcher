package com.example.scraper.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.scraper.model.Review;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ScrapeController {

    @PostMapping("/scrape")
    public ResponseEntity<List<Review>> scrapeReviews(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        try {
            // 🧠 Step 1: Expand short Amazon links automatically
            if (url.contains("amzn.in")) {
                url = Jsoup.connect(url)
                        .followRedirects(true)
                        .execute()
                        .url()
                        .toString();
            }

            // 🧠 Step 2: Fetch product page using a browser-like user agent
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            List<Review> reviews = new ArrayList<>();

            // 🛒 Step 3: Parse reviews (Amazon example)
            Elements reviewBlocks = doc.select("div[data-hook=review]");
            for (Element block : reviewBlocks) {
                String reviewer = block.select("span.a-profile-name").text();
                String ratingText = block.select("i[data-hook=review-star-rating] span.a-icon-alt").text();
                double rating = ratingText.isEmpty() ? 0.0 : Double.parseDouble(ratingText.split(" ")[0]);
                String comment = block.select("span[data-hook=review-body]").text();

                reviews.add(new Review(reviewer, rating, comment));
            }

            // 🧾 Step 4: Return reviews if found
            if (reviews.isEmpty()) {
                reviews.add(new Review("Info", 0, "No reviews found or page structure changed."));
            }

            return ResponseEntity.ok(reviews);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(new Review("Error", 0, "Unable to fetch reviews: " + e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of(new Review("Error", 0, "Something went wrong: " + e.getMessage())));
        }
    }
}
