// Polling variables
let pollingInterval;
let startTime;
let tickCount = 0;

function updateProgress(progressDecimal) {
    const progressPercentage = progressDecimal * 100;
    // Update progress bar width
    const progressBar = document.getElementById('progressBar');
    progressBar.style.width = progressPercentage + '%';

    // Update percentage text
    document.getElementById('progressPercentage').textContent = Math.round(progressPercentage);

    // Update elapsed time
    const elapsed = Math.floor((Date.now() - startTime) / 1000);
    document.getElementById('elapsedTime').textContent = elapsed + 's';
}

function startPolling() {
    // Start time tracking
    startTime = Date.now();

    // Poll every 500ms
    pollingInterval = setInterval(() => {
        fetch('/api/simulation/progress')
            .then(response => {
                if (!response.ok) {
                    console.error('Failed to fetch progress');
                    return;
                }
                return response.json();
            })
            .then(data => {
                if (!data) return;

                const percentage = data.progressPercentage;
                updateProgress(percentage);

                // If simulation is complete, redirect to results
                if (percentage >= 1.0) {
                    stopPolling();
                    // Small delay to show 100%
                    setTimeout(() => {
                        window.location.href = '/results.html';
                    }, 500);
                }
            })
            .catch(error => {
                console.error('Error polling progress:', error);
            });
    }, 500);
}

function stopPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    startPolling();
});

// Clean up polling if user leaves the page
window.addEventListener('beforeunload', () => {
    stopPolling();
});
