function setText(id, value){
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

/**
 * Format a numeric result with two decimal places.
 *
 * @param {*} num Raw numeric value.
 * @returns {string} Formatted number or '--'.
 */
function formatNumber(num) {
    return typeof num === 'number' ? num.toFixed(2) : '--';
}

/**
 * Convert enum-style values into readable labels for the event log.
 *
 * @param {string|null|undefined} rawValue Raw enum-like text.
 * @returns {string} Human-readable label or 'unknown'.
 */
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

/**
 * Format raw backend event log entries into readable timeline lines.
 *
 * @param {Array<Object>|null|undefined} rawEvents Raw event log entries.
 * @returns {string[]} Formatted event log lines.
 */
function formatRawEventLog(rawEvents) {
    const formatted = [];
    if (!Array.isArray(rawEvents)) {
        return formatted;
    }

    // Sort first so the rendered timeline always reads in chronological order.
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
            // Runway events carry status and mode details, so the output sentence is richer here.
            const runwayLabel = 'Runway ' + (event.runwayID + 1);
            const eventType = toReadableText(event.type);
            const status = toReadableText(event.runwayStatus);
            const mode = toReadableText(event.runwayMode);

            let duration = 'duration: unknown';
            if (typeof event.duration === 'number') {
                if (event.duration < 0) {
                    duration = 'duration: indefinite';
                } else if (event.duration === 0) {
                    duration = 'duration: unspecified';
                } else {
                    duration = 'duration: ' + event.duration + ' mins';
                }
            }

            formatted.push('Minute ' + tick + ': ' + runwayLabel + ' ' + eventType + '. Status: ' + status + ', mode: ' + mode + ', ' + duration + '.');
        } else {
            const callsign = event.callsign ? ('Aircraft ' + event.callsign) : 'an aircraft';
            const eventType = toReadableText(event.type);
            const status = toReadableText(event.status);
            formatted.push('Minute ' + tick + ': ' + callsign + ' triggered ' + eventType + ' (' + status + ').');
        }
    }

    return formatted;
}

/**
 * Reconstruct a best-effort event log from the saved config when no log was persisted.
 *
 * @param {Object|null|undefined} config Saved simulation config.
 * @returns {string[]} Timeline lines built from scheduled events.
 */
function buildFallbackEventLogFromConfig(config) {
    const lines = [];

    if (!config) {
        return lines;
    }

    const scheduled = [];
    const runwaySettings = Array.isArray(config.runwaySettings) ? config.runwaySettings : [];

    function getInitialRunwaySetting(runwayId) {
        if (!Number.isInteger(runwayId)) return null;

        // Prefer an explicit runwayID match so we do not rely only on array order.
        for (let i = 0; i < runwaySettings.length; i++) {
            const setting = runwaySettings[i];
            if (setting && Number.isInteger(setting.runwayID) && setting.runwayID === runwayId) {
                return setting;
            }
        }

        if (runwayId >= 0 && runwayId < runwaySettings.length) {
            return runwaySettings[runwayId] || null;
        }

        return null;
    }

    const runwayEventsByRunway = config.scheduledRunwayEvents || {};
    Object.entries(runwayEventsByRunway).forEach(function ([runwayKey, events]) {
        if (!Array.isArray(events)) return;

        events.forEach(function (event) {
            // Missing ticks are pushed to the end so incomplete data still renders safely.
            const tick = Number.isFinite(event && event.tick) ? event.tick : Number.MAX_SAFE_INTEGER;
            const runwayId = Number.isInteger(event && event.runwayID) ? event.runwayID : Number.parseInt(runwayKey, 10);
            const eventTypeRaw = event && event.type ? String(event.type).toUpperCase() : '';

            scheduled.push({
                tick: tick,
                category: 'runway',
                event: event || {},
                runwayId: Number.isInteger(runwayId) ? runwayId : null
            });

            if (Number.isFinite(event && event.duration) && event.duration > 0 && eventTypeRaw !== 'REVERSION') {
                const initialSetting = getInitialRunwaySetting(runwayId);
                // Add the matching reversion point so temporary runway changes have a visible end.
                scheduled.push({
                    tick: tick + event.duration,
                    category: 'runway-reversion',
                    runwayId: Number.isInteger(runwayId) ? runwayId : null,
                    runwayStatus: initialSetting ? initialSetting.status : null,
                    runwayMode: initialSetting ? initialSetting.mode : null
                });
            }
        });
    });

    const aircraftEventsByCallsign = config.scheduledAircraftEvents || {};
    Object.entries(aircraftEventsByCallsign).forEach(function ([callsignKey, events]) {
        if (!Array.isArray(events)) return;

        events.forEach(function (event) {
            const tick = Number.isFinite(event && event.tick) ? event.tick : Number.MAX_SAFE_INTEGER;
            // Keep the callsign near the event so the output stays readable even if the payload shape varies.
            scheduled.push({
                tick: tick,
                category: 'aircraft',
                event: event || {},
                callsign: (event && event.callsign) ? event.callsign : callsignKey
            });
        });
    });

    scheduled.sort(function (a, b) {
        return a.tick - b.tick;
    });

    for (let i = 0; i < scheduled.length; i++) {
        const item = scheduled[i];
        const tick = item.tick === Number.MAX_SAFE_INTEGER ? '--' : item.tick;

        if (item.category === 'runway') {
            const runwayLabel = item.runwayId === null ? 'Runway ?' : 'Runway ' + (item.runwayId + 1);
            const eventType = toReadableText(item.event.type);
            const status = toReadableText(item.event.runwayStatus);
            const mode = toReadableText(item.event.runwayMode);

            let durationText = 'duration: unknown';
            if (typeof item.event.duration === 'number') {
                if (item.event.duration < 0) {
                    durationText = 'duration: indefinite';
                } else if (item.event.duration === 0) {
                    durationText = 'duration: immediate';
                } else {
                    durationText = 'duration: ' + item.event.duration + ' mins';
                }
            }

            lines.push('Minute ' + tick + ': ' + runwayLabel + ' ' + eventType + '. Status: ' + status + ', mode: ' + mode + ', ' + durationText + '.');
        } else if (item.category === 'runway-reversion') {
            const runwayLabel = item.runwayId === null ? 'Runway ?' : 'Runway ' + (item.runwayId + 1);
            const status = toReadableText(item.runwayStatus);
            const mode = toReadableText(item.runwayMode);
            lines.push('Minute ' + tick + ': ' + runwayLabel + ' Reversion. Status: ' + status + ', mode: ' + mode + ', duration: immediate.');
        } else {
            const callsign = item.callsign ? ('aircraft ' + item.callsign) : 'an aircraft';
            const eventType = toReadableText(item.event.type);
            const status = toReadableText(item.event.status);
            lines.push('Minute ' + tick + ': ' + callsign + ' ' + eventType + ' (' + status + ').');
        }
    }

    return lines;
}

