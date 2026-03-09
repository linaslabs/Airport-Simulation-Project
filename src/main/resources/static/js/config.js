const runwayCounter = document.getElementById("runway-counter");
const add1Btn = document.getElementById("add-button");
const minus1Btn = document.getElementById("minus-button");
const runwayList = document.getElementById("runway-list");
const template = document.getElementById("runway-template");
let invalidEventIds = new Set();

let number = 1;

function add1(){
    if (number < 10) {
        number++;
        addRunwayRow(number); // adds new runways column
    }
    runwayCounter.innerHTML = number.toString() // updates number n in input parameter  (- n +)
    updateEventForm(); // relays the update to the events section
}

function minus1(){
    if (number > 1) {
        removeRow(number); // removes last runway

        number--;
    }
    runwayCounter.innerHTML = number.toString() // updates number n in input parameter (- n +)
    updateEventForm(); // relays the update to the events section
}



function addRunwayRow(id) {

    // clone the HTML template for runway rows
    const clone = template.content.cloneNode(true);

    // replace generic id, and the label, status and mode names to be based on runway number (or id)
    const rowDiv = clone.querySelector(".runway-row");
    rowDiv.id = "runway-row-" + id;

    const label = clone.querySelector(".runway-label");
    label.innerText = "Runway " + id;

    const statusSelect = clone.querySelector(".runway-status");
    statusSelect.name = "status_" + id;
    statusSelect.id = "status_" + id;

    const modeSelect = clone.querySelector(".runway-mode");
    modeSelect.name = "mode_" + id;
    modeSelect.id = "mode_" + id;

    runwayList.appendChild(clone); // add the updated clone to the page
}

function removeRow(id) {
    // find the row with the id (runway number) to be deleted
    const rowToDelete = document.getElementById("runway-row-" + id);

    if (rowToDelete) {// make sure the row exists
        rowToDelete.remove();
    }
}

// create first row, because default name is 1 for noe
for (let i = 1; i <= 1; i++) {
    addRunwayRow(i);
}



add1Btn.onclick = add1
minus1Btn.onclick = minus1

// INPUT CONFIG
// format for inputs with text boxes
const inputConfig = [
    { label: "Inbound Rate /hr",           name: "inbound_rate",      range: "0 - 100",     min: 0,  max: 100,     step: 1,    val: 15 },
    { label: "Outbound Rate /hr",          name: "outbound_rate",     range: "0 - 100",     min: 0,  max: 100,     step: 1,    val: 15 },
    { label: "Simulation Duration (mins)", name: "sim_duration",      range: "60 - 1440",   min: 60, max: 1440,    step: 1,    val: 120 },
    { label: "Max Delay Time (mins)",      name: "max_delay",         range: "0 - 60",      min: 0,  max: 60,      step: 1,    val: 30 },
    { label: "Simulation Seed",            name: "seed",              range: "0 - 10^8",    min: 0,  max: 100000000,step: 1,    val: 0 },
    { label: "Mechanical Failure Rate",    name: "mech_failure_rate", range: "0.00 - 0.10", min: 0,  max: 0.1,     step: 0.01, val: 0.00 },
    { label: "Health Issue Rate",          name: "health_issue_rate", range: "0.00 - 0.10", min: 0,  max: 0.1,     step: 0.01, val: 0.00 },
    { label: "Runway Inspection Rate",     name: "inspection_rate",   range: "0.00 - 0.10", min: 0,  max: 0.1,     step: 0.01, val: 0.00 },
    { label: "Snow Clearance Rate",        name: "snow_rate",         range: "0.00 - 0.10", min: 0,  max: 0.1,     step: 0.01, val: 0.00 },
    { label: "Equipment Failure Rate",     name: "equip_failure_rate",range: "0.00 - 0.10", min: 0,  max: 0.1,     step: 0.01, val: 0.00 },
];
// generate input params
function generateInputs() {
    const mainContainer = document.getElementById("input-parameters-list");
    const rateContainer = document.getElementById("rate-parameters-list");
    const template = document.getElementById("input-field-template");

    inputConfig.forEach(config => {
        const clone = template.content.cloneNode(true);
        const input = clone.querySelector(".field-input");

        clone.querySelector(".field-label").innerText = config.label;
        clone.querySelector(".field-range").innerText = "Range: " + config.range;

        input.id = "input-" + config.name;
        input.name = config.name;
        input.value = config.val;

        if (config.min !== undefined) input.min = config.min;
        if (config.max !== undefined) input.max = config.max;
        if (config.step !== undefined) input.step = config.step;

        input.addEventListener('keydown', function(e) {
            if (['e', 'E', '+', '-'].includes(e.key)) {
                e.preventDefault();
            }
        });

        if (config.name === "seed") {
            clone.querySelector(".dice-btn").style.display = "block";
        }

        // Rate fields go to rate container, everything else to main
        const rateFields = ['mech_failure_rate', 'health_issue_rate', 'inspection_rate', 'snow_rate', 'equip_failure_rate'];
        if (rateFields.includes(config.name)) {
            rateContainer.appendChild(clone);
        } else {
            mainContainer.appendChild(clone);
        }
    });
}

