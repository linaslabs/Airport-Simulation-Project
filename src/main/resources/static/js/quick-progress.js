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

            // Update the current tick count display, if present
            const tickCountElement = document.getElementById('tickCount');
            if (tickCountElement && typeof snapshotData.currentTick !== 'undefined') {
                tickCountElement.textContent = snapshotData.currentTick.toString();
            }
        });

        // Subscribe to the complete stream so we know when the simulation finishes
        const completeSubscription = stompClient.subscribe('/simulation/complete', function () {
            console.log('Simulation complete. Redirecting to results...');

            // Simulation 100 percent completion
            updateProgress(1.0);

            // Close connection
            if (stompClient !== null) {
                stompClient.disconnect();
            }

            // Wait half a second secs for the user to see the bar hit 100% (for UI) then redirect
            setTimeout(() => {
                window.location.href = '/results.html';
            }, 500);
        });

        // Tell the backend when we're ready to start.
        stompClient.send("/app/simulation/ready", {}, "");
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