document.addEventListener('DOMContentLoaded', () => {
    const scrapeForm = document.getElementById('scrapeForm');
    const urlInput = document.getElementById('urlInput');
    const submitBtn = document.getElementById('submitBtn');
    const resultsContainer = document.getElementById('results');
    const reviewsList = document.getElementById('reviewsList');
    const errorMsg = document.getElementById('errorMsg');

    scrapeForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const url = urlInput.value.trim();
        if (!url) return;

        // Reset UI
        setLoading(true);
        errorMsg.classList.add('hidden');
        resultsContainer.classList.add('hidden');
        reviewsList.innerHTML = '';

        try {
            const response = await fetch(`/api/scrape?url=${encodeURIComponent(url)}`);
            
            if (!response.ok) {
                throw new Error('Failed to fetch reviews');
            }

            const reviews = await response.json();

            if (reviews.length === 0) {
                showError('No reviews found. Please check the link or try another product.');
                return;
            }

            // Check for error-wrapped reviews
            const errorReview = reviews.find(r => r.text && r.text.startsWith('Error:'));
            if (errorReview) {
                showError(errorReview.text);
                return;
            }

            displayReviews(reviews);

        } catch (error) {
            console.error('Error:', error);
            showError('An error occurred while fetching reviews. Please try again.');
        } finally {
            setLoading(false);
        }
    });

    function displayReviews(reviews) {
        reviews.forEach(review => {
            const card = document.createElement('div');
            card.className = 'review-card';
            
            const p = document.createElement('p');
            p.className = 'review-text';
            p.textContent = review.text;
            
            card.appendChild(p);
            reviewsList.appendChild(card);
        });
        
        resultsContainer.classList.remove('hidden');
    }

    function showError(message) {
        errorMsg.textContent = message;
        errorMsg.classList.remove('hidden');
    }

    function setLoading(isLoading) {
        if (isLoading) {
            submitBtn.classList.add('loading');
            submitBtn.disabled = true;
        } else {
            submitBtn.classList.remove('loading');
            submitBtn.disabled = false;
        }
    }
});