function setupRateToggle() {
    const rateFields = ['input-mech_failure_rate', 'input-health_issue_rate',
        'input-inspection_rate', 'input-snow_rate', 'input-equip_failure_rate'];

    const checkbox = document.getElementById('input-auto_gen');

    function updateRates() {
        rateFields.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            if (checkbox.checked) {
                el.disabled = false;
                el.style.opacity = '1';
            } else {
                el.disabled = false; // temporarily enable to set value
                el.value = '0.00';
                el.disabled = true;
                el.style.opacity = '0.4';
            }
        });
    }

    checkbox.addEventListener('change', updateRates);
    updateRates(); // run on page load
}

function openSaveModal() {
    // Clear the input field for a fresh start
    document.getElementById("new-config-name").value = "";

    // Show the modal using Bootstrap's JS API
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('saveConfigModal'));
    modal.show();
}



function startSimulation() {
    // HALT if validation fails!
    if (!validateInputs()) return;

    // input params data
    let inputParams = {};
    const inputs = document.querySelectorAll('input[type="number"]');

    inputs.forEach(input => {
        // Example: id="num_runways" -> inputParams["num_runways"] = 10
        if (input.id) {
            inputParams[input.id] = parseFloat(input.value);
        }
    });

    // Runways
    let runwayData = [];
    const runwayCount = parseInt(runwayCounter.innerText);

    // Note: statusToEnum and modeToEnum are global functions in your file
    for (let i = 1; i <= runwayCount; i++) {
        const statusVal = document.getElementById(`status_${i}`).value;
        const modeVal = document.getElementById(`mode_${i}`).value;

        runwayData.push({
            runwayID: i - 1,  // Java expects 0-based index
            status: statusToEnum(statusVal),
            mode: modeToEnum(modeVal)
        });
    }

    const processedEvents = formatEventsForBackend(scheduledEventsData);

    const payload = {
        runwaySettings: runwayData,
        scheduledRunwayEvents: processedEvents.runways,
        scheduledAircraftEvents: processedEvents.aircraft,
        simulationID: Date.now().toString(),

        // Logic Configuration
        automaticGenerationEnabled: document.getElementById("input-auto_gen")?.checked,
        seed: parseInt(document.getElementById("input-seed")?.value),
        tickTime: 1000,

        // Parameters
        inboundRate: parseInt(document.getElementById("input-inbound_rate").value),
        outboundRate: parseInt(document.getElementById("input-outbound_rate").value),
        maxWaitTime: parseInt(document.getElementById("input-max_delay").value), // maybe could allow forr indefintie wait time
        duration: parseInt(document.getElementById("input-sim_duration").value),

        // Statistical Rates (@NotNull in Java)
        mechanicalFailureRate: parseFloat(document.getElementById("input-mech_failure_rate")?.value),
        passengerHealthIssueRate: parseFloat(document.getElementById("input-health_issue_rate")?.value) ,
        runwayInspectionRate: parseFloat(document.getElementById("input-inspection_rate")?.value),
        snowClearanceRate: parseFloat(document.getElementById("input-snow_rate")?.value),
        equipmentFailureRate: parseFloat(document.getElementById("input-equip_failure_rate")?.value),

        // Force to uppercase to match the Java Enum perfectly!
        simulationMode: document.getElementById("simulation-mode").value.toUpperCase()
    };

    sessionStorage.setItem('draftConfig', JSON.stringify(payload));

    console.log("Sending Payload:", payload); // Debug check

    fetch('/api/configuration/validate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(async response => {
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `Failed to validate configuration (HTTP ${response.status})`);
            }
            return response.text();
        })
        .then(data => {
            console.log('Configuration validated:', data);
            // Now start the simulation
            return fetch('/api/simulation/initialise', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });
        })
        .then(async response => {
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `Failed to start simulation (HTTP ${response.status})`);
            }
            return response.text();
        })
        .then(data => {
            console.log('Simulation started:', data);
            // Redirect to progress page
            const simMode = payload.simulationMode

            if (simMode == "QUICK_SIM"){
                window.location.href = '/quick-progress.html';
            } else if(simMode == "TABLE_VIEW"){
                window.location.href = '/tabular-progress.html';
            } else {
                window.location.href = '/graphical-progress.html';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("Error: " + error.message);
        });
}


function generateRandomSeed() {
    // Generates a random number
    const randomSeed = Math.floor(Math.random() * 100000000)
    document.getElementById("input-seed").value = randomSeed;
}


// initialisation
generateInputs();
setupRateToggle();


// EVENTS STUFF - SPRINT 2
const eventOptions = {
    "Runway": ["No Change", "Available", "Runway Inspection", "Snow Clearance", "Equipment Failure"],
    "Aircraft": ["Mechanical Failure", "Passenger Health"]
};


