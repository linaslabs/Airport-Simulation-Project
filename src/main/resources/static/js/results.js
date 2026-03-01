function setText(id, value){
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function formatNumber(num) {
    return typeof num === 'number' ? num.toFixed(2) : '--';
}

function loadResults(){
    fetch('/api/results/summary')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch results');
            }
            return response.json();
        })
        .then(data => {
            // Map StatisticsSummary fields to HTML elements
            setText("throughput", formatNumber(data.hourlyThroughput));

            // Departures
            setText("depAvgWait", formatNumber(data.avgWaitTime));
            setText("depMaxQueue", data.maxTakeOffQueueSize);
            setText("depMaxDelay", formatNumber(data.maxTakeOffDelay));
            setText("depAvgDelay", formatNumber(data.avgTakeOffDelay));
            setText("depCancelled", data.cancellationCount);

            // Arrivals
            setText("arrAvgHold", formatNumber(data.avgHoldingTime));
            setText("arrMaxHolding", data.maxHoldingSize);
            setText("arrMaxDelay", formatNumber(data.maxArrivalDelay));
            setText("arrAvgDelay", formatNumber(data.avgArrivalDelay));
            setText("arrDiverted", data.diversionCount);
        })
        .catch(error => {
            console.error('Error loading results:', error);
            // Fallback: display '--' for all fields
        });
}

function saveResults(){
    // in future this might POST to server; for now simply notify user
    alert("Save successfully");
}


// Wire buttons + init
document.addEventListener("DOMContentLoaded", () => {
    loadResults();

    const saveBtn = document.getElementById("saveBtn");

    if (saveBtn) saveBtn.addEventListener("click", saveResults);
});
