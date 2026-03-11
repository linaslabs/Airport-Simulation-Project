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
    if (!tbody || !aircraftList) return;

    countBadge.textContent = aircraftList.length;
    tbody.innerHTML = '';

    if (aircraftList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #94a3b8; font-weight: 600; padding: 15px;">No aircraft in holding</td></tr>';
        return;
    }

    // Loop through each aircraft and apply the table row to match their details
    aircraftList.forEach((aircraft, index) => {
        const isEmergency = aircraft.emergencyStatus && aircraft.emergencyStatus !== 'NONE';
        const row = document.createElement('tr');

        let lockClass = '';
        let disableAttribute = '';

        if (isEmergency) {
            row.classList.add('emergency-row');

            if (aircraft.emergencySource === 'SCHEDULED') {
                lockClass = 'lock-scheduled';
                disableAttribute = 'disabled title="Scheduled emergency"';
            } else if (aircraft.emergencySource === 'RANDOM') {
                lockClass = 'lock-random';
                disableAttribute = 'disabled title="Random natural emergency"';
            } else {
                disableAttribute = 'disabled title="Manually triggered emergency"';
            }
        }

        const altitude = (index + 1) * 1000;

        row.innerHTML = `
            <td class="text-bold-dark">${aircraft.callsign}</td>
            <td><div class="table-val-box text-bold-slate">${altitude} ft</div></td>
            <td><div class="table-val-box text-bold-slate">${aircraft.fuelLevel.toFixed(1)}</div></td>
            <td>
                <select class="${lockClass}" onchange="changeAircraftEmergency('${aircraft.callsign}', this.value)" ${disableAttribute}>
                    <option value="NONE" ${aircraft.emergencyStatus === 'NONE' ? 'selected' : ''}>None</option>
                    <option value="FUEL" ${aircraft.emergencyStatus === 'FUEL' ? 'selected' : ''}>Fuel</option>
                    <option value="MECHANICAL" ${aircraft.emergencyStatus === 'MECHANICAL' ? 'selected' : ''}>Mechanical</option>
                    <option value="PASSENGER" ${aircraft.emergencyStatus === 'PASSENGER' ? 'selected' : ''}>Health</option>
                </select>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function updateTakeoffTable(aircraftList, currentTick, cancellationThreshold) {
    const tbody = document.getElementById('takeoff-table-body');
    const countBadge = document.getElementById('takeoff-count');
    if (!tbody || !aircraftList) return;

    countBadge.textContent = aircraftList.length;
    tbody.innerHTML = '';

    if (aircraftList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #94a3b8; font-weight: 600; padding: 15px;">No aircraft in queue</td></tr>';
        return;
    }

    // Loop through each aircraft and apply the table row to match their details
    aircraftList.forEach((aircraft, index) => {
        const row = document.createElement('tr');
        const waitTime = currentTick - aircraft.entryTick;

        let displayState = "Waiting";
        let stateClass = "text-bold-slate";

        if (index === 0) {
            displayState = "Next Departure";
            stateClass = "text-success";
        }

        if (cancellationThreshold > 0 && waitTime >= (cancellationThreshold - 5)) {
            displayState = "Cancellation Risk";
            stateClass = "text-danger";
        }

        row.innerHTML = `
            <td class="text-bold-dark">${aircraft.callsign}</td>
            <td><div class="table-val-box text-bold-slate">Tick ${aircraft.entryTick}</div></td>
            <td><div class="table-val-box text-bold-slate">${waitTime} mins</div></td>
            <td><div class="table-val-box ${stateClass}">${displayState}</div></td>
        `;
        tbody.appendChild(row);
    });
}

function updateRunwayTable(runways, currentTick) {
    const tbody = document.getElementById('runway-table-body');
    if (!tbody || !runways) return;

    tbody.innerHTML = '';

    // Loop through each runway and apply the table row to match its details
    for (let i = 0; i < 10; i++) {
        const row = document.createElement('tr');

        if (i < runways.length) {
            const runway = runways[i];
            const isOccupied = runway.aircraftCallsign && runway.aircraftCallsign.trim() !== '';

            const aircraftText = isOccupied ? runway.aircraftCallsign : '-';
            const aircraftClass = isOccupied ? 'text-bold-blue' : 'text-bold-muted';
            const occupiedText = isOccupied && runway.occupiedUntil > currentTick ? `Tick ${runway.occupiedUntil}` : '-';

            let lockClass = '';
            let lockAttribute = '';

            if (runway.lockSource === 'SCHEDULED') {
                lockClass = 'lock-scheduled';
                lockAttribute = 'disabled title="Locked by pre-scheduled event"';
            } else if (runway.lockSource === 'RANDOM') {
                lockClass = 'lock-random';
                lockAttribute = 'disabled title="Locked by random natural event"';
            }

            row.innerHTML = `
                <td class="text-bold-dark">Runway ${runway.runwayID + 1}</td>
                <td>
                    <select class="mode-select ${lockClass}" onchange="changeRunwayMode(${runway.runwayID}, this.value)" ${lockAttribute}>
                        <option value="LANDING" ${runway.mode === 'LANDING' ? 'selected' : ''}>Landing</option>
                        <option value="TAKEOFF" ${runway.mode === 'TAKEOFF' ? 'selected' : ''}>Take-Off</option>
                        <option value="MIXED" ${runway.mode === 'MIXED' ? 'selected' : ''}>Mixed</option>
                    </select>
                </td>
                <td>
                    <select class="status-select ${lockClass}" onchange="changeRunwayStatus(${runway.runwayID}, this.value)" ${lockAttribute}>
                        <option value="AVAILABLE" ${runway.status === 'AVAILABLE' ? 'selected' : ''}>Available</option>
                        <option value="INSPECTION" ${runway.status === 'INSPECTION' ? 'selected' : ''}>Inspection</option>
                        <option value="SNOW_CLEARANCE" ${runway.status === 'SNOW_CLEARANCE' ? 'selected' : ''}>Snow Clearance</option>
                        <option value="EQUIPMENT_FAILURE" ${runway.status === 'EQUIPMENT_FAILURE' ? 'selected' : ''}>Equipment Failure</option>
                    </select>
                </td>
                <td class="${aircraftClass}">${aircraftText}</td>
                <td><div class="table-val-box text-bold-slate">${occupiedText}</div></td>
            `;
        } else {
            row.style.backgroundColor = '#f8fafc';
            row.innerHTML = `
                <td class="text-bold-muted">Runway ${i + 1}</td>
                <td><div class="table-val-box text-muted">-</div></td>
                <td><div class="table-val-box text-muted">INACTIVE</div></td>
                <td class="text-bold-muted">-</td>
                <td><div class="table-val-box text-muted">-</div></td>
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

            updateGlobalStats(snapshotData);
            updateRunwayTable(snapshotData.runways, snapshotData.currentTick);
            updateHoldingTable(snapshotData.holdingAircraft);
            updateTakeoffTable(snapshotData.takeoffAircraft, snapshotData.currentTick, snapshotData.cancellationThreshold);

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

function changeRunwayMode(runwayId, newMode) {
    fetch(`/api/simulation/runway/${runwayId}/mode?mode=${newMode}`, { method: 'POST' })
        .then(response => {
            if(!response.ok) console.error('Failed to change runway mode');
        });
}

function changeRunwayStatus(runwayId, newStatus) {
    fetch(`/api/simulation/runway/${runwayId}/status?status=${newStatus}`, { method: 'POST' })
        .then(response => {
            if(!response.ok) console.error('Failed to change runway status');
        });
}

function changeAircraftEmergency(callsign, newStatus) {
    fetch(`/api/simulation/aircraft/${callsign}/emergency?status=${newStatus}`, { method: 'POST' })
        .then(response => {
            if(!response.ok) console.error('Failed to change aircraft emergency status');
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