// 2. The Event Builder (No Duration gathered or saved)
function addEvent() {
    const typeSelect = document.getElementById("new-event-type");
    const nameSelect = document.getElementById("new-event-name");
    const locSelect = document.getElementById("new-event-loc");
    const timeInput = document.getElementById("new-event-time");
    const durationInput = document.getElementById("new-event-duration");
    const list = document.getElementById("scheduled-events-list");
    const emptyMsg = document.getElementById("empty-list-msg");
    const simInput = document.querySelector('input[id="input-sim_duration"]');

    const eventType = typeSelect.value;
    const eventEnum = nameSelect.value;
    const displayLabel = nameSelect.options[nameSelect.selectedIndex].text;
    const eventLocText = locSelect.options[locSelect.selectedIndex].text;
    const eventLocId = locSelect.value;

    const startTime = parseInt(timeInput.value);
    const simLimit = (simInput && simInput.value !== "") ? parseInt(simInput.value) : 999999;

    // --- 1. TIME VALIDATION ---
    if (isNaN(startTime) || startTime < 0) {
        alert("Please enter a valid start time (0 or greater).");
        timeInput.focus();
        return;
    }
    if (startTime > simLimit) {
        alert(`Error: Cannot schedule past the simulation limit (${simLimit}m).`);
        timeInput.focus();
        return;
    }

    const modeSelect = document.getElementById("new-event-mode");
    let rawMode = modeSelect.value;
    const eventModeVal = eventType === "Runway" ? (rawMode === "null" ? null : rawMode) : "MIXED";
    const eventModeLabel = eventType === "Runway" ? modeSelect.options[modeSelect.selectedIndex].text : "";

    // --- 2. DURATION VALIDATION ---
    let eventDuration;
    if (eventType === "Aircraft") {
        eventDuration = -1; // Aircraft events never have a duration
    } else {
        // If left blank, send -1 (Indefinite)
        eventDuration = durationInput.value === "" ? -1 : parseInt(durationInput.value);

        // Prevent 0 or negative typed durations
        if (eventDuration !== -1 && eventDuration <= 0) {
            alert("Duration must be at least 1 minute, or left blank for Indefinite.");
            durationInput.focus();
            return;
        }

        if (eventDuration !== -1 && eventDuration > 1440) {
            alert("Duration cannot exceed 1440 minutes.");
            durationInput.focus();
            return;
        }
    }
    if (eventType === "Runway") {
        const newStart = startTime;
        const newEnd = eventDuration === -1 ? Infinity : startTime + eventDuration;

        const hasOverlap = scheduledEventsData.some(ev => {
            // We only care about events on the EXACT SAME runway
            if (ev.type !== "Runway" || ev.locationId !== eventLocId) return false;

            const existingStart = parseInt(ev.time);
            const existingEnd = parseInt(ev.duration) === -1 ? Infinity : existingStart + parseInt(ev.duration);

            // The absolute mathematical rule for overlapping time ranges:
            return (newStart < existingEnd) && (existingStart < newEnd);
        });

        if (hasOverlap) {
            alert(`Error: This event overlaps with an existing event on ${eventLocText}.`);
            return; // HALT!
        }
    }

    const uniqueId = Date.now();

    // Data array gets the Enum for the DTO
    scheduledEventsData.push({
        id: uniqueId,
        type: eventType,
        name: eventEnum,
        locationId: eventLocId,
        time: startTime,
        duration: eventDuration,
        mode: eventModeVal,
        modeLabel: eventModeLabel
    });

    if (emptyMsg) emptyMsg.remove();

    const row = document.createElement("div");
    row.className = "d-flex align-items-center justify-content-between small mb-2 pb-2 border-bottom pe-1";
    row.setAttribute("data-id", uniqueId.toString());

    // --- 3. FORMATTING FOR UI ---
    // Calculate End Display
    let durationDisplay = eventDuration === -1 ? "∞" : `${startTime + eventDuration}m`;

    // Always show badge, default to "No Change"
    let modeBadge = eventType === "Runway" ? `<span class="badge bg-info text-dark ms-1">${eventModeLabel || 'No Change'}</span>` : "";

    // Shorten Labels
    let shortLabel = displayLabel.replace("Equipment Failure", "Equip. Failure").replace("Runway Inspection", "Inspection");

    row.innerHTML = `
    <div class="ps-1 pe-2" style="flex: 1; min-width: 0;">
        <div class="d-flex flex-wrap align-items-center">
            <strong class="me-1">${shortLabel}</strong> ${modeBadge}
        </div>
        <span class="text-muted d-block" style="font-size: 0.75rem;">on ${eventLocText}</span>
    </div>
    <div class="text-end flex-shrink-0 px-2 d-flex align-items-center justify-content-end" style="min-width: 95px;">
        <div class="fw-bold text-dark text-nowrap" style="font-size: 0.85rem;">${startTime}m - ${durationDisplay}</div>
    </div>
    <div class="flex-shrink-0" style="width: 25px; text-align: right;">
        <button class="btn btn-link text-danger p-0 border-0 fs-5" onclick="deleteEvent(${uniqueId})">&times;</button>
    </div>
    `;

    list.appendChild(row);
    list.scrollTop = list.scrollHeight;

    timeInput.value = "";
    durationInput.value = "";
    modeSelect.value = "null";
    nameSelect.value = "No Change";
    nameSelect.disabled = false;
    modeSelect.disabled = false;
}


