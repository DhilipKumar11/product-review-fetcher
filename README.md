# Product Review Fetcher 🛍️

A powerful web application that instantly fetches and displays product reviews from Amazon and Flipkart. Built with Spring Boot and Selenium WebDriver, this tool bypasses bot detection to reliably scrape reviews from e-commerce platforms.

## 🌐 Live Demo

**[Try it now on Render](https://review-fetcher.onrender.com)**

## ✨ Features

- 🔍 **Instant Review Fetching** - Simply paste an Amazon or Flipkart product URL
- 🌐 **Multiple Amazon Domains** - Supports amazon.com, amazon.in, amazon.co.uk, amazon.de, and more
- 🔗 **Shortened URL Support** - Works with shortened URLs (e.g., amzn.in/d/xxxxx)
- 🤖 **Anti-Bot Detection** - Advanced techniques to bypass e-commerce bot detection
- ⚡ **Smart Timeout Handling** - Automatically recovers from page load timeouts
- 📱 **Responsive Design** - Clean, modern UI that works on all devices
- 🎯 **Direct Review Navigation** - Bypasses product pages to go straight to reviews

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Chrome browser (for Selenium WebDriver)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/DhilipKumar11/product-review-fetcher.git
   cd product-review-fetcher
   ```

2. **Build the project**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Open your browser**
   ```
   http://localhost:8080
   ```

## 📖 How to Use

1. Navigate to the application (locally or via [Render](https://review-fetcher.onrender.com))
2. Paste an Amazon or Flipkart product URL in the input field
3. Click "Fetch Reviews"
4. View the extracted reviews instantly!

### Supported URL Formats

**Amazon:**
- Full URLs: `https://www.amazon.in/dp/ASIN`
- Shortened URLs: `https://amzn.in/d/xxxxx`
- Product pages: `https://www.amazon.com/product/ASIN`

**Flipkart:**
- Product URLs: `https://www.flipkart.com/product-name/p/itm...`

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.4.10** - Application framework
- **Selenium WebDriver 4.27.0** - Web scraping and automation
- **WebDriverManager 5.8.0** - Automatic ChromeDriver management
- **Jsoup 1.18.1** - HTML parsing
- **Java 17** - Programming language

### Frontend
- **HTML5** - Structure
- **CSS3** - Styling with modern design
- **Vanilla JavaScript** - Dynamic interactions

### Deployment
- **Render** - Cloud hosting platform
- **Docker** - Containerization

## 🏗️ Architecture

```
┌─────────────────┐
│   Frontend      │
│  (HTML/CSS/JS)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Spring Boot    │
│  REST API       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Selenium       │
│  WebDriver      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Amazon/        │
│  Flipkart       │
└─────────────────┘
```

## 🔧 Configuration

### Chrome Options (Anti-Detection)

The application uses several techniques to avoid bot detection:

- Headless Chrome with anti-automation flags disabled
- Custom user agent strings
- Window size configuration to avoid mobile detection
- Language and locale settings
- Web security and CORS handling

### Timeout Settings

- **Page Load Timeout**: 30 seconds
- **Script Timeout**: 30 seconds
- **Element Wait Timeout**: 10 seconds

## 📁 Project Structure

```
review-fetcher/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/scraper/
│   │   │       ├── controller/
│   │   │       │   └── ScrapeController.java
│   │   │       ├── model/
│   │   │       │   └── Review.java
│   │   │       └── ScraperApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── style.css
│   │       │   └── script.js
│   │       └── application.properties
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

## 🐳 Docker Deployment

The application includes a Dockerfile for containerized deployment:

```bash
# Build the Docker image
docker build -t review-fetcher .

# Run the container
docker run -p 8080:8080 review-fetcher
```

## 🌟 Key Features Explained

### Smart Timeout Recovery

When Amazon blocks or delays page loads, the application:
1. Stops the page load after 30 seconds
2. Extracts the product ID from the partial URL
3. Navigates directly to the reviews page
4. Bypasses product page bot detection

### Product ID Extraction

Supports multiple Amazon URL patterns:
- `/dp/ASIN`
- `/product/ASIN`
- `/gp/product/ASIN`

### Direct Review Navigation

Constructs review URLs in the format:
```
https://www.{domain}/product-reviews/{productId}
```

This bypasses the product page entirely, avoiding stricter bot detection.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

**Dhilip Kumar**

- GitHub: [@DhilipKumar11](https://github.com/DhilipKumar11)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Selenium WebDriver for powerful browser automation
- Render for free hosting services

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/DhilipKumar11/product-review-fetcher/issues) page
2. Create a new issue with detailed information
3. Contact via GitHub

---

**⭐ If you find this project useful, please consider giving it a star!**
