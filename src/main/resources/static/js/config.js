const runwayCounter = document.getElementById("runway-counter");
const add1Btn = document.getElementById("add-button");
const minus1Btn = document.getElementById("minus-button");
const runwayList = document.getElementById("runway-list");
const template = document.getElementById("runway-template");

let number = 10
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

// create first 10 rows, because default value of runway numbers is 10
for (let i = 1; i <= 10; i++) {
    addRunwayRow(i);
}



add1Btn.onclick = add1
minus1Btn.onclick = minus1

// INPUT CONFIG
// format for inputs with text boxes
const inputConfig = [
    { label: "Inbound Rate /hr",        name: "inbound_rate",      range: "0 - 100",    min: 0,  max: 100, val: 15 },
    { label: "Outbound Rate /hr",       name: "outbound_rate",     range: "0 - 100",    min: 0,  max: 100, val: 15 },
    { label: "Simulation Duration (mins)", name: "sim_duration",      range: "60 - 1440",  min: 60, max: 1440, val: 120 },
    { label: "Mech. Failure Rate",      name: "mech_failure_rate", range: "0.00 - 0.10", min: 0, max: 0.1, step: 0.01, val: 0.05 },
    { label: "Health Issue Rate",       name: "health_issue_rate", range: "0.0 - 0.1",   min: 0, max: 0.1, step: 0.01, val: 0.01 },
    { label: "Runway Inspection Rate",  name: "inspection_rate",   range: "0.0 - 0.1",   min: 0, max: 0.1, step: 0.01, val: 0.01 },
    { label: "Snow Clearance Rate",     name: "snow_rate",         range: "0.0 - 0.1",   min: 0, max: 0.1, step: 0.01, val: 0.01 },
    { label: "Equip. Failure Rate",     name: "equip_failure_rate",range: "0.0 - 0.1",   min: 0, max: 0.1, step: 0.01, val: 0.01 },
    { label: "Max Delay Time (mins)",      name: "max_delay",         range: "0 - 60",    min: 0,  max: 60,  val: 30 },
    { label: "Simulation Seed", name: "seed", range: "0 - 10^8", min: 0, max: 99999999, val: 0 }
];

// generate input params
function generateInputs() {
    const container = document.getElementById("input-parameters-list");
    const template = document.getElementById("input-field-template");

    inputConfig.forEach(config => {
        const clone = template.content.cloneNode(true);
        const input = clone.querySelector(".field-input");

        // Restore Range Text
        clone.querySelector(".field-label").innerText = config.label;
        clone.querySelector(".field-range").innerText = "Range: " + config.range;

        input.id = "input-" + config.name;
        input.name = config.name;
        input.value = config.val;

        // Enable Dice only for Seed
        if (config.name === "seed") {
            clone.querySelector(".dice-btn").style.display = "block";
        }

        container.appendChild(clone);
    });
}

function openSaveModal() {
    // Clear the input field for a fresh start
    document.getElementById("new-config-name").value = "";

    // Show the modal using Bootstrap's JS API
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('saveConfigModal'));
    modal.show();
}