let scheduledEventsData = [];

function deleteEvent(idToDelete) {
    // keep everything that does not match the ID
    scheduledEventsData = scheduledEventsData.filter(item => item.id !== idToDelete);

    console.log("Deleted. Remaining Data:", scheduledEventsData);

    // find the row by its data-id attribute
    const rowToRemove = document.querySelector(`div[data-id="${idToDelete}"]`);

    if (rowToRemove) {
        rowToRemove.remove();
    }

    const list = document.getElementById("scheduled-events-list");

    // check if list is empty - if so add empty message
    if (list.children.length === 0) {
        const emptyMsg = document.createElement("div");
        emptyMsg.id = "empty-list-msg";
        emptyMsg.className = "text-center text-muted small mt-5";
        emptyMsg.innerText = "No events scheduled";
        list.appendChild(emptyMsg);
    }
}

function statusToEnum(statusVal) {
    const mapping = {
        'available': 'AVAILABLE',
        'snowclearance': 'SNOW_CLEARANCE', // Updated: was SNOWCLEARANCE
        'inspection': 'INSPECTION',
        'failure': 'EQUIPMENT_FAILURE'     // Updated: was FAILURE
    };
    return mapping[statusVal.toLowerCase()] || 'AVAILABLE';
}

// Helper function to convert mode dropdown value to enum
function modeToEnum(modeVal) {
    const mapping = {
        'mixed': 'MIXED',
        'landing': 'LANDING',
        'takeoff': 'TAKEOFF'
    };
    return mapping[modeVal.toLowerCase()] || 'MIXED';
}

// HELPER FUNCTION: Turns our frontend UI array into perfect Java Maps
// Lookup maps at the top level (outside any function, near statusToEnum/modeToEnum)
const backendToStatusHTML = {
    'AVAILABLE': 'available',
    'SNOW_CLEARANCE': 'snowclearance',
    'INSPECTION': 'inspection',
    'EQUIPMENT_FAILURE': 'failure'
};

const backendToModeHTML = {
    'MIXED': 'mixed',
    'LANDING': 'landing',
    'TAKEOFF': 'takeoff'
};
function formatEventsForBackend(frontendEvents) {
    let runwayMap = {};
    let aircraftMap = {};

    frontendEvents.forEach(ev => {
        const tickTime = parseInt(ev.time);

        if (ev.type === "Runway") {
            let status = 'AVAILABLE';

            if (ev.name === "No Change") {
                status = null;
            }
            else if (ev.name === "Runway Inspection") {
                status = 'INSPECTION';
            }
            else if (ev.name === "Snow Clearance") {
                status = 'SNOW_CLEARANCE'; // UPDATED TO MATCH MAIN
            } else if (ev.name === "Equipment Failure") {
                status = 'EQUIPMENT_FAILURE'; // UPDATED TO MATCH MAIN
            }

            if (!runwayMap[tickTime]) runwayMap[tickTime] = [];

            runwayMap[tickTime].push({
                tick: tickTime,
                runwayID: parseInt(ev.locationId),
                status: status,
                mode: ev.mode || 'MIXED',
                type: 'SCHEDULED_CHANGE',
                duration: ev.duration
            });
        }
        else if (ev.type === "Aircraft") {
            let status = 'NONE';
            if (ev.name === "Mechanical Failure") status = 'MECHANICAL';
            else if (ev.name === "Passenger Health") status = 'PASSENGER';

            if (!aircraftMap[tickTime]) aircraftMap[tickTime] = [];
            aircraftMap[tickTime].push({
                tick: tickTime,
                callsign: null,
                type: 'SCHEDULED_EMERGENCY',
                status: status
            });
        }
    });
    return { runways: runwayMap, aircraft: aircraftMap };
}

// EVENTS STUFF - SPRINT 2

function updateEventForm() {
    const typeSelect = document.getElementById("new-event-type");
    const nameSelect = document.getElementById("new-event-name");
    const locSelect = document.getElementById("new-event-loc");
    const selectedType = typeSelect.value;

    // update event names
    nameSelect.innerHTML = "";
    eventOptions[selectedType].forEach(eventName => {
        const option = document.createElement("option");
        option.value = eventName;      // Keeps the text like ("Snow Clearance")
        option.innerText = eventName;
        nameSelect.appendChild(option);
    });

    // update locations
    locSelect.innerHTML = "";
    if (selectedType === "Runway") {
        const count = parseInt(runwayCounter.innerText);
        for (let i = 1; i <= count; i++) {
            const option = document.createElement("option");
            option.value = i - 1;             // Secret ID for Java (0, 1, 2)
            option.innerText = "Runway " + i;
            locSelect.appendChild(option);
        }
    } else {
        const option = document.createElement("option");
        option.value = "Holding Pattern";
        option.innerText = "Holding Pattern";
        locSelect.appendChild(option);
    }

    const modeContainer = document.getElementById("mode-container");
    if (modeContainer) {
        if (selectedType === "Aircraft") {
            modeContainer.style.display = 'none'; // Hide for Aircraft
        } else {
            modeContainer.style.display = 'block'; // Show for Runway
        }
    }

    const durationContainer = document.getElementById("duration-container");
    if (durationContainer) {
        if (selectedType === "Aircraft") {
            durationContainer.style.display = 'none'; // removes duration
        } else {
            durationContainer.style.display = 'block'; // adds duration
        }
    }

    // Reset disabled states and mode value when form type changes.
    nameSelect.disabled = false;
    document.getElementById("new-event-mode").disabled = false;
    document.getElementById("new-event-mode").value = "null";
}

