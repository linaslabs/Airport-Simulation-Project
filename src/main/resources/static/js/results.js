function setText(id, value){
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function formatNumber(num) {
    return typeof num === 'number' ? num.toFixed(2) : '--';
}

function toReadableText(rawValue) {
    if (rawValue === null || rawValue === undefined || rawValue === '') {
        return 'unknown';
    }

    const text = String(rawValue).toLowerCase();
    const parts = text.split('_');
    const outputParts = [];

    for (let i = 0; i < parts.length; i++) {
        const part = parts[i];
        if (!part) continue;
        outputParts.push(part.charAt(0).toUpperCase() + part.slice(1));
    }

    return outputParts.join(' ');
}

function formatRawEventLog(rawEvents) {
    const formatted = [];
    if (!Array.isArray(rawEvents)) {
        return formatted;
    }

    const sorted = rawEvents.slice();
    sorted.sort(function (a, b) {
        const tickA = (a && typeof a.tick === 'number') ? a.tick : Number.MAX_SAFE_INTEGER;
        const tickB = (b && typeof b.tick === 'number') ? b.tick : Number.MAX_SAFE_INTEGER;
        return tickA - tickB;
    });

    for (let i = 0; i < sorted.length; i++) {
        const event = sorted[i] || {};
        const tick = (typeof event.tick === 'number') ? event.tick : '--';

        if (typeof event.runwayID === 'number') {
            const runwayLabel = 'Runway ' + (event.runwayID + 1);
            const eventType = toReadableText(event.type);
            const status = toReadableText(event.runwayStatus);
            const mode = toReadableText(event.runwayMode);

            let duration = 'duration: unknown';
            if (typeof event.duration === 'number') {
                if (event.duration < 0) {
                    duration = 'duration: indefinite';
                } else if (event.duration === 0) {
                    duration = 'duration: immediate';
                } else {
                    duration = 'duration: ' + event.duration + ' mins';
                }
            }

            formatted.push('Minute ' + tick + ': ' + runwayLabel + ' ' + eventType + '. Status: ' + status + ', mode: ' + mode + ', ' + duration + '.');
        } else {
            const callsign = event.callsign ? ('aircraft ' + event.callsign) : 'an aircraft';
            const eventType = toReadableText(event.type);
            const status = toReadableText(event.status);
            formatted.push('Minute ' + tick + ': ' + callsign + ' triggered ' + eventType + ' (' + status + ').');
        }
    }

    return formatted;
}

function getEventLogLines(data) {
    if (data && Array.isArray(data.eventLog)) {
        return data.eventLog;
    }

    if (data && data.log && Array.isArray(data.log.eventLog)) {
        return formatRawEventLog(data.log.eventLog);
    }

    return [];
}

function renderEventLog(eventLog) {
    const list = document.getElementById('resultEventLog');
    if (!list) return;

    list.innerHTML = '';

    if (!Array.isArray(eventLog) || eventLog.length === 0) {
        const emptyItem = document.createElement('li');
        emptyItem.className = 'event-log-empty';
        emptyItem.textContent = 'No events logged.';
        list.appendChild(emptyItem);
        return;
    }

    eventLog.forEach(line => {
        const item = document.createElement('li');
        item.textContent = line;
        list.appendChild(item);
    });
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

            renderEventLog(getEventLogLines(data));
        })
        .catch(error => {
            console.error('Error loading results:', error);
            alert("Failed to load simulation results. Please ensure a simulation has completed.");
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
        body: JSON.stringify(resultName)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save results');
            }
            return response.text();
        })
        .then(() => {
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