function startSimulation() {
    // gathering data


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

    // Helper function to convert status dropdown value to enum
    function statusToEnum(statusVal) {
        const mapping = {
            'available': 'AVAILABLE',
            'snow': 'SNOWCLEARANCE',
            'inspection': 'INSPECTION',
            'failure': 'FAILURE'
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

    for (let i = 1; i <= runwayCount; i++) {
        const statusVal = document.getElementById(`status_${i}`).value;
        const modeVal = document.getElementById(`mode_${i}`).value;

        runwayData.push({
            runwayID: i - 1,  // Java expects 0-based index
            status: statusToEnum(statusVal),
            mode: modeToEnum(modeVal)
        });
    }
    console.log("TRUTH TEST - What is in the array?:", scheduledEventsData);

    const processedEvents = formatEventsForBackend(scheduledEventsData);

    const payload = {
        runwaySettings: runwayData,
        scheduledRunwayEvents: processedEvents.runways,
        scheduledAircraftEvents: processedEvents.aircraft,
        simulationID: Date.now().toString(),

        // Logic Configuration
        automaticGenerationEnabled: document.getElementById("input-auto_gen")?.checked || false,
        seed: parseInt(document.getElementById("input-seed")?.value) || 0,
        tickTime: 1000,

        // Parameters
        inboundRate: parseInt(document.getElementById("input-inbound_rate").value) || 15,
        outboundRate: parseInt(document.getElementById("input-outbound_rate").value) || 15,
        maxWaitTime: parseInt(document.getElementById("input-max_delay").value) || 30,
        duration: parseInt(document.getElementById("input-sim_duration").value) || 120,

        // Statistical Rates (@NotNull in Java)
        mechanicalFailureRate: parseFloat(document.getElementById("input-mech_failure_rate")?.value) || 0.05,
        passengerHealthIssueRate: parseFloat(document.getElementById("input-health_issue_rate")?.value) || 0.01,
        runwayInspectionRate: parseFloat(document.getElementById("input-inspection_rate")?.value) || 0.01,
        snowClearanceRate: parseFloat(document.getElementById("input-snow_rate")?.value) || 0.01,
        equipmentFailureRate: parseFloat(document.getElementById("input-equip_failure_rate")?.value) || 0.01
    };

    console.log("Sending Payload:", payload); // Debug check
    alert(
        "1. RAW ARRAY (If this is empty, you didn't add an event!):\n" +
        JSON.stringify(scheduledEventsData, null, 2) +
        "\n\n2. FORMATTED RUNWAYS:\n" +
        JSON.stringify(payload.scheduledRunwayEvents, null, 2)
    );

    // sending

    fetch('/api/configuration/validate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to validate configuration');
            }
            return response.text();
        })
        .then(data => {
            console.log('Configuration validated:', data);
            // Now start the simulation
            return fetch('/api/simulation/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to start simulation');
            }
            return response.text();
        })
        .then(data => {
            console.log('Simulation started:', data);
            // Redirect to progress page
            window.location.href = '/progress.html';
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



// EVENTS STUFF - SPRINT 2
const eventOptions = {
    "Runway": ["Runway Inspection", "Snow Clearance", "Equipment Failure",
        "Mode: Landing Only", // NEW
        "Mode: Takeoff Only", // NEW
        "Mode: Mixed"], // NEW
    "Aircraft": ["Mechanical Failure", "Passenger Health"]
};


// 2. The Event Builder (No Duration gathered or saved)
function addEvent() {
    const typeSelect = document.getElementById("new-event-type");
    const nameSelect = document.getElementById("new-event-name");
    const locSelect = document.getElementById("new-event-loc");
    const timeInput = document.getElementById("new-event-time");
    const list = document.getElementById("scheduled-events-list");
    const emptyMsg = document.getElementById("empty-list-msg");
    const simInput = document.querySelector('input[id="input-sim_duration"]');

    const eventType = typeSelect.value;

    // Logic Separation:
    const eventEnum = nameSelect.value; // Raw Enum (e.g., SNOWCLEARANCE)
    const displayLabel = nameSelect.options[nameSelect.selectedIndex].text; // Clean Label (e.g., Snow Clearance)

    const eventLocText = locSelect.options[locSelect.selectedIndex].text;
    const eventLocId = locSelect.value;
    const startTime = parseInt(timeInput.value);
    const simLimit = parseInt(simInput.value);

    // Validation
    if (isNaN(startTime)) {
        alert("Please enter a valid time.");
        return;
    }
    if (startTime > simLimit) {
        alert(`Error: Simulation ends at ${simLimit}m.`);
        return;
    }

    const durationInput = document.getElementById("new-event-duration");
    let eventDuration = parseInt(durationInput.value);

    // Auto-fill logic
    if (eventType === "Aircraft") {
        eventDuration = -1; // Aircraft events don't use duration
    } else if (isNaN(eventDuration)) {
        eventDuration = 30; // Default runway duration if left blank
    }

    const uniqueId = Date.now();

    // Data array gets the Enum for the DTO
    scheduledEventsData.push({
        id: uniqueId,
        type: eventType,
        name: eventEnum,
        locationId: eventLocId,
        time: startTime,
        duration: eventDuration // new
    });

    if (emptyMsg) emptyMsg.remove();

    const row = document.createElement("div");
    row.className = "d-flex align-items-center small mb-2 pb-2 border-bottom pe-1";
    row.setAttribute("data-id", uniqueId.toString());

    // UI List gets the Display Label
    row.innerHTML = `
    <div class="flex-grow-1 text-truncate">
        <strong>${displayLabel}</strong> 
        <span class="text-muted">on ${eventLocText}</span>
    </div>
    <div style="width: 70px;" class="text-end text-muted">
        ${startTime} mins
    </div>
    <div style="width: 30px;" class="text-end">
        <button class="btn btn-link text-danger p-0 border-0 fs-5" 
                onclick="deleteEvent(${uniqueId})">&times;</button>
    </div>
    `;

    let durationBadge = eventType === "Runway" ? `<span class="badge bg-secondary ms-1">${eventDuration}m</span>` : "";

    row.innerHTML = `
    <div class="flex-grow-1 text-truncate">
        <strong>${displayLabel}</strong> ${durationBadge}
        <span class="text-muted d-block" style="font-size: 0.75rem;">on ${eventLocText}</span>
    </div>
    <div style="width: 50px;" class="text-end text-muted fw-bold">
        ${startTime}m
    </div>
    <div style="width: 30px;" class="text-end">
        <button class="btn btn-link text-danger p-0 border-0 fs-5" onclick="deleteEvent(${uniqueId})">&times;</button>
    </div>
    `;

    list.appendChild(row);
    list.scrollTop = list.scrollHeight;
    timeInput.value = "";
    durationInput.value = "";
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
        'snow': 'SNOWCLEARANCE', // Fixed: Matches your Enum exactly
        'inspection': 'INSPECTION',
        'failure': 'FAILURE'
    };
    return mapping[statusVal.toLowerCase()] || 'AVAILABLE';
}

function modeToEnum(modeVal) {
    const mapping = {
        'mixed': 'MIXED',
        'landing': 'LANDING',
        'takeoff': 'TAKEOFF'
    };
    return mapping[modeVal.toLowerCase()] || 'MIXED';
}

// HELPER FUNCTION: Turns our frontend UI array into perfect Java Maps
function formatEventsForBackend(frontendEvents) {
    let runwayMap = {};
    let aircraftMap = {};

    frontendEvents.forEach(ev => {
        const tickTime = parseInt(ev.time);

        if (ev.type === "Runway") {
            let status = 'AVAILABLE';
            let type = 'SCHEDULED_CHANGE'; // From your RunwayEventType Enum
            let mode = 'MIXED';
            if (ev.name === "Runway Inspection") {
                status = 'INSPECTION';
            } else if (ev.name === "Snow Clearance") {
                status = 'SNOWCLEARANCE';
            } else if (ev.name === "Equipment Failure") {
                status = 'FAILURE';
            } else if (ev.name === "Mode: Landing Only") {
                mode = 'LANDING';
            } else if (ev.name === "Mode: Takeoff Only") {
                mode = 'TAKEOFF';
            } else if (ev.name === "Mode: Mixed") {
                mode = 'MIXED';
            }

            if (!runwayMap[tickTime]) runwayMap[tickTime] = [];
            runwayMap[tickTime].push({
                tick: tickTime,
                runwayID: parseInt(ev.locationId),
                status: status,
                mode: mode,
                type: type,
                duration: ev.duration // Required by RunwayEvent.java @NotNull
            });
        }
        else if (ev.type === "Aircraft") {
            let status = 'NONE';
            if (ev.name === "Mechanical Failure") status = 'MECHANICAL';
            else if (ev.name === "Passenger Health") status = 'PASSENGER';

            if (!aircraftMap[tickTime]) aircraftMap[tickTime] = [];
            aircraftMap[tickTime].push({
                tick: tickTime,
                callsign: "BAW" + Math.floor(Math.random() * 900 + 100),
                type: 'SCHEDULED_EMERGENCY', // From your AircraftEventType Enum
                status: status                // From your EmergencyStatus Enum
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

    const durationContainer = document.getElementById("duration-container");
    if (durationContainer) {
        if (selectedType === "Aircraft") {
            durationContainer.style.display = 'none'; // removes duration
        } else {
            durationContainer.style.display = 'block'; // adds duration
        }
    }
}

const dummyConfigs = [
    {
        id: 1, name: "Config 1", date: "27-02-2026",
        inboundRate: 5, outboundRate: 2, duration: 240, maxWait: 45, seed: 123456, runways: 2, events: 5
    },
    {
        id: 2, name: "Config 2", date: "26-02-2026",
        inboundRate: 15, outboundRate: 15, duration: 120, maxWait: 30, seed: 987654, runways: 8, events: 0
    },
    {
        id: 3, name: "Config 3", date: "20-02-2026",
        inboundRate: 15, outboundRate: 35, duration: 120, maxWait: 34, seed: 231495, runways: 10, events: 0
    },
    {
        id: 4, name: "Config 4", date: "20-02-2025",
        inboundRate: 15, outboundRate: 35, duration: 120, maxWait: 15, seed: 231435, runways: 10, events: 17
    }
];

// load configuration modal/pop-up
function openLoadConfigModal() {
    const list = document.getElementById("saved-configs-list");
    const template = document.getElementById("saved-config-template");
    const emptyMsg = document.getElementById("empty-configs-msg");

    // Clear out old rows (but keep the empty message div)
    list.querySelectorAll('.list-group-item').forEach(row => row.remove());

    if (dummyConfigs.length === 0) {
        emptyMsg.style.display = "block";
    } else {
        emptyMsg.style.display = "none";

        dummyConfigs.forEach(configData => {
            const clone = template.content.cloneNode(true);

            // Set Name and Date
            clone.querySelector(".config-name").innerText = configData.name;
            clone.querySelector(".config-date").innerText = "Saved: " + configData.date;

            // --- The Extensible Grid Logic ---
            const detailsToShow = [
                { label: "Inbound", value: `${configData.inboundRate}/hr` },
                { label: "Outbound", value: `${configData.outboundRate}/hr` },
                { label: "Duration", value: `${configData.duration} mins` },
                { label: "Max Delay", value: `${configData.maxWait} mins` },
                { label: "Runways", value: configData.runways },
                { label: "Seed", value: configData.seed },
                {
                    label: "No. of events",
                    // 1. First, check if the actual array exists and has items
                    value: (configData.savedEvents && configData.savedEvents.length > 0)
                        ? configData.savedEvents.length
                        // 2. If the array is missing/empty, fall back to the hardcoded 'events' number
                        : (configData.events !== undefined ? configData.events : 0)
                },
                {
                    label: "Random Events",
                    value: configData.autoGen !== undefined ? (configData.autoGen ? "Enabled" : "Disabled") : undefined
                }
                // can add more stuff
                // { label: "Mech Fail", value: configData.mechFailureRate ? `${configData.mechFailureRate * 100}%` : undefined },
                // { label: "Snow Rate", value: configData.snowRate ? `${configData.snowRate * 100}%` : undefined }
            ];


            let detailsHTML = `<div class="row g-2">`;

            detailsToShow.forEach(item => {
                // Skips any rates that are undefined or missing from the database
                if (item.value !== undefined && String(item.value).indexOf("undefined") === -1) {
                    detailsHTML += `
                        <div class="col-6 text-truncate" title="${item.value}">
                            <span class="text-dark fw-bold">${item.label}:</span> ${item.value}
                        </div>
                    `;
                }
            });

            detailsHTML += `</div>`;
            clone.querySelector(".config-stats").innerHTML = detailsHTML;

            // --- Button Interactions ---
            const detailsBox = clone.querySelector(".config-details-box");
            clone.querySelector(".details-btn").onclick = function() {
                const bsCollapse = new bootstrap.Collapse(detailsBox);
                bsCollapse.toggle();
            };

            clone.querySelector(".load-btn").onclick = function() {
                applyConfigToPage(configData);
            };

            list.appendChild(clone);
        });
    }

    // Show the Bootstrap Modal safely
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('loadConfigModal'));
    modal.show();
}

// 3. The actual LOAD function
function applyConfigToPage(data) {
    const fieldMapping = {
        "input-inbound_rate": data.inboundRate,
        "input-outbound_rate": data.outboundRate,
        "input-sim_duration": data.duration,
        "input-max_delay": data.maxWait,
        "input-seed": data.seed
    };

    Object.keys(fieldMapping).forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = fieldMapping[id];
    });

    const autoGenCheckbox = document.getElementById("input-auto_gen");
    if (autoGenCheckbox) {
        autoGenCheckbox.checked = data.autoGen === true;
    }

    // Sync Runway Count (Adds or removes rows)
    while (number > data.runways) minus1();
    while (number < data.runways) add1();

    // We loop through EVERY runway now active and ensure its status and mode matches the stored one
    for (let i = 1; i <= number; i++) {
        const statusEl = document.getElementById(`status_${i}`);
        const modeEl = document.getElementById(`mode_${i}`);

        if (statusEl && modeEl) {
            // If the config has specific saved details, use them.
            // Otherwise, force them to the "clean" default.
            if (data.runwayDetails && data.runwayDetails[i - 1]) {
                statusEl.value = data.runwayDetails[i - 1].status;
                modeEl.value = data.runwayDetails[i - 1].mode;
            } else {
                statusEl.value = "available"; // Default
                modeEl.value = "mixed";       // Default
            }
        }
    }

    // 3. Restore Events (Ensures empty list if no events saved)
    scheduledEventsData = data.savedEvents ? [...data.savedEvents] : [];

    // 4. Refresh the Event UI
    rebuildEventListUI();

    document.activeElement.blur(); // Remove focus from the Load button
    const modal = bootstrap.Modal.getInstance(document.getElementById('loadConfigModal'));
    if (modal) modal.hide();
}

function rebuildEventListUI() {
    const list = document.getElementById("scheduled-events-list");
    if (!list) return;

    list.innerHTML = ""; // Wipe everything

    if (scheduledEventsData.length === 0) {
        // Add the "No events" message if the list is empty
        const emptyMsg = document.createElement("div");
        emptyMsg.id = "empty-list-msg";
        emptyMsg.className = "text-center text-muted small mt-5";
        emptyMsg.innerText = "No events scheduled";
        list.appendChild(emptyMsg);
        return;
    }

    // Re-draw the rows for the loaded events
    scheduledEventsData.forEach(ev => {
        const row = document.createElement("div");
        row.className = "d-flex align-items-center small mb-2 pb-2 border-bottom pe-1";
        row.setAttribute("data-id", ev.id.toString());

        // 1. Recreate the badge using the saved duration
        let durationBadge = ev.type === "Runway" ? `<span class="badge bg-secondary ms-1">${ev.duration}m</span>` : "";

        // 2. Format the location text so it reads "Runway 1" instead of "0"
        let eventLocText = ev.type === 'Runway' ? 'Runway ' + (parseInt(ev.locationId) + 1) : ev.locationId;

        // 3. Inject the exact same HTML template you just made
        row.innerHTML = `
            <div class="flex-grow-1 text-truncate">
                <strong>${ev.name}</strong> ${durationBadge}
                <span class="text-muted d-block" style="font-size: 0.75rem;">on ${eventLocText}</span>
            </div>
            <div style="width: 50px;" class="text-end text-muted fw-bold">
                ${ev.time}m
            </div>
            <div style="width: 30px;" class="text-end">
                <button class="btn btn-link text-danger p-0 border-0 fs-5" onclick="deleteEvent(${ev.id})">&times;</button>
            </div>
        `;
        list.appendChild(row);
    });
}
// initial run
document.addEventListener('DOMContentLoaded', () => {
    updateEventForm();
});


// Attach the logic to the button inside the modal
document.getElementById("confirm-save-btn").onclick = function() {
    const name = document.getElementById("new-config-name").value.trim();
    if (!name) {
        alert("Please enter a name.");
        return;
    }


    const currentRunwayDetails = [];
    for (let i = 1; i <= number; i++) {
        const sEl = document.getElementById(`status_${i}`);
        const mEl = document.getElementById(`mode_${i}`);
        currentRunwayDetails.push({
            id: i - 1,
            status: sEl ? sEl.value : "available",
            mode: mEl ? mEl.value : "mixed"
        });
    }

    const configData = {
        name: name,
        date: new Date().toLocaleDateString(),
        inboundRate: document.getElementById("input-inbound_rate").value,
        outboundRate: document.getElementById("input-outbound_rate").value,
        duration: document.getElementById("input-sim_duration").value,
        maxWait: document.getElementById("input-max_delay").value,
        seed: document.getElementById("input-seed").value,
        autoGen: document.getElementById("input-auto_gen").checked,
        runways: number,
        runwayDetails: currentRunwayDetails,
        savedEvents: [...scheduledEventsData] // Uses your existing events array
    };

    dummyConfigs.push(configData);

    // Close modal and notify
    bootstrap.Modal.getInstance(document.getElementById('saveConfigModal')).hide();
    alert("Config Saved!");
};