// Lock mode if a closure status is selected
document.getElementById("new-event-name").addEventListener("change", function () {
    const closureStatuses = ["Runway Inspection", "Snow Clearance", "Equipment Failure"];
    const modeSelect = document.getElementById("new-event-mode");

    if (closureStatuses.includes(this.value)) {
        modeSelect.value = "null";
        modeSelect.disabled = true;
    } else {
        modeSelect.disabled = false;
    }
});

// Lock status to "Available" if a mode other than "No Change" is selected
document.getElementById("new-event-mode").addEventListener("change", function () {
    const nameSelect = document.getElementById("new-event-name");

    if (this.value !== "null") {
        nameSelect.value = "Available";
        nameSelect.disabled = true;
    } else {
        nameSelect.disabled = false;
    }
});


// 3. The actual LOAD function
function applyConfigToPage(data) {
    const fieldMapping = {
        "input-sim_mode": data.simulationMode,
        "input-inbound_rate": data.inboundRate,
        "input-outbound_rate": data.outboundRate,
        "input-sim_duration": data.duration,
        "input-max_delay": data.maxWaitTime,
        "input-seed": data.seed,
        "input-mech_failure_rate": data.mechanicalFailureRate,
        "input-health_issue_rate": data.passengerHealthIssueRate,
        "input-inspection_rate": data.runwayInspectionRate,
        "input-snow_rate": data.snowClearanceRate,
        "input-equip_failure_rate": data.equipmentFailureRate
    };

    Object.keys(fieldMapping).forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = fieldMapping[id];
    });

    const autoGenCheckbox = document.getElementById("input-auto_gen");
    if (autoGenCheckbox) {
        autoGenCheckbox.checked = data.automaticGenerationEnabled === true;
    }

    const targetRunways = data.runwaySettings ? data.runwaySettings.length : 1;
    while (number > targetRunways) minus1();
    while (number < targetRunways) add1();

    if (data.runwaySettings) {
        data.runwaySettings.forEach((runway, index) => {
            const i = index + 1;
            const statusEl = document.getElementById(`status_${i}`);
            const modeEl = document.getElementById(`mode_${i}`);

            // This is the fixed way!
            if (statusEl) statusEl.value = backendToStatusHTML[runway.status] || "available";
            if (modeEl && runway.mode) modeEl.value = backendToModeHTML[runway.mode] || "mixed";
        });
    }


    scheduledEventsData = [];

    const backendToEventName = {
        'AVAILABLE': 'Available',
        'INSPECTION': 'Runway Inspection',
        'SNOW_CLEARANCE': 'Snow Clearance',
        'EQUIPMENT_FAILURE': 'Equipment Failure',
        'MECHANICAL': 'Mechanical Failure',
        'PASSENGER': 'Passenger Health'
    };

    const backendToModeName = {
        'MIXED': 'Mixed',
        'LANDING': 'Landing',
        'TAKEOFF': 'Take-Off'
    };

    if (data.scheduledRunwayEvents) {
        console.log("FULL DATA scheduledRunwayEvents:", JSON.stringify(data.scheduledRunwayEvents));
        Object.keys(data.scheduledRunwayEvents).forEach(tick => {
            data.scheduledRunwayEvents[tick].forEach(ev => {
                const rawStatus = ev.status ?? ev.runwayStatus ?? null;
                const rawMode = ev.mode ?? ev.runwayMode ?? null;

                const safeStatus = rawStatus ? rawStatus.toUpperCase() : null;
                const safeMode = rawMode ? rawMode.toUpperCase() : null;

                scheduledEventsData.push({
                    id: Date.now() + Math.floor(Math.random() * 1000),
                    type: "Runway",
                    name: safeStatus ? backendToEventName[safeStatus] : "No Change",
                    rawStatus: safeStatus, // THE FIX: Hidden Enum for saving
                    mode: safeMode || "null",
                    modeLabel: safeMode ? backendToModeName[safeMode] : "No Change",
                    rawMode: safeMode,     // THE FIX: Hidden Enum for saving
                    locationId: ev.runwayID.toString(),
                    locationName: "Runway " + (ev.runwayID + 1),
                    time: tick.toString(),
                    duration: ev.duration ? ev.duration.toString() : "-1"
                });
            });
        });
    }

    if (data.scheduledAircraftEvents) {
        Object.keys(data.scheduledAircraftEvents).forEach(tick => {
            data.scheduledAircraftEvents[tick].forEach(ev => {
                const rawStatus = ev.status || ev.aircraftStatus;
                const safeStatus = rawStatus ? rawStatus.toUpperCase() : null;

                scheduledEventsData.push({
                    id: Date.now() + Math.floor(Math.random() * 1000),
                    type: "Aircraft",
                    name: safeStatus ? backendToEventName[safeStatus] : "Mechanical Failure",
                    rawStatus: safeStatus, // THE FIX: Hidden Enum for saving
                    mode: "N/A",
                    locationId: "Holding Pattern",
                    locationName: "Holding Pattern",
                    time: tick.toString(),
                    duration: "N/A"
                });
            });
        });
    }

    rebuildEventListUI();
    document.getElementById('input-auto_gen').dispatchEvent(new Event('change'));

    document.activeElement.blur();
    const modal = bootstrap.Modal.getInstance(document.getElementById('loadConfigModal'));
    if (modal) modal.hide();
}