/**
 * Resolve the best event log source available for the results page.
 *
 * @param {Object|null|undefined} data Result payload from the backend.
 * @returns {string[]} Event log lines ready for display.
 */
function getEventLogLines(data) {
    if (data && Array.isArray(data.eventLog) && data.eventLog.length > 0) {
        // Best case: the saved result already contains ready-to-render lines.
        return data.eventLog;
    }

    if (data && data.log && Array.isArray(data.log.eventLog)) {
        // Next best option: format the raw logger payload on the client.
        const formatted = formatRawEventLog(data.log.eventLog);
        if (formatted.length > 0) {
            return formatted;
        }
    }

    // Final fallback: rebuild a readable timeline from the scheduled config.
    return buildFallbackEventLogFromConfig(data && data.config);
}

/**
 * Render the event log list in the results page.
 *
 * @param {string[]|null|undefined} eventLog Event log lines.
 * @returns {void}
 */
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
        // Render as plain text so log lines are never treated as HTML.
        item.textContent = line;
        list.appendChild(item);
    });
}

/**
 * Load the latest simulation result and fill the page metrics.
 *
 * @returns {void}
 */
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
            // The endpoint returns a SimulationResult; the numeric metrics live inside stats.
            const statistics = data?.stats;

            if (!statistics) {
                throw new Error('Invalid results format from server');
            }

            // Fill the summary cards first.
            setText("throughput", formatNumber(statistics.hourlyThroughput));

            // Departure metrics.
            setText("depAvgWait", formatNumber(statistics.avgWaitTime));
            setText("depMaxQueue", statistics.maxTakeOffQueueSize);
            setText("depMaxDelay", formatNumber(statistics.maxTakeOffDelay));
            setText("depAvgDelay", formatNumber(statistics.avgTakeOffDelay));
            setText("depCancelled", statistics.cancellationCount);

            // Arrival metrics.
            setText("arrAvgHold", formatNumber(statistics.avgHoldingTime));
            setText("arrMaxHolding", statistics.maxHoldingSize);
            setText("arrMaxDelay", formatNumber(statistics.maxArrivalDelay));
            setText("arrAvgDelay", formatNumber(statistics.avgArrivalDelay));
            setText("arrDiverted", statistics.diversionCount);

            // The event log may come from saved text, raw logger data, or a config-based fallback.
            renderEventLog(getEventLogLines(data));
        })
        .catch(error => {
            console.error('Error loading results:', error);
            alert("Failed to load simulation results. Please ensure a simulation has completed.");
        });
}

/**
 * Open the save-results modal and reset the input field.
 *
 * @returns {void}
 */
function openSaveModal(){
    const modal = document.getElementById("saveModal");
    const resultNameInput = document.getElementById("resultName");
    if (modal) {
        modal.style.display = "flex";
        resultNameInput.focus();
        resultNameInput.value = ""; // Clear previous input
    }
}

/**
 * Close the save-results modal.
 *
 * @returns {void}
 */
function closeSaveModal(){
    const modal = document.getElementById("saveModal");
    if (modal) modal.style.display = "none";
}

/**
 * Save the current result under the user-provided name.
 *
 * @returns {void}
 */
function saveResults(){
    const resultName = document.getElementById("resultName").value.trim();
    if (!resultName) {
        alert("Please enter a simulation result name");
        return;
    }

    // The backend expects the raw name string as JSON.
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


/**
 * Initialise the results page controls and modal behaviour.
 */
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

    // Let Enter act like the confirm button while the input is focused.
    if (resultNameInput) {
        resultNameInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") saveResults();
        });
    }

    // Clicking the backdrop closes the modal, matching the rest of the UI behaviour.
    if (modal) {
        window.addEventListener("click", (e) => {
            if (e.target === modal) closeSaveModal();
        });
    }
});
