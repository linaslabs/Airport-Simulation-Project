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
    { label: "Max Delay Time (mins)",      name: "max_delay",         range: "0 - 60",    min: 0,  max: 60,  val: 30 }
];

// generate input params
function generateInputs() {
    const container = document.getElementById("input-parameters-list");
    const template = document.getElementById("input-field-template");

    // loop through each item in the list above
    inputConfig.forEach(config => {
        const clone = template.content.cloneNode(true);

        const label = clone.querySelector(".field-label");
        label.innerText = config.label; // update innerText with appropriate label i.e "Inbound Rate \hr"
        label.htmlFor = "input-" + config.name; // needs same name as input.id, so it is clear this label corresponds this input box

        const rangeText = clone.querySelector(".field-range");
        if (rangeText) {
            rangeText.innerText = "Range: " + config.range;
        }

        // input box
        const input = clone.querySelector(".field-input");
        input.id = "input-" + config.name;       // ID for the label to find
        input.name = config.name;
        input.min = config.min;
        input.max = config.max;

        // adds the step
        if (config.step) input.step = config.step;
        // default val
        if (config.val) input.value = config.val;

        // add the finished box to the page
        container.appendChild(clone);
    });
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

    const payload = {
        runwaySettings: runwayData,

        inboundRate: parseInt(document.querySelector('input[id="input-inbound_rate"]').value) || 15, // || so some value can be read
        outboundRate: parseInt(document.querySelector('input[id="input-outbound_rate"]').value) || 15,
        maxWaitTime: parseInt(document.querySelector('input[id="input-max_delay"]').value) || 30,
        duration: parseInt(document.querySelector('input[id="input-sim_duration"]').value) || 120,
        automaticGenerationEnabled: false,

        mechanicalFailureRate: parseFloat(document.querySelector('input[id="input-mech_failure_rate"]').value) || 0.0,
        passengerHealthIssueRate: parseFloat(document.querySelector('input[id="input-health_issue_rate"]').value) || 0.0,
        runwayInspectionRate: parseFloat(document.querySelector('input[id="input-inspection_rate"]').value) || 0.0,
        snowClearanceRate: parseFloat(document.querySelector('input[id="input-snow_rate"]').value) || 0.0,
        equipmentFailureRate: parseFloat(document.querySelector('input[id="input-equip_failure_rate"]').value) || 0.0,

        tickTime: 1000, // like sim duration

        // not yet implemented
        seed: Math.floor(Math.random() * 100000000),

        // send explicit empty maps for event lists when no manual events are scheduled yet
        scheduledRunwayEvents: {},
        scheduledAircraftEvents: {}

        // Mechanical Failure Rate
        //
        // Health Issue Rate
        //
        // Runway Inspection Rate
        //
        // Snow Clearance Rate
    };

    console.log("Sending Payload:", payload); // Debug check

    // sending

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
            return fetch('/api/simulation/start', {
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
            window.location.href = '/progress.html';
        })
        .catch(error => {
            console.error('Error:', error);
            alert("Error: " + error.message);
        });
}





// initialisation
generateInputs();


// EVENTS STUFF - SPRINT 2
const eventOptions = {
    "Runway": ["Runway Inspection", "Snow Clearance", "Equipment Failure"],
    "Aircraft": ["Mechanical Failure", "Passenger Health"]
};

// 2. Main Update Function
function updateEventForm() {
    const typeSelect = document.getElementById("new-event-type");
    const nameSelect = document.getElementById("new-event-name");
    const locSelect = document.getElementById("new-event-loc");

    const selectedType = typeSelect.value;

    // --- A. UPDATE EVENT NAMES ---
    // Clear current options
    nameSelect.innerHTML = "";

    // Loop through the list above and add new options
    eventOptions[selectedType].forEach(eventName => {
        const option = document.createElement("option");
        option.value = eventName;
        option.innerText = eventName;
        nameSelect.appendChild(option);
    });

    // --- B. UPDATE LOCATIONS ---
    locSelect.innerHTML = "";

    if (selectedType === "Runway") {
        // Get the current number of runways from your counter
        const count = parseInt(runwayCounter.innerText);
        // Add "All Runways" option
        /* const allOpt = document.createElement("option");
        allOpt.value = "All";
        allOpt.innerText = "All Runways";
        locSelect.appendChild(allOpt); */

        // Loop to create R01, R02, etc.
        for (let i = 1; i <= count; i++) {
            const option = document.createElement("option");
            option.value = "R" + i; // Stores "R1"
            option.innerText = "Runway " + i; // Shows "Runway 1"
            locSelect.appendChild(option);
        }
    } else {
        // If Passenger, location is Holding Pattern
        const option = document.createElement("option");
        option.value = "Holding Pattern";
        option.innerText = "Holding Pattern";
        locSelect.appendChild(option);
    }
}


// Run this once when page loads to set the initial state
document.addEventListener('DOMContentLoaded', function() {
    updateEventForm();
});

let scheduledEventsData = [];

function addEvent() {
    // get elements
    const nameSelect = document.getElementById("new-event-name");
    const locSelect = document.getElementById("new-event-loc");
    const timeInput = document.getElementById("new-event-time");
    const list = document.getElementById("scheduled-events-list");
    const emptyMsg = document.getElementById("empty-list-msg");
    const simInput = document.querySelector('input[name="sim_duration"]');

    // get values
    const eventName = nameSelect.value;
    const eventLoc = locSelect.options[locSelect.selectedIndex].text;
    const startTime = parseInt(timeInput.value);
    const simLimit = parseInt(simInput.value);

    // might change validation
    // validate empty start time
    if (isNaN(startTime)) {
        alert("Please enter a valid time.");
        return;
    }

    // validate start time (shouldn't start event after simulation finishes)
    if (startTime > simLimit) {
        alert(`Error: Simulation ends at ${simLimit}m. You cannot schedule an event at ${startTime}m.`);
        return;
    }

    const uniqueId = Date.now(); //
    scheduledEventsData.push({
        id: uniqueId,
        name: eventName,
        location: eventLoc,
        time: startTime
    });

    console.log("Event Added:", scheduledEventsData);

    // replace empty message if it is being shown
    if (emptyMsg) emptyMsg.remove();

    const row = document.createElement("div");
    // 'pe-1' ensures the content doesn't touch the scrollbar
    row.className = "d-flex align-items-center small mb-2 pb-2 border-bottom pe-1";
    row.setAttribute("data-id", uniqueId.toString());

    row.innerHTML = `
    <div class="flex-grow-1 text-truncate">
        <strong>${eventName}</strong> <span class="text-muted">on ${eventLoc}</span>
    </div>
    
    <div style="width: 60px;" class="text-end text-muted">
        ${startTime} mins
    </div>
    
    <div style="width: 30px;" class="text-end">
        <button class="btn btn-link text-danger p-0 border-0 fs-5" 
                onclick="deleteEvent(${uniqueId})">
            &times;
        </button>
    </div>
`;

    list.appendChild(row);
    list.scrollTop = list.scrollHeight;

    // empty time input box
    timeInput.value = "";
}

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