function loadFullConfig(name) {
    // API endpoint for fetching a specific configuration
    fetch(`/api/configuration/templates/vieworload/${name}`)
        .then(response => {
            if (!response.ok) throw new Error("Could not find configuration " + name);
            return response.json();
        })
        .then(fullData => {
            // fullData is the full ConfigurationTemplate object
            applyConfigToPage(fullData);

            // Close the modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('loadConfigModal'));
            if (modal) modal.hide();
        })
        .catch(err => alert(err.message));
}

function resetConfig() {
    if (confirm("Are you sure you want to reset all configurations to their default values?")) {
        // Destroy the draft so it doesn't try to load it again
        sessionStorage.removeItem('draftConfig');
        // Reload the page to reset all HTML elements and JS arrays instantly
        window.location.reload();
    }
}

let configSortField = null;
let configSortAsc = true;

function renderConfigRows(summaries) {
    const list = document.getElementById("saved-configs-list");
    const emptyMsg = document.getElementById("empty-configs-msg");

    list.querySelectorAll('tr.config-row').forEach(row => row.remove());

    if (!summaries || summaries.length === 0) {
        emptyMsg.style.display = "";
        return;
    }
    emptyMsg.style.display = "none";

    summaries.forEach(summary => {
        const row = document.createElement("tr");
        row.className = "config-row";
        row.innerHTML = `
        <td style="vertical-align:middle;"><strong style="font-size:1.05rem;">${summary.templateName}</strong></td>
        <td style="vertical-align:middle; color:#6c757d;">${new Date(summary.dateCreated).toLocaleString()}</td>
        <td style="vertical-align:middle; color:#6c757d; font-size:0.8rem; line-height:1.8;">
            Runways: ${summary.runwayCount}<br>
            Inbound Rate: ${summary.inboundRate} /hr<br>
            Outbound Rate: ${summary.outboundRate} /hr<br>
            Scheduled Events: ${summary.scheduledEventsCount || 0}
        </td>
        <td style="vertical-align:middle; white-space:nowrap;">
            <button class="btn btn-dark btn-sm rounded-pill load-btn px-3">Load</button>
            <button class="btn btn-danger btn-sm rounded-pill delete-btn px-3">Delete</button>
        </td>
        `;
        row.querySelector(".load-btn").onclick = function () {
            loadFullConfig(summary.templateName);
        };
        row.querySelector(".delete-btn").onclick = function () {
            if (confirm(`Delete "${summary.templateName}"?`)) {
                fetch(`/api/configuration/templates/delete/${summary.templateName}`, { method: 'DELETE' })
                    .then(() => openLoadConfigModal());
            }
        };
        list.appendChild(row);
    });
}

function sortConfigs(summaries, field, asc) {
    return [...summaries].sort((a, b) => {
        let valA = field === 'name' ? a.templateName.toLowerCase() : new Date(a.dateCreated);
        let valB = field === 'name' ? b.templateName.toLowerCase() : new Date(b.dateCreated);
        if (valA < valB) return asc ? -1 : 1;
        if (valA > valB) return asc ? 1 : -1;
        return 0;
    });
}

function updateSortHeaders(field) {
    document.querySelectorAll('.sort-btn').forEach(btn => {
        const btnField = btn.getAttribute('data-field');
        if (btnField === field) {
            btn.innerText = configSortAsc ? '↑' : '↓';
        } else {
            btn.innerText = '↑↓';
        }
    });
}

let currentSummaries = [];

function openLoadConfigModal() {
    configSortField = null;
    configSortAsc = true;

    fetch('/api/configuration/templates/summaries')
        .then(response => response.json())
        .then(summaries => {
            currentSummaries = summaries || [];
            renderConfigRows(currentSummaries);

            document.querySelectorAll('.sort-btn').forEach(btn => {
                btn.innerText = '↑↓';
                btn.onclick = function () {
                    const field = btn.getAttribute('data-field');
                    if (configSortField === field) {
                        configSortAsc = !configSortAsc;
                    } else {
                        configSortField = field;
                        configSortAsc = true;
                    }
                    updateSortHeaders(field);
                    renderConfigRows(sortConfigs(currentSummaries, field, configSortAsc));
                };
            });
        })
        .catch(err => {
            console.error("Failed to load summaries:", err);
            document.getElementById("empty-configs-msg").style.display = "";
        });

    bootstrap.Modal.getOrCreateInstance(document.getElementById('loadConfigModal')).show();
}

