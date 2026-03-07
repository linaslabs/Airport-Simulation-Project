let stompClient = null;
let startTime;

let isSimulationPaused = false; // For playback controls

function togglePlayPause(){
    const btn = document.getElementById('btn-play-pause');

    if (isSimulationPaused) {
        // Resume
        fetch('/api/simulation/resume', { method: 'POST' })
            .then(() => {
                btn.innerHTML = '⏸ Pause';
                isSimulationPaused = false;
            });
    } else {
        // Pause
        fetch('/api/simulation/pause', { method: 'POST' })
            .then(() => {
                btn.innerHTML = '▶ Play';
                isSimulationPaused = true;
            });
    }
}

function setSpeed(multiplier){
    fetch(`/api/simulation/speed/${multiplier}`, { method: 'POST'})
        .then(() => console.log(`Speed changed to ${multiplier}x`))

    // Update UI buttons
    const allSpeeds = [1, 5, 20];

    allSpeeds.forEach(speed => {
        const btn = document.getElementById(`btn-speed-${speed}`);
        if (btn) {
            // Mute buttons by ensuring they have the secondary css class
            if (!btn.classList.contains('secondary')) {
                btn.classList.add('secondary');
            }
        }
    });

    // Highlight the clicked button
    const activeBtn = document.getElementById(`btn-speed-${multiplier}`);
    if (activeBtn) {
        activeBtn.classList.remove('secondary');
    }

}

function triggerFastForward() {
    // Disable controls in the event that we have state bugs
    document.getElementById('btn-play-pause').disabled = true;
    document.getElementById('btn-fast-forward').disabled = true;
    document.getElementById('btn-fast-forward').innerHTML = '⏳ Processing...';

    fetch('/api/simulation/fastforward', { method: 'POST' })
        .then(() => console.log('Fast forwarding to end...'));
}

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

function updateHoldingTable(aircraftList) {
    const tbody = document.getElementById('holding-table-body');
    const countBadge = document.getElementById('holding-count');
    if (!tbody || !aircraftList) return; // Don't update if there is no table or list

    countBadge.textContent = aircraftList.length;
    tbody.innerHTML = '';

    if (aircraftList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: #888; padding: 20px;">No aircraft in holding</td></tr>';
        return;
    }

    aircraftList.forEach(aircraft => {
        const isEmergency = aircraft.emergencyStatus && aircraft.emergencyStatus !== 'NONE';
        const row = document.createElement('tr');

        // Styling for emergencies
        if (isEmergency) {
            row.style.backgroundColor = '#ff9494';
            row.style.color = '#dc3545';           // Dark red
            row.style.fontWeight = 'bold';
        }

        // Insert the row
        row.innerHTML = `
            <td>${aircraft.callsign}</td>
            <td>${aircraft.fuelLevel.toFixed(1)}</td>
            <td>${aircraft.emergencyStatus}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateTakeoffTable(aircraftList) {
    const tbody = document.getElementById('takeoff-table-body');
    const countBadge = document.getElementById('takeoff-count');
    if (!tbody || !aircraftList) return; // Don't update if there is no table or list

    countBadge.textContent = aircraftList.length;
    tbody.innerHTML = '';

    if (aircraftList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: #888; padding: 20px;">No aircraft in queue</td></tr>';
        return;
    }

    aircraftList.forEach(aircraft => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td style="font-weight: bold;">${aircraft.callsign}</td>
            <td>Minute ${aircraft.entryTick}</td>
            <td>${aircraft.status}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateRunwayTable(runways) {
    const tbody = document.getElementById('runway-table-body');
    if (!tbody || !runways) return;

    tbody.innerHTML = '';

    // Loop 10 times, if the runway is not used, grey it out
    for (let i = 0; i < 10; i++) {
        const row = document.createElement('tr');

        if (i < runways.length) {
            // Render runway if active
            const runway = runways[i];
            const isOccupied = runway.aircraftCallsign && runway.aircraftCallsign.trim() !== '';
            const aircraftText = isOccupied ? runway.aircraftCallsign : '-';
            const aircraftStyle = isOccupied ? 'font-weight: bold; color: #0056b3;' : 'color: #999;';

            row.innerHTML = `
                <td style="font-weight: bold;">Runway ${runway.runwayID + 1}</td>
                <td>${runway.mode}</td>
                <td>${runway.status}</td>
                <td style="${aircraftStyle}">${aircraftText}</td>
            `;
        } else {
            // Grey out runway
            row.style.backgroundColor = '#efefef';
            row.style.color = '#ccc';
            row.innerHTML = `
                <td>Runway ${i + 1}</td>
                <td>-</td>
                <td>INACTIVE</td>
                <td>-</td>
            `;
        }

        tbody.appendChild(row);
    }
}

async function stopSimulation() {
    // Disable button to stop spam
    const btn = document.getElementById('btn-stop-sim');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = 'Stopping...';
    }

    // Await call to make sure that the backend receives the stop call, and disconnects the simulation
    try {
        await fetch('/api/simulation/stop', { method: 'POST' });

    } catch(err) {
        console.error("Error stopping backend simulation:", err);
    } finally {
        // Setting this flag in storage as true, this is used to reload users configuration after starting sim and stopping
        sessionStorage.setItem('wasAborted', 'true');

        if (typeof stompClient !== 'undefined' && stompClient !== null) {
            stompClient.disconnect(() => {
                window.location.href = '/config.html';
            });
        } else {
            window.location.href = '/config.html';
        }
    }
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
            if (tickCountElement && snapshotData.currentTick != null) {
                tickCountElement.textContent = snapshotData.currentTick.toString();
            }

            updateGlobalStats(snapshotData)
            updateRunwayTable(snapshotData.runways);
            updateHoldingTable(snapshotData.holdingAircraft);
            updateTakeoffTable(snapshotData.takeoffAircraft);

            // snapshotData.holdingAircraft and snapshotData.runways can be used here to draw out the real-time simulation
            // ...
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

            // Wait half a second for the user to see the bar hit 100% (for UI) then redirect
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