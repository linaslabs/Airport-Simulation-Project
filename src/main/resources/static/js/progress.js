let stompClient = null;
let startTime;

function updateProgress(progressDecimal) {
    const progressPercentage = progressDecimal * 100;

    // Update progress bar width
    const progressBar = document.getElementById('progressBar');
    if (progressBar) progressBar.style.width = progressPercentage + '%';

    // Update percentage text
    const percentageText = document.getElementById('progressPercentage');
    if (percentageText) percentageText.textContent = Math.round(progressPercentage).toString();

    // Update elapsed time
    const elapsed = Math.floor((Date.now() - startTime) / 1000);
    const timeText = document.getElementById('elapsedTime');
    if (timeText) timeText.textContent = elapsed + 's';
}

function updateGlobalStats(data) {
    let throughput = 0;
    if (data.currentTick > 0) {
        throughput = ((data.totalLanded + data.totalDeparted) / (data.currentTick / 60)).toFixed(1);
    }

    document.getElementById('stat-throughput').textContent = throughput;

    // Arrival Stats
    document.getElementById('stat-landed').textContent = data.totalLanded;
    document.getElementById('stat-avg-hold').textContent = data.avgHoldingTime.toFixed(1);
    document.getElementById('stat-max-hold').textContent = data.maxHoldingSize;
    document.getElementById('stat-arr-max-delay').textContent = data.maxArrivalDelay;
    document.getElementById('stat-arr-avg-delay').textContent = data.avgArrivalDelay.toFixed(1);
    document.getElementById('stat-diversions').textContent = data.diversionCount;

    // Departure Stats
    document.getElementById('stat-departed').textContent = data.totalDeparted;
    document.getElementById('stat-avg-wait').textContent = data.avgWaitTime.toFixed(1);
    document.getElementById('stat-max-takeoff').textContent = data.maxTakeOffQueueSize;
    document.getElementById('stat-dep-max-delay').textContent = data.maxTakeOffDelay;
    document.getElementById('stat-dep-avg-delay').textContent = data.avgTakeOffDelay.toFixed(1);
    document.getElementById('stat-cancellations').textContent = data.cancellationCount;
}

function connectWebSocket() {
    startTime = Date.now();

    // Connect to the websocket endpoint defined in the backend (config/WebSocketConfig)
    const socket = new SockJS('/simulation-websocket');
    stompClient = Stomp.over(socket);

    // Set to null to hide STOMP debug messages in the browser console (can enable if needed)
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket: ' + frame);

        // Subscribe to snapshot stream
        const snapshotSubscription = stompClient.subscribe('/simulation/snapshot', function (message) {
            // Parse the body into a JS object
            const snapshotData = JSON.parse(message.body);

            console.log("Received Snapshot Data:", snapshotData);

            // Extract the progress and update the UI
            updateProgress(snapshotData.progressPercent);

            updateGlobalStats(snapshotData)

            // Update the current tick count display, if present
            const tickCountElement = document.getElementById('tickCount');
            if (tickCountElement && typeof snapshotData.currentTick !== 'undefined') {
                tickCountElement.textContent = snapshotData.currentTick.toString();
            }

            // snapshotData.holdingAircraft and snapshotData.runways can be used here to draw out the real-time simulation
            // ...
        });

        // Subscribe to the summary stream so we know when the simulation finishes
        const summarySubscription = stompClient.subscribe('/simulation/summary', function () {
            console.log('Simulation complete. Redirecting to results...');

            // Close connection
            if (stompClient !== null) {
                stompClient.disconnect();
            }

            // Wait 2 secs for the user to see the bar hit 100% (for UI) then redirect
            setTimeout(() => {
                window.location.href = '/results.html';
            }, 2000);
        });

    }, function(error) {
        console.error('WebSocket Error: ', error);
        // Web socket failure handling can happen here if necessary (maybe fallback to polling?)
    });
}

document.addEventListener('DOMContentLoaded', () => {
    connectWebSocket();
});

// If user leaves page early, clean-up
window.addEventListener('beforeunload', () => {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});