function rebuildEventListUI() {
    const list = document.getElementById("scheduled-events-list");
    const currentRunwayCount = parseInt(document.getElementById("runway-counter").innerText);
    if (!list) return;

    list.innerHTML = "";

    if (scheduledEventsData.length === 0) {
        const emptyMsg = document.createElement("div");
        emptyMsg.id = "empty-list-msg";
        emptyMsg.className = "text-center text-muted small mt-5";
        emptyMsg.innerText = "No events scheduled";
        list.appendChild(emptyMsg);
        return;
    }

    scheduledEventsData.forEach(ev => {
        const isGhost = ev.type === 'Runway' && parseInt(ev.locationId) >= currentRunwayCount;
        const isLate = !isGhost && invalidEventIds.has(ev.id);

        const row = document.createElement("div");
        row.className = `d-flex align-items-center justify-content-between small mb-2 pb-2 border-bottom ${isGhost ? 'ghost-event' : ''} ${isLate ? 'late-event' : ''}`;
        row.setAttribute("data-id", ev.id.toString());

        let parsedDuration = parseInt(ev.duration);
        let start = parseInt(ev.time);
        let durationDisplay = (parsedDuration === -1 || isNaN(parsedDuration)) ? "∞" : `${start + parsedDuration}m`;

        let modeBadge = ev.type === "Runway" ? `<span class="badge bg-info text-dark ms-1">${ev.modeLabel || 'No Change'}</span>` : "";
        let eventLocText = ev.type === 'Runway' ? 'Runway ' + (parseInt(ev.locationId) + 1) : ev.locationId;
        let warning = isGhost ? `<span class="ghost-badge ms-1">INVALID RUNWAY</span>` : "";
        let lateWarning = isLate ? `<span class="late-badge ms-1">PAST DURATION</span>` : "";
        let shortName = ev.name.replace("Equipment Failure", "Equip. Failure").replace("Runway Inspection", "Inspection");

        row.innerHTML = `
            <div class="ps-1 pe-2" style="flex: 1; min-width: 0;">
                <div class="d-flex flex-wrap align-items-center">
                    <strong class="me-1 ${isGhost ? 'text-danger' : isLate ? 'text-warning' : ''}">${shortName}</strong> ${modeBadge} ${warning} ${lateWarning}
                </div>
                <span class="text-muted d-block" style="font-size: 0.75rem;">on ${eventLocText}</span>
            </div>
            <div class="text-end flex-shrink-0 px-2 d-flex align-items-center justify-content-end" style="min-width: 95px;">
                <div class="fw-bold ${isGhost ? 'text-danger' : isLate ? 'text-warning' : 'text-dark'} text-nowrap" style="font-size: 0.85rem;">${start}m - ${durationDisplay}</div>
            </div>
            <div class="flex-shrink-0" style="width: 25px; text-align: right;">
                <button class="btn btn-link text-danger p-0 border-0 fs-5" onclick="deleteEvent(${ev.id})">&times;</button>
            </div>
        `;
        list.appendChild(row);
    });
}

// initial run
document.addEventListener('DOMContentLoaded', () => {
    // Setup the drop downs
    updateEventForm();

    // Check if there was a saved draft from local storage (only created when the user starts the simulation)
    const savedDraft = sessionStorage.getItem('draftConfig')
    // Check if the user just aborted from a simulation
    const wasAborted = sessionStorage.getItem('wasAborted');

    // Restore their previous configuration only if they've just come after stopping the simulation
    if (savedDraft && wasAborted === 'true') {
        try {
            const draftData = JSON.parse(savedDraft);
            applyConfigToPage(draftData);
            console.log("Restored previous configuration because simulation was aborted.");
        } catch (e) {
            console.error("Failed to restore draft config", e);
        }
    }

    // Set placeholder and block invalid characters for Event Inputs
    const timeInput = document.getElementById("new-event-time");
    const durationInput = document.getElementById("new-event-duration");

    if (durationInput) {
        durationInput.placeholder = "Indefinite";
    }

    [timeInput, durationInput].forEach(input => {
        if (input) {
            input.addEventListener('keydown', function(e) {
                if (['e', 'E', '+', '-'].includes(e.key)) {
                    e.preventDefault();
                }
            });
        }
    });

    // NEW: Lock 'Mode' if the Runway is closed
    const nameSelect = document.getElementById("new-event-name");
    const modeSelect = document.getElementById("new-event-mode");

    if (nameSelect && modeSelect) {
        nameSelect.addEventListener("change", function() {
            if (["Runway Inspection", "Snow Clearance", "Equipment Failure"].includes(this.value)) {
                modeSelect.value = "null"; // Force to "No Change"
                modeSelect.disabled = true; // Lock the dropdown
            } else {
                modeSelect.disabled = false; // Unlock it for Available/No Change
            }
        });
    }

    sessionStorage.removeItem('wasAborted');
});

// Attach the logic to the button inside the modal


