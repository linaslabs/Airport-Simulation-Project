function setText(id, value){
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function formatNumber(num) {
    return typeof num === 'number' ? num.toFixed(2) : '--';
}

function loadResults(){
    fetch('/api/results/lastresult')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch results');
            }
            return response.json();
        })
        .then(data => {

            console.log(data);
            // The endpoint returns a SimulationResult, inside the stats attribute is the statistics
            const statistics = data?.stats;

            if (!statistics) {
                throw new Error('Invalid results format from server');
            }

            // Map fields to HTML elements
            setText("throughput", formatNumber(statistics.hourlyThroughput));

            // Departures
            setText("depAvgWait", formatNumber(statistics.avgWaitTime));
            setText("depMaxQueue", statistics.maxTakeOffQueueSize);
            setText("depMaxDelay", formatNumber(statistics.maxTakeOffDelay));
            setText("depAvgDelay", formatNumber(statistics.avgTakeOffDelay));
            setText("depCancelled", statistics.cancellationCount);

            // Arrivals
            setText("arrAvgHold", formatNumber(statistics.avgHoldingTime));
            setText("arrMaxHolding", statistics.maxHoldingSize);
            setText("arrMaxDelay", formatNumber(statistics.maxArrivalDelay));
            setText("arrAvgDelay", formatNumber(statistics.avgArrivalDelay));
            setText("arrDiverted", statistics.diversionCount);
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
