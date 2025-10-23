async function fetchReviews() {
    const url = document.getElementById('urlInput').value.trim();
    const reviewsContainer = document.getElementById('reviews');
    reviewsContainer.innerHTML = "⏳ Fetching reviews...";

    try {
        const response = await fetch(`/api/scrape?url=${encodeURIComponent(url)}`);
        const reviews = await response.json();

        if (!reviews.length) {
            reviewsContainer.innerHTML = "❌ No reviews found.";
            return;
        }

        reviewsContainer.innerHTML = reviews.map(r => `<div class='review'>${r.text}</div>`).join('');
    } catch (err) {
        reviewsContainer.innerHTML = "⚠️ Error fetching reviews.";
    }
}