function saveConfiguration() {
    // HALT if validation fails!
    if (!validateInputs()) return;

    const nameInput = document.getElementById("new-config-name");
    const configName = nameInput ? nameInput.value.trim() : "";
    if (!configName) return alert("Please enter a name");

    const runwayCount = parseInt(document.getElementById("runway-counter").innerText) || 1;
    const runwaySettings = [];
    for (let i = 1; i <= runwayCount; i++) {
        const statusVal = document.getElementById(`status_${i}`).value;
        const modeVal = document.getElementById(`mode_${i}`).value;
        runwaySettings.push({
            runwayID: i - 1,
            status: statusToEnum(statusVal),
            mode: modeToEnum(modeVal)
        });
    }

    const backendEvents = formatEventsForBackend(scheduledEventsData);

    const seedRaw = document.getElementById("input-seed")?.value;
    const seed = (seedRaw !== "" && seedRaw != null) ? parseInt(seedRaw) : 0;

    const payload = {
        templateName: configName,
        runwaySettings: runwaySettings,
        inboundRate: parseInt(document.getElementById("input-inbound_rate").value) || 15,
        outboundRate: parseInt(document.getElementById("input-outbound_rate").value) || 15,
        maxWaitTime: parseInt(document.getElementById("input-max_delay").value) || 30,
        duration: parseInt(document.getElementById("input-sim_duration").value) || 120,
        tickTime: 1000,
        automaticGenerationEnabled: document.getElementById("input-auto_gen").checked || false,
        seed: seed,
        mechanicalFailureRate: parseFloat(document.getElementById("input-mech_failure_rate").value) || 0.0,
        passengerHealthIssueRate: parseFloat(document.getElementById("input-health_issue_rate").value) || 0.0,
        runwayInspectionRate: parseFloat(document.getElementById("input-inspection_rate").value) || 0.0,
        snowClearanceRate: parseFloat(document.getElementById("input-snow_rate").value) || 0.0,
        equipmentFailureRate: parseFloat(document.getElementById("input-equip_failure_rate").value) || 0.0,
        simulationMode: document.getElementById("simulation-mode").value.toUpperCase(),
        scheduledRunwayEvents: backendEvents.runways || {},
        scheduledAircraftEvents: backendEvents.aircraft || {}
    };

    // Shows exact payload in console before sending - remove once working
    console.log("Save payload:", JSON.stringify(payload, null, 2));

    fetch('/api/configuration/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(async res => {
            if (!res.ok) {
                const errText = await res.text();
                console.error("Full server error:", errText);
                throw new Error(errText || "400 Bad Request");
            }
            alert("Configuration saved successfully!");
            const modal = bootstrap.Modal.getInstance(document.getElementById('saveConfigModal'));
            if (modal) modal.hide();
        })
        .catch(err => {
            console.error("Save Error:", err);
            alert("Server rejected save: " + err.message);
        });
}


// VALIDATION

function validateInputs() {
    const inputs = document.querySelectorAll('.field-input');
    let isValid = true;
    let errorMessages = [];
    let firstInvalidInput = null;

    // --- Part 1: Box Validation ---
    for (let input of inputs) {
        const val = parseFloat(input.value);
        const min = parseFloat(input.min);
        const max = parseFloat(input.max);
        if (input.value === "" || isNaN(val) || val < min || val > max) {
            input.style.border = "2px solid red";
            isValid = false;
            if (!firstInvalidInput) firstInvalidInput = input;
        } else {
            input.style.border = "";
        }
    }
    if (!isValid) {
        errorMessages.push("Please fix the highlighted fields. Ensure numbers are within specified ranges.");
    }

    // --- Part 2 & 3: Flag bad events ---
    invalidEventIds.clear();
    const currentRunwayCount = parseInt(document.getElementById("runway-counter").innerText);
    const simDurationEl = document.getElementById("input-sim_duration");
    const simLimit = simDurationEl && simDurationEl.value !== "" ? parseInt(simDurationEl.value) : null;

    let hasGhost = false;
    let hasLate = false;

    scheduledEventsData.forEach(ev => {
        const isGhost = ev.type === 'Runway' && parseInt(ev.locationId) >= currentRunwayCount;
        const isLate = simLimit !== null && parseInt(ev.time) >= simLimit;
        if (isGhost || isLate) invalidEventIds.add(ev.id);
        if (isGhost) hasGhost = true;
        if (isLate) hasLate = true;
    });

    if (hasGhost) {
        errorMessages.push("You have events scheduled for runways that no longer exist. Please adjust the runway count or delete those events.");
        isValid = false;
    }
    if (hasLate) {
        errorMessages.push("You have events scheduled at or after the simulation ends. These will never occur. Please remove them or increase the simulation duration.");
        isValid = false;
    }

    rebuildEventListUI();

    // --- Part 4: Single Alert ---
    if (!isValid) {
        if (firstInvalidInput) firstInvalidInput.focus();
        alert(errorMessages.join("\n\n"));
        return false;
    }

    return true;
}

document.getElementById("confirm-save-btn").onclick = saveConfiguration;