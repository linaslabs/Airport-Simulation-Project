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

function openSaveModal(){
    const modal = document.getElementById("saveModal");
    const resultNameInput = document.getElementById("resultName");
    if (modal) {
        modal.style.display = "flex";
        resultNameInput.focus();
        resultNameInput.value = ""; // Clear previous input
    }
}

function closeSaveModal(){
    const modal = document.getElementById("saveModal");
    if (modal) modal.style.display = "none";
}

function saveResults(){
    const resultName = document.getElementById("resultName").value.trim();
    if (!resultName) {
        alert("Please enter a simulation result name");
        return;
    }

    // POST to server with the result name
    fetch('/api/results/save', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: resultName })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save results');
            }
            return response.json();
        })
        .then(data => {
            alert("Saved successfully: " + resultName);
            closeSaveModal();
        })
        .catch(error => {
            console.error('Error saving results:', error);
            alert("Failed to save results");
        });
}


// Wire buttons + init
document.addEventListener("DOMContentLoaded", () => {
    loadResults();

    const saveBtn = document.getElementById("saveBtn");
    const cancelBtn = document.getElementById("cancelBtn");
    const confirmSaveBtn = document.getElementById("confirmSaveBtn");
    const modal = document.getElementById("saveModal");
    const resultNameInput = document.getElementById("resultName");

    if (saveBtn) saveBtn.addEventListener("click", openSaveModal);
    if (cancelBtn) cancelBtn.addEventListener("click", closeSaveModal);
    if (confirmSaveBtn) confirmSaveBtn.addEventListener("click", saveResults);

    // Allow Enter key to submit
    if (resultNameInput) {
        resultNameInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") saveResults();
        });
    }

    // Close modal when clicking outside of it
    if (modal) {
        window.addEventListener("click", (e) => {
            if (e.target === modal) closeSaveModal();
        });
    }
});
