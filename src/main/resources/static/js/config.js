/**
 * DOM Elements for Runway Management
 */
const runwayCounter = document.getElementById("runway-counter");
const add1Btn = document.getElementById("add-button");
const minus1Btn = document.getElementById("minus-button");
const runwayList = document.getElementById("runway-list");
const template = document.getElementById("runway-template");

/**
 * Tracks the IDs of scheduled events that are invalid (e.g., overlapping or out of bounds) - for validation purposes
 * @type {Set<number>}
 */
let invalidEventIds = new Set();

/**
 * Tracks the current number of active runways.
 * @type {number}
 */
let number = 1;

/**
 * Increases the runway count by 1 (up to a maximum of 10).
 * Updates the UI counter, adds a new runway row, and refreshes the event form.
 */
function add1() {
  if (number < 10) {
    number++;
    addRunwayRow(number); // adds new runways column
  }
  runwayCounter.innerHTML = number.toString(); // updates number n in input parameter  (- n +)
  updateEventForm(); // relays the update to the events section
}

/**
 * Decreases the runway count by 1 (down to a minimum of 1).
 * Updates the UI counter, removes the last runway row, and refreshes the event form.
 */
function minus1() {
  if (number > 1) {
    removeRow(number); // removes last runway

    number--;
  }
  runwayCounter.innerHTML = number.toString(); // updates number n in input parameter (- n +)
  updateEventForm(); // relays the update to the events section
}
/**
 * Clones the HTML template to create a new runway configuration row.
 * Updates the IDs and labels to match the newly assigned runway number.
 * * @param {number} id - The assigned runway number (1-indexed value).
 */
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

/**
 * Removes a specific runway row from the DOM.
 * * @param {number} id - The assigned runway number to remove (1-indexed).
 */
function removeRow(id) {
  // find the row with the id (runway number) to be deleted
  const rowToDelete = document.getElementById("runway-row-" + id);

  if (rowToDelete) {
    // make sure the row exists
    rowToDelete.remove();
  }
}

// create first row, because default number of rows is 1 (formerly 10 - hence the loop)
for (let i = 1; i <= 1; i++) {
  addRunwayRow(i);
}

add1Btn.onclick = add1;
minus1Btn.onclick = minus1;

/**
 * Configuration array defining the properties, constraints, and help tips for all input fields.
 * This array acts as the blueprint for dynamically generating the Input Parameters UI.
 * @type {Array<Object>}
 */
const inputConfig = [
  { label: "Inbound Rate /hr",           name: "inbound_rate",      range: "0 - 100",     min: 0,  max: 100,      step: 1,    val: 15 },
  { label: "Outbound Rate /hr",          name: "outbound_rate",     range: "0 - 100",     min: 0,  max: 100,      step: 1,    val: 15 },
  { label: "Simulation Duration (mins)", name: "sim_duration",      range: "60 - 1440",   min: 60, max: 1440,     step: 1,    val: 120 },
  { label: "Max Departure Wait Time (mins)",  name: "max_delay",         range: "0 - 360",     min: 0,  max: 360,      step: 1,    val: 20 },
  { label: "Simulation Seed",            name: "seed",              range: "0 - 10^7",    min: 0,  max: 10000000, step: 1,    val: 0 },
  { label: "Mechanical Failure Multiplier", name: "mech_failure_rate", range: "0.0 - 10000", min: 0, max: 10000, step: 0.1, val: "0.0", help: "Multiplies the baseline chance (0.000000135) of an aircraft in the holding pattern experiencing a mechanical issue each minute." },
  { label: "Health Issue Multiplier",       name: "health_issue_rate", range: "0.0 - 10000", min: 0, max: 10000, step: 0.1, val: "0.0", help: "Multiplies the baseline chance (0.000000556) of an aircraft in the holding pattern experiencing a passenger health emergency, serious enough to require early landing, each minute." },
  { label: "Runway Inspection Multiplier",  name: "inspection_rate",   range: "0.0 - 10000", min: 0, max: 10000, step: 0.1, val: "0.0", help: "Multiplies the baseline chance (0.001388) of a runway temporarily closing for inspection each minute." },
  { label: "Snow Clearance Multiplier",     name: "snow_rate",         range: "0.0 - 10000", min: 0, max: 10000, step: 0.1, val: "0.0", help: "Multiplies the baseline chance (0.00000951) of a runway temporarily closing for snow clearance each minute." },
  { label: "Equipment Failure Multiplier",  name: "equip_failure_rate",range: "0.0 - 10000", min: 0, max: 10000, step: 0.1, val: "0.0", help: "Multiplies the baseline chance (0.00000278) of runway temporarily closing due to equipment failure each minute." },
];

/**
 * Dynamically builds the Input Parameters form based on the `inputConfig` array.
 * Assigns fields to either the main parameters list or the rate multipliers list.
 * Attaches validation listeners and help tooltips.
 */
function generateInputs() {
  const mainContainer = document.getElementById("input-parameters-list");
  const rateContainer = document.getElementById("rate-parameters-list");
  const template = document.getElementById("input-field-template");

  // iterate through the predefined configuration array to build each input field
  inputConfig.forEach((config) => {
    const clone = template.content.cloneNode(true);
    const input = clone.querySelector(".field-input");

    const labelEl = clone.querySelector(".field-label");
    labelEl.innerText = config.label;

    // if a help description exists, dynamically build and attach a Bootstrap tooltip (?) element
    if (config.help) {
      const helpBubble = document.createElement("span");
      helpBubble.className = "help-bubble";
      helpBubble.innerText = "?";
      helpBubble.setAttribute("tabindex", "0");
      helpBubble.setAttribute("role", "button");
      helpBubble.setAttribute("data-bs-toggle", "tooltip");
      helpBubble.setAttribute("data-bs-placement", "top");
      helpBubble.setAttribute("data-bs-title", config.help);
      helpBubble.setAttribute("aria-label", config.help);
      labelEl.appendChild(helpBubble);
    }
    // set the grey subtext showing the valid numerical range
    clone.querySelector(".field-range").innerText = "Range: " + config.range;

    // map the config values to the actual HTML input attributes
    input.id = "input-" + config.name;
    input.name = config.name;
    input.value = config.val;

    if (config.min !== undefined) input.min = config.min;
    if (config.max !== undefined) input.max = config.max;
    if (config.step !== undefined) input.step = config.step;

    // block any non numbers being input into boxes (including the special case of e)
    input.addEventListener("keydown", function (e) {
      if (["e", "E", "+", "-"].includes(e.key)) {
        e.preventDefault();
      }
    });

    // show the random seed generator button ONLY for the seed parameter
    if (config.name === "seed") {
      clone.querySelector(".dice-btn").style.display = "block";
    }

    // rate fields go to rate container, everything else to main
    const rateFields = [
      "mech_failure_rate",
      "health_issue_rate",
      "inspection_rate",
      "snow_rate",
      "equip_failure_rate",
    ];
    if (rateFields.includes(config.name)) {
      rateContainer.appendChild(clone);
    } else {
      mainContainer.appendChild(clone);
    }
  });
}

/**
 * Initialises Bootstrap Tooltips for all dynamically generated help bubbles.
 */
function initialiseHelpBubbles() {
  if (!window.bootstrap || !bootstrap.Tooltip) return;

  document
    .querySelectorAll('.help-bubble[data-bs-toggle="tooltip"]')
    .forEach((el) => {
      bootstrap.Tooltip.getOrCreateInstance(el, {
        trigger: "hover focus",
        boundary: "viewport",
      });
    });
}

/**
 * Updates the disabled state and opacity of rate fields based on the checkbox.
 * Forces values back to 0.0 when disabled.
 */
function updateRates() {
  // array of input ids for the random event multiplier fields
  const rateFields = [
    "input-mech_failure_rate",
    "input-health_issue_rate",
    "input-inspection_rate",
    "input-snow_rate",
    "input-equip_failure_rate",
  ];

  // grab the checkbox state
  const checkbox = document.getElementById("input-auto_gen");

  // safety check in case the checkbox doesn't exist on the page
  if (!checkbox) return;

  // loop through each rate field and toggle its state
  rateFields.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;

    if (checkbox.checked) {
      el.disabled = false;
      el.style.opacity = "1";
    } else {
      el.disabled = true;
      el.style.opacity = "0.4";
      el.value = "0.0"; // forces the value back to 0.0 when disabled
    }
  });
}

/**
 * Attaches the toggle logic to the checkbox and runs the initial check on page load.
 */
function setupRateToggle() {
  const checkbox = document.getElementById("input-auto_gen");
  if (checkbox) {
    checkbox.addEventListener("change", updateRates);
    updateRates(); // run on page load
  }
}

/**
 * Opens the save configuration modal.
 * Clears any previously entered configuration name to ensure a blank form.
 */
function openSaveModal() {
  // clear the input field for a fresh start
  document.getElementById("new-config-name").value = "";

  // show the modal using bootstrap's js api
  const modal = bootstrap.Modal.getOrCreateInstance(
      document.getElementById("saveConfigModal"),
  );
  modal.show();
}

/**
 * Validates the current UI inputs, compiles all configuration settings into a JSON payload,
 * and sends it to the backend API.
 * * If validation is successful, it initializes the simulation and redirects the user
 * to the appropriate progress page based on the selected simulation mode.
 */
function startSimulation() {
  // HALT if validation fails!
  if (!validateInputs()) return;

  // collect basic input params data
  let inputParams = {};
  const inputs = document.querySelectorAll('input[type="number"]');

  inputs.forEach((input) => {
    // Example: id="num_runways" -> inputParams["num_runways"] = 10
    if (input.id) {
      inputParams[input.id] = parseFloat(input.value);
    }
  });

  // process runway configuration
  let runwayData = [];
  const runwayCount = parseInt(runwayCounter.innerText);


  for (let i = 1; i <= runwayCount; i++) {
    const statusVal = document.getElementById(`status_${i}`).value;
    const modeVal = document.getElementById(`mode_${i}`).value;

    runwayData.push({
      runwayID: i - 1, // Java expects 0-based index
      status: statusToEnum(statusVal),
      mode: modeToEnum(modeVal),
    });
  }

  const processedEvents = formatEventsForBackend(scheduledEventsData);

  // compile the final payload object
  const payload = {
    runwaySettings: runwayData,
    scheduledRunwayEvents: processedEvents.runways,
    scheduledAircraftEvents: processedEvents.aircraft,
    simulationID: Date.now().toString(),

    // logic configuration
    automaticGenerationEnabled:
      document.getElementById("input-auto_gen")?.checked,
    seed: parseInt(document.getElementById("input-seed")?.value),
    tickTime: 1000,

    // parameters
    inboundRate: parseInt(document.getElementById("input-inbound_rate").value),
    outboundRate: parseInt(
      document.getElementById("input-outbound_rate").value,
    ),
    maxWaitTime: parseInt(document.getElementById("input-max_delay").value), // maybe could allow for indefinite wait time
    duration: parseInt(document.getElementById("input-sim_duration").value),

    // random-event multipliers
    mechanicalFailureMultiplier: parseFloat(
      document.getElementById("input-mech_failure_rate")?.value,
    ),
    passengerHealthIssueMultiplier: parseFloat(
      document.getElementById("input-health_issue_rate")?.value,
    ),
    runwayInspectionMultiplier: parseFloat(
      document.getElementById("input-inspection_rate")?.value,
    ),
    snowClearanceMultiplier: parseFloat(
      document.getElementById("input-snow_rate")?.value,
    ),
    equipmentFailureMultiplier: parseFloat(
      document.getElementById("input-equip_failure_rate")?.value,
    ),

    // force to uppercase to match the java enums
    simulationMode: document
      .getElementById("simulation-mode")
      .value.toUpperCase(),
  };

  // save to session storage so it can be restored if the user aborts
  sessionStorage.setItem("draftConfig", JSON.stringify(payload));

  //console.log("Sending Payload:", payload); // Debug check

  // validate configuration via api
  fetch("/api/configuration/validate", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })
    .then(async (response) => {
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          errorText ||
            `Failed to validate configuration (HTTP ${response.status})`,
        );
      }
      return response.text();
    })
    .then((data) => {
      //console.log("Configuration validated:", data);
      // if valid, initialize the simulation
      return fetch("/api/simulation/initialise", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });
    })
    .then(async (response) => {
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          errorText ||
            `Failed to initialise simulation (HTTP ${response.status})`,
        );
      }
      return response.text();
    })
    .then((data) => {
      // console.log("Simulation initialised:", data);
      // redirect to the correct progress view
      const simMode = payload.simulationMode;

      if (simMode == "QUICK_SIM") {
        window.location.href = "/quick-progress.html";
      } else {
        window.location.href = "/tabular-progress.html";
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("Error: " + error.message);
    });
}

function generateRandomSeed() {
  // generates a random number
  const randomSeed = Math.floor(Math.random() * 10000000);
  document.getElementById("input-seed").value = randomSeed;
}

// initialisation
generateInputs();
initialiseHelpBubbles();
setupRateToggle();

// EVENTS - SPRINT 2
/**
 * Defines the available event sub-types based on the primary category (Runway vs Aircraft).
 * Used to dynamically populate the event name dropdown.
 * @type {Object<string, string[]>}
 */
const eventOptions = {
  Runway: [
    "No Change",
    "Available",
    "Runway Inspection",
    "Snow Clearance",
    "Equipment Failure",
  ],
  Aircraft: ["Mechanical Failure", "Passenger Health"],
};

/**
 * Array holding all user-scheduled events before they are sent to the backend.
 * @type {Array<Object>}
 */
let scheduledEventsData = [];

/**
 * Validates form inputs, checks for time overlaps, and adds a new custom event
 * to the scheduledEventsData array and the UI list.
 */
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
  const simLimit =
    simInput && simInput.value !== "" ? parseInt(simInput.value) : 999999;

  // validate the start time input
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
  const eventModeVal =
    eventType === "Runway" ? (rawMode === "null" ? null : rawMode) : "MIXED";
  const eventModeLabel =
    eventType === "Runway"
      ? modeSelect.options[modeSelect.selectedIndex].text
      : "";

  // validate the duration input based on event type
  let eventDuration;
  if (eventType === "Aircraft") {
    eventDuration = -1; // aircraft events automatically indefinite duration
  } else {
    // default to -1 (indefinite) if left blank
    eventDuration = durationInput.value === "" ? -1 : parseInt(durationInput.value);

    // prevent zero or negative typed durations
    if (eventDuration !== -1 && eventDuration <= 0) {
      alert(
        "Duration must be at least 1 minute, or left blank for Indefinite.",
      );
      durationInput.focus();
      return;
    }

    // limits duration to max simulation duration - logical fix
    if (eventDuration !== -1 && eventDuration > 1440) {
      alert("Duration cannot exceed 1440 minutes.");
      durationInput.focus();
      return;
    }
  }
  // check for overlapping runway events
  if (eventType === "Runway") {
    const newStart = startTime;
    const newEnd = eventDuration === -1 ? Infinity : startTime + eventDuration;

    const hasOverlap = scheduledEventsData.some((ev) => {
      // only check for overlaps on the exact same runway
      if (ev.type !== "Runway" || ev.locationId !== eventLocId) return false;

      const existingStart = parseInt(ev.time);
      const existingEnd =
        parseInt(ev.duration) === -1
          ? Infinity
          : existingStart + parseInt(ev.duration);

      // check if the time ranges intersect
      return newStart < existingEnd && existingStart < newEnd;
    });

    if (hasOverlap) {
      alert(
        `Error: This event overlaps with an existing event on ${eventLocText}.`,
      );
      return;
    }
  }

  const uniqueId = Date.now();

  // push the validated event to the tracking array
  scheduledEventsData.push({
    id: uniqueId,
    type: eventType,
    name: eventEnum,
    locationId: eventLocId,
    time: startTime,
    duration: eventDuration,
    mode: eventModeVal,
    modeLabel: eventModeLabel,
  });

  if (emptyMsg) emptyMsg.remove();

  // construct the ui row for the new event
  const row = document.createElement("div");
  row.className =
    "d-flex align-items-center justify-content-between small mb-2 pb-2 border-bottom pe-1";
  row.setAttribute("data-id", uniqueId.toString());

  // calculate how the duration should be displayed
  let durationDisplay = eventDuration === -1 ? "∞" : `${startTime + eventDuration}m`;

  // apply visual badges for runway modes
  let modeBadge =
    eventType === "Runway" ? `<span class="badge bg-info text-dark ms-1">${eventModeLabel || "No Change"}</span>` : "";

  // shorten labels to fit
  let shortLabel = displayLabel
    .replace("Equipment Failure", "Equip. Failure")
    .replace("Runway Inspection", "Inspection");

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

  // reset form inputs after successful submission
  timeInput.value = "";
  durationInput.value = "";
  modeSelect.value = "null";
  nameSelect.value = "No Change";
  nameSelect.disabled = false;
  modeSelect.disabled = false;
}


/**
 * Removes a scheduled event from both the tracking array and the UI.
 * @param {number} idToDelete - The unique timestamp ID of the event.
 */
function deleteEvent(idToDelete) {
  // keep everything that does not match the ID
  scheduledEventsData = scheduledEventsData.filter(
    (item) => item.id !== idToDelete,
  );

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

/**
 * Maps frontend runway status strings to backend Java enums.
 * @param {string} statusVal - The raw string from the UI.
 * @returns {string} The formatted enum string.
 */
function statusToEnum(statusVal) {
  const mapping = {
    available: "AVAILABLE",
    snowclearance: "SNOW_CLEARANCE", // Updated: was SNOWCLEARANCE
    inspection: "INSPECTION",
    failure: "EQUIPMENT_FAILURE", // Updated: was FAILURE
  };
  return mapping[statusVal.toLowerCase()] || "AVAILABLE";
}

/**
 * Maps frontend runway mode strings to backend Java enums.
 * @param {string} modeVal - The raw string from the UI.
 * @returns {string} The formatted enum string.
 */
function modeToEnum(modeVal) {
  const mapping = {
    mixed: "MIXED",
    landing: "LANDING",
    takeoff: "TAKEOFF",
  };
  return mapping[modeVal.toLowerCase()] || "MIXED";
}

/**
 * Lookup map for converting backend status enums back to frontend HTML values.
 * @type {Object<string, string>}
 */
const backendToStatusHTML = {
  AVAILABLE: "available",
  SNOW_CLEARANCE: "snowclearance",
  INSPECTION: "inspection",
  EQUIPMENT_FAILURE: "failure",
};

/**
 * Lookup map for converting backend mode enums back to frontend HTML values.
 * @type {Object<string, string>}
 */
const backendToModeHTML = {
  MIXED: "mixed",
  LANDING: "landing",
  TAKEOFF: "takeoff",
};

/**
 * Processes the raw frontend event data into the strict map structure required by the Java backend.
 * Separates events into runway and aircraft categories, keyed by their execution tick.
 * @param {Array<Object>} frontendEvents - The array of scheduled events.
 * @returns {Object} An object containing the formatted `runways` and `aircraft` maps.
 */
function formatEventsForBackend(frontendEvents) {
  let runwayMap = {};
  let aircraftMap = {};

  frontendEvents.forEach((ev) => {
    const tickTime = parseInt(ev.time);

    if (ev.type === "Runway") {
      let status = "AVAILABLE";

      if (ev.name === "No Change") {
        status = null;
      } else if (ev.name === "Runway Inspection") {
        status = "INSPECTION";
      } else if (ev.name === "Snow Clearance") {
        status = "SNOW_CLEARANCE"; // UPDATED TO MATCH MAIN
      } else if (ev.name === "Equipment Failure") {
        status = "EQUIPMENT_FAILURE"; // UPDATED TO MATCH MAIN
      }

      // initialise the array for this tick if it doesn't exist
      if (!runwayMap[tickTime]) runwayMap[tickTime] = [];

      runwayMap[tickTime].push({
        tick: tickTime,
        runwayID: parseInt(ev.locationId),
        status: status,
        mode: ev.mode || "MIXED",
        type: "SCHEDULED_CHANGE",
        duration: ev.duration,
      });
    } else if (ev.type === "Aircraft") {
      let status = "NONE";
      if (ev.name === "Mechanical Failure") status = "MECHANICAL";
      else if (ev.name === "Passenger Health") status = "PASSENGER";

      // initialise the array for this tick if it doesn't exist
      if (!aircraftMap[tickTime]) aircraftMap[tickTime] = [];
      aircraftMap[tickTime].push({
        tick: tickTime,
        callsign: null,
        type: "SCHEDULED_EMERGENCY",
        status: status,
      });
    }
  });
  return { runways: runwayMap, aircraft: aircraftMap };
}

/**
 * Updates the available options in the event creation form based on the selected event type.
 * Toggles the visibility of the mode and duration fields (hidden for Aircraft events).
 */
function updateEventForm() {
  const typeSelect = document.getElementById("new-event-type");
  const nameSelect = document.getElementById("new-event-name");
  const locSelect = document.getElementById("new-event-loc");
  const selectedType = typeSelect.value;

  // update event names dropdown
  nameSelect.innerHTML = "";
  eventOptions[selectedType].forEach((eventName) => {
    const option = document.createElement("option");
    option.value = eventName; // Keeps the text like ("Snow Clearance")
    option.innerText = eventName;
    nameSelect.appendChild(option);
  });

  // update locations dropdown based on active runways or holding pattern
  locSelect.innerHTML = "";
  if (selectedType === "Runway") {
    const count = parseInt(runwayCounter.innerText);
    for (let i = 1; i <= count; i++) {
      const option = document.createElement("option");
      option.value = i - 1;
      option.innerText = "Runway " + i;
      locSelect.appendChild(option);
    }
  } else {
    const option = document.createElement("option");
    option.value = "Holding Pattern";
    option.innerText = "Holding Pattern";
    locSelect.appendChild(option);
  }

  // toggle mode container visibility
  const modeContainer = document.getElementById("mode-container");
  if (modeContainer) {
    if (selectedType === "Aircraft") {
      modeContainer.style.display = "none"; // hide for Aircraft
    } else {
      modeContainer.style.display = "block"; // show for Runway
    }
  }

  // toggle duration container visibility
  const durationContainer = document.getElementById("duration-container");
  if (durationContainer) {
    if (selectedType === "Aircraft") {
      durationContainer.style.display = "none"; // removes duration
    } else {
      durationContainer.style.display = "block"; // adds duration
    }
  }

  // Reset disabled states and mode value when form type changes.
  nameSelect.disabled = false;
  document.getElementById("new-event-mode").disabled = false;
  document.getElementById("new-event-mode").value = "null";
}

/**
 * Event listener that locks the runway mode to "No Change" if a closure status is selected.
 */
document.getElementById("new-event-name").addEventListener("change", function () {
    const closureStatuses = [
      "Runway Inspection",
      "Snow Clearance",
      "Equipment Failure",
    ];
    const modeSelect = document.getElementById("new-event-mode");

    // lock the mode dropdown if the runway is being closed
    if (closureStatuses.includes(this.value)) {
      modeSelect.value = "null";
      modeSelect.disabled = true;
    } else {
      modeSelect.disabled = false;
    }
  });

/**
 * Event listener that locks the status to "Available" if a mode other than "No Change" is selected.
 */
document.getElementById("new-event-mode").addEventListener("change", function () {
    const nameSelect = document.getElementById("new-event-name");

    if (this.value !== "null") {
      nameSelect.value = "Available";
      nameSelect.disabled = true;
    } else {
      nameSelect.disabled = false;
    }
  });

/**
 * Loads a full configuration object into the UI, updating all inputs,
 * runway rows, and scheduled events to match the saved data.
 * @param {Object} data - The configuration data object.
 */
function applyConfigToPage(data) {
  // map json fields to html element ids
  const fieldMapping = {
    "simulation-mode": data.simulationMode,
    "input-inbound_rate": data.inboundRate,
    "input-outbound_rate": data.outboundRate,
    "input-sim_duration": data.duration,
    "input-max_delay": data.maxWaitTime,
    "input-seed": data.seed,
    "input-mech_failure_rate":
      data.mechanicalFailureMultiplier ??
      data.mechanicalFailureRate ??
      1.0,
    "input-health_issue_rate":
      data.passengerHealthIssueMultiplier ??
      data.passengerHealthIssueRate ??
      1.0,
    "input-inspection_rate":
      data.runwayInspectionMultiplier ??
      data.runwayInspectionRate ??
      1.0,
    "input-snow_rate":
      data.snowClearanceMultiplier ??
      data.snowClearanceRate ??
      1.0,
    "input-equip_failure_rate":
      data.equipmentFailureMultiplier ??
      data.equipmentFailureRate ??
      1.0,
  };

  // update basic inputs
  Object.keys(fieldMapping).forEach((id) => {
    const el = document.getElementById(id);
    const value = fieldMapping[id];
    if (el && value !== undefined) {
      el.value = value;
    }
  });
  // check the auto generation toggle
  const autoGenCheckbox = document.getElementById("input-auto_gen");
  if (autoGenCheckbox) {
    autoGenCheckbox.checked = data.automaticGenerationEnabled === true;
  }
  // add or remove runways to match the configuration
  const targetRunways = data.runwaySettings ? data.runwaySettings.length : 1;
  while (number > targetRunways) minus1();
  while (number < targetRunways) add1();
  // apply statuses and modes to each runway
  if (data.runwaySettings) {
    data.runwaySettings.forEach((runway, index) => {
      const i = index + 1;
      const statusEl = document.getElementById(`status_${i}`);
      const modeEl = document.getElementById(`mode_${i}`);

      if (statusEl)
        statusEl.value = backendToStatusHTML[runway.status] || "available";
      if (modeEl && runway.mode)
        modeEl.value = backendToModeHTML[runway.mode] || "mixed";
    });
  }
  // clear existing events
  scheduledEventsData = [];

  const backendToEventName = {
    AVAILABLE: "Available",
    INSPECTION: "Runway Inspection",
    SNOW_CLEARANCE: "Snow Clearance",
    EQUIPMENT_FAILURE: "Equipment Failure",
    MECHANICAL: "Mechanical Failure",
    PASSENGER: "Passenger Health",
  };

  const backendToModeName = {
    MIXED: "Mixed",
    LANDING: "Landing",
    TAKEOFF: "Take-Off",
  };
  // process scheduled runway events
  if (data.scheduledRunwayEvents) {
    //console.log("FULL DATA scheduledRunwayEvents:", JSON.stringify(data.scheduledRunwayEvents));

    Object.keys(data.scheduledRunwayEvents).forEach((tick) => {
      data.scheduledRunwayEvents[tick].forEach((ev) => {
        const rawStatus = ev.status ?? ev.runwayStatus ?? null;
        const rawMode = ev.mode ?? ev.runwayMode ?? null;

        const safeStatus = rawStatus ? rawStatus.toUpperCase() : null;
        const safeMode = rawMode ? rawMode.toUpperCase() : null;
        // push to the frontend tracking array
        scheduledEventsData.push({
          id: Date.now() + Math.floor(Math.random() * 1000),
          type: "Runway",
          name: safeStatus ? backendToEventName[safeStatus] : "No Change",
          rawStatus: safeStatus, // THE FIX: Hidden Enum for saving
          mode: safeMode || "null",
          modeLabel: safeMode ? backendToModeName[safeMode] : "No Change",
          rawMode: safeMode, // THE FIX: Hidden Enum for saving
          locationId: ev.runwayID.toString(),
          locationName: "Runway " + (ev.runwayID + 1),
          time: tick.toString(),
          duration: ev.duration ? ev.duration.toString() : "-1",
        });
      });
    });
  }

  // process scheduled aircraft events
  if (data.scheduledAircraftEvents) {
    Object.keys(data.scheduledAircraftEvents).forEach((tick) => {
      data.scheduledAircraftEvents[tick].forEach((ev) => {
        const rawStatus = ev.status || ev.aircraftStatus;
        const safeStatus = rawStatus ? rawStatus.toUpperCase() : null;
        // push to the frontend tracking array
        scheduledEventsData.push({
          id: Date.now() + Math.floor(Math.random() * 1000),
          type: "Aircraft",
          name: safeStatus
            ? backendToEventName[safeStatus]
            : "Mechanical Failure",
          rawStatus: safeStatus, // THE FIX: Hidden Enum for saving
          mode: "N/A",
          locationId: "Holding Pattern",
          locationName: "Holding Pattern",
          time: tick.toString(),
          duration: "N/A",
        });
      });
    });
  }
  // rebuild the ui list
  rebuildEventListUI();
  document.getElementById("input-auto_gen").dispatchEvent(new Event("change"));

  // hide the load modal
  document.activeElement.blur();
  const modal = bootstrap.Modal.getInstance(
    document.getElementById("loadConfigModal"),
  );
  if (modal) modal.hide();
}

/**
 * Fetches a specific configuration template from the backend API and applies it to the page.
 * @param {string} name - The unique name of the configuration template to load.
 */
function loadFullConfig(name) {
  // fetch the configuration data
  fetch(`/api/configuration/templates/vieworload/${name}`)
    .then((response) => {
      if (!response.ok) throw new Error("Could not find configuration " + name);
      return response.json();
    })
    .then((fullData) => {
      // apply the fetched data to the ui
      applyConfigToPage(fullData);

      // close the modal
      const modal = bootstrap.Modal.getInstance(
        document.getElementById("loadConfigModal"),
      );
      if (modal) modal.hide();
    })
    .catch((err) => alert(err.message));
}

/**
 * Prompts the user to confirm, then clears the current draft configuration
 * and reloads the page to reset all fields to their defaults.
 */
function resetConfig() {
  if (confirm("Are you sure you want to reset all configurations to their default values?")) {
    // destroy the draft so it doesn't try to load it again
    sessionStorage.removeItem("draftConfig");
    // reload the page to reset all html elements and js arrays
    window.location.reload();
  }
}

/**
 * The field used for sorting configuration summaries.
 * @type {string|null}
 */
let configSortField = null;
/**
 * Boolean representing if the current sort order is ascending.
 * @type {boolean}
 */
let configSortAsc = true;

/**
 * Generates the HTML table rows for the saved configuration summaries in the load modal.
 * @param {Array<Object>} summaries - The array of configuration summary objects.
 */
function renderConfigRows(summaries) {
  const list = document.getElementById("saved-configs-list");
  const emptyMsg = document.getElementById("empty-configs-msg");

  // clear existing rows
  list.querySelectorAll("tr.config-row").forEach((row) => row.remove());
  // show empty message if no data exists
  if (!summaries || summaries.length === 0) {
    emptyMsg.style.display = "";
    return;
  }
  emptyMsg.style.display = "none";

  // build a row for each summary
  summaries.forEach((summary) => {
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
        <td class="config-options-cell" style="vertical-align:middle; white-space:nowrap; text-align: center;">
            <div class="config-options-actions">
                <button class="btn-option btn-compare load-btn" title="Load this configuration">Load</button>
                <button class="btn-option btn-delete delete-btn" title="Delete this configuration">Delete</button>
            </div>
        </td>
        `;
    // add event listeners
    row.querySelector(".load-btn").onclick = function () {
      loadFullConfig(summary.templateName);
    };
    row.querySelector(".delete-btn").onclick = function () {
      if (confirm(`Delete "${summary.templateName}"?`)) {
        fetch(`/api/configuration/templates/delete/${summary.templateName}`, {
          method: "DELETE",
        }).then(() => openLoadConfigModal());
      }
    };
    list.appendChild(row);
  });
}

/**
 * Sorts the configuration summaries array based on the specified field and direction.
 * @param {Array<Object>} summaries - The array to sort.
 * @param {string} field - The field to sort by ("name" or "date").
 * @param {boolean} asc - True for ascending, false for descending.
 * @returns {Array<Object>} The newly sorted array.
 */
function sortConfigs(summaries, field, asc) {
  return [...summaries].sort((a, b) => {
    let valA =
      field === "name" ? a.templateName.toLowerCase() : new Date(a.dateCreated);
    let valB =
      field === "name" ? b.templateName.toLowerCase() : new Date(b.dateCreated);
    if (valA < valB) return asc ? -1 : 1;
    if (valA > valB) return asc ? 1 : -1;
    return 0;
  });
}

/**
 * Updates the sort indicators (arrows) in the table headers.
 * @param {string} field - The currently active sort field.
 */
function updateSortHeaders(field) {
  document.querySelectorAll(".sort-btn").forEach((btn) => {
    const btnField = btn.getAttribute("data-field");
    if (btnField === field) {
      btn.innerText = configSortAsc ? "↑" : "↓";
    } else {
      btn.innerText = "↑↓";
    }
  });
}

/**
 * Caches the currently loaded configuration summaries to allow for local sorting.
 * @type {Array<Object>}
 */
let currentSummaries = [];

/**
 * Fetches all saved configuration summaries from the backend and opens the load modal.
 */
function openLoadConfigModal() {
  // reset sorting state
  configSortField = null;
  configSortAsc = true;

  // fetch summaries from the api
  fetch("/api/configuration/templates/summaries")
    .then((response) => response.json())
    .then((summaries) => {
      // store and render the fetched summaries
      currentSummaries = summaries || [];
      renderConfigRows(currentSummaries);
      // setup sorting buttons
      document.querySelectorAll(".sort-btn").forEach((btn) => {
        btn.innerText = "↑↓";
        btn.onclick = function () {
          const field = btn.getAttribute("data-field");
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
    .catch((err) => {
      console.error("Failed to load summaries:", err);
      document.getElementById("empty-configs-msg").style.display = "";
    });
  // show the modal
  bootstrap.Modal.getOrCreateInstance(
    document.getElementById("loadConfigModal"),
  ).show();
}

/**
 * Rebuilds the scheduled events UI list, applying visual warnings for invalid events.
 * Flags "ghost" events (assigned to runways that no longer exist) and "late" events
 * (scheduled after the simulation duration ends).
 */
function rebuildEventListUI() {
  const list = document.getElementById("scheduled-events-list");
  // get the current runway count to check for ghost events
  const currentRunwayCount = parseInt(
    document.getElementById("runway-counter").innerText,
  );
  if (!list) return;

  // clear the existing events list
  list.innerHTML = "";

  // show empty message if no events exist
  if (scheduledEventsData.length === 0) {
    const emptyMsg = document.createElement("div");
    emptyMsg.id = "empty-list-msg";
    emptyMsg.className = "text-center text-muted small mt-5";
    emptyMsg.innerText = "No events scheduled";
    list.appendChild(emptyMsg);
    return;
  }

  // iterate through scheduled events to build the ui
  scheduledEventsData.forEach((ev) => {
    // check if the event is assigned to a removed runway
    const isGhost = ev.type === "Runway" && parseInt(ev.locationId) >= currentRunwayCount;

    // check if the event is scheduled after simulation duration
    const isLate = !isGhost && invalidEventIds.has(ev.id);

    // construct the html row with appropriate warning classes
    const row = document.createElement("div");
    row.className = `d-flex align-items-center justify-content-between small mb-2 pb-2 border-bottom ${isGhost ? "ghost-event" : ""} ${isLate ? "late-event" : ""}`;
    row.setAttribute("data-id", ev.id.toString());

    let parsedDuration = parseInt(ev.duration);
    let start = parseInt(ev.time);
    let durationDisplay =
      parsedDuration === -1 || isNaN(parsedDuration)
        ? "∞"
        : `${start + parsedDuration}m`;

    let modeBadge =
      ev.type === "Runway"
        ? `<span class="badge bg-info text-dark ms-1">${ev.modeLabel || "No Change"}</span>`
        : "";
    let eventLocText =
      ev.type === "Runway"
        ? "Runway " + (parseInt(ev.locationId) + 1)
        : ev.locationId;
    let warning = isGhost
      ? `<span class="ghost-badge ms-1">INVALID RUNWAY</span>`
      : "";
    let lateWarning = isLate
      ? `<span class="late-badge ms-1">PAST DURATION</span>`
      : "";
    let shortName = ev.name
      .replace("Equipment Failure", "Equip. Failure")
      .replace("Runway Inspection", "Inspection");

    row.innerHTML = `
            <div class="ps-1 pe-2" style="flex: 1; min-width: 0;">
                <div class="d-flex flex-wrap align-items-center">
                    <strong class="me-1 ${isGhost ? "text-danger" : isLate ? "text-warning" : ""}">${shortName}</strong> ${modeBadge} ${warning} ${lateWarning}
                </div>
                <span class="text-muted d-block" style="font-size: 0.75rem;">on ${eventLocText}</span>
            </div>
            <div class="text-end flex-shrink-0 px-2 d-flex align-items-center justify-content-end" style="min-width: 95px;">
                <div class="fw-bold ${isGhost ? "text-danger" : isLate ? "text-warning" : "text-dark"} text-nowrap" style="font-size: 0.85rem;">${start}m - ${durationDisplay}</div>
            </div>
            <div class="flex-shrink-0" style="width: 25px; text-align: right;">
                <button class="btn btn-link text-danger p-0 border-0 fs-5" onclick="deleteEvent(${ev.id})">&times;</button>
            </div>
        `;
    list.appendChild(row);
  });
}

/**
 * Initialises page state when the DOM is fully loaded.
 * Restores draft configurations, sets up input blockers, and attaches dynamic UI listeners.
 */
document.addEventListener("DOMContentLoaded", () => {
  // setup the drop downs
  updateEventForm();

  // check if there was a saved draft from local storage
  const savedDraft = sessionStorage.getItem("draftConfig");
  // check if the user just aborted from a simulation
  const wasAborted = sessionStorage.getItem("wasAborted");

  // restore their previous configuration only if they've just come after stopping the simulation
  if (savedDraft && wasAborted === "true") {
    try {
      const draftData = JSON.parse(savedDraft);
      applyConfigToPage(draftData);
      //console.log("Restored previous configuration because simulation was aborted.",);
    } catch (e) {
      console.error("Failed to restore draft config", e);
    }
  }

  // set placeholder and block invalid characters for event inputs
  const timeInput = document.getElementById("new-event-time");
  const durationInput = document.getElementById("new-event-duration");

  if (durationInput) {
    durationInput.placeholder = "Indefinite";
  }

  [timeInput, durationInput].forEach((input) => {
    if (input) {
      // block non digit symbols
      input.addEventListener("keydown", function (e) {
        if (["e", "E", "+", "-"].includes(e.key)) {
          e.preventDefault();
        }
      });
    }
  });

  // lock 'mode' if the runway is closed
  const nameSelect = document.getElementById("new-event-name");
  const modeSelect = document.getElementById("new-event-mode");

  if (nameSelect && modeSelect) {
    nameSelect.addEventListener("change", function () {
      if (
        ["Runway Inspection", "Snow Clearance", "Equipment Failure"].includes(
          this.value,
        )
      ) {
        modeSelect.value = "null"; // force to "no change"
        modeSelect.disabled = true; // lock the dropdown
      } else {
        modeSelect.disabled = false; // Unlock it for available/no change
      }
    });
  }
  // clear the abort flag so it doesn't loop
  sessionStorage.removeItem("wasAborted");
});

/**
 * Safely parses a value into a base-10 integer.
 * Returns a specified default value if the parsed result is not a valid number.
 * @param {string|number} value - The value to parse.
 * @param {number} defaultValue - The fallback value to return if parsing fails.
 * @returns {number} The parsed integer or the default value.
 */
function parseIntOrDefault(value, defaultValue) {
  // attempt to parse the value as a base-10 integer
  const parsed = parseInt(value, 10);
  // return the fallback if it's not a number, otherwise return the parsed integer
  return Number.isNaN(parsed) ? defaultValue : parsed;
}

/**
 * Gathers all current configuration inputs, formats them into a JSON payload,
 * and sends a POST request to save the template to the backend database.
 */
function saveConfiguration() {
  // Stop if validation fails
  if (!validateInputs()) return;

  // get the configuration name
  const nameInput = document.getElementById("new-config-name");
  const configName = nameInput ? nameInput.value.trim() : "";
  if (!configName) return alert("Please enter a name");

  // gather runway settings
  const runwayCount =
    parseInt(document.getElementById("runway-counter").innerText) || 1;
  const runwaySettings = [];
  for (let i = 1; i <= runwayCount; i++) {
    const statusVal = document.getElementById(`status_${i}`).value;
    const modeVal = document.getElementById(`mode_${i}`).value;
    runwaySettings.push({
      runwayID: i - 1,
      status: statusToEnum(statusVal),
      mode: modeToEnum(modeVal),
    });
  }

  // format scheduled events
  const backendEvents = formatEventsForBackend(scheduledEventsData);

  // parse the seed value
  const seedRaw = document.getElementById("input-seed")?.value;
  const seed = seedRaw !== "" && seedRaw != null ? parseInt(seedRaw) : 0;

  // compile the final payload
  const payload = {
    templateName: configName,
    runwaySettings: runwaySettings,
    inboundRate: parseIntOrDefault(
      document.getElementById("input-inbound_rate").value,
      15
    ),
    outboundRate: parseIntOrDefault(
      document.getElementById("input-outbound_rate").value,
      15
    ),
    maxWaitTime: parseIntOrDefault(
      document.getElementById("input-max_delay").value,
      30
    ),
    duration: parseIntOrDefault(
      document.getElementById("input-sim_duration").value,
      120
    ),
    tickTime: 1000,
    automaticGenerationEnabled:
      document.getElementById("input-auto_gen").checked || false,
    seed: seed,
    mechanicalFailureMultiplier:
      parseFloat(document.getElementById("input-mech_failure_rate").value) ||
      0.0,
    passengerHealthIssueMultiplier:
      parseFloat(document.getElementById("input-health_issue_rate").value) ||
      0.0,
    runwayInspectionMultiplier:
      parseFloat(document.getElementById("input-inspection_rate").value) || 0.0,
    snowClearanceMultiplier:
      parseFloat(document.getElementById("input-snow_rate").value) || 0.0,
    equipmentFailureMultiplier:
      parseFloat(document.getElementById("input-equip_failure_rate").value) ||
      0.0,
    simulationMode: document
      .getElementById("simulation-mode")
      .value.toUpperCase(),
    scheduledRunwayEvents: backendEvents.runways || {},
    scheduledAircraftEvents: backendEvents.aircraft || {},
  };

  // shows exact payload in console before sending - remove once working
  //console.log("Save payload:", JSON.stringify(payload, null, 2));

  fetch("/api/configuration/save", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  })
    .then(async (res) => {
      if (!res.ok) {
        const errText = await res.text();
        console.error("Full server error:", errText);
        throw new Error(errText || "400 Bad Request");
      }
      alert("Configuration saved successfully!");
      const modal = bootstrap.Modal.getInstance(
        document.getElementById("saveConfigModal"),
      );
      if (modal) modal.hide();
    })
    .catch((err) => {
      console.error("Save Error:", err);
      alert("Server rejected save: " + err.message);
    });
}

// VALIDATION
/**
 * Validates all numerical inputs against their min/max constraints and checks
 * scheduled events for invalid runway assignments or out-of-bounds times.
 * @returns {boolean} True if all inputs are valid, false otherwise.
 */
function validateInputs() {
  const inputs = document.querySelectorAll(".field-input");
  let isValid = true;
  let errorMessages = [];
  let firstInvalidInput = null;

  // validate input boxes
  for (let input of inputs) {
    const val = parseFloat(input.value);
    const min = parseFloat(input.min);
    const max = parseFloat(input.max);
    // check if the value is empty, non-numeric, or out of bounds
    if (input.value === "" || isNaN(val) || val < min || val > max) {
      input.style.border = "2px solid red"; // highlight invalid box
      isValid = false;
      if (!firstInvalidInput) firstInvalidInput = input;
    } else {
      input.style.border = ""; // remove highlight if valid
    }
  }
  // add error message for input box validation failures
  if (!isValid) {
    errorMessages.push(
      "Please fix the highlighted fields. Ensure numbers are within specified ranges.",
    );
  }

  // flagging invalid events
  invalidEventIds.clear(); // reset invalid events tracker
  const currentRunwayCount = parseInt(
    document.getElementById("runway-counter").innerText,
  );
  const simDurationEl = document.getElementById("input-sim_duration");
  const simLimit =
    simDurationEl && simDurationEl.value !== ""
      ? parseInt(simDurationEl.value)
      : null;

  let hasGhost = false;
  let hasLate = false;

  // flag ghost and late events
  scheduledEventsData.forEach((ev) => {
    const isGhost =
      ev.type === "Runway" && parseInt(ev.locationId) >= currentRunwayCount;
    const isLate = simLimit !== null && parseInt(ev.time) >= simLimit;
    if (isGhost || isLate) invalidEventIds.add(ev.id);
    if (isGhost) hasGhost = true;
    if (isLate) hasLate = true;
  });

  // add error messages
  if (hasGhost) {
    errorMessages.push(
      "You have events scheduled for runways that no longer exist. Please adjust the runway count or delete those events.",
    );
    isValid = false;
  }
  if (hasLate) {
    errorMessages.push(
      "You have events scheduled at or after the simulation ends. These will never occur. Please remove them or increase the simulation duration.",
    );
    isValid = false;
  }

  // rebuild the ui list to show the event warnings
  rebuildEventListUI();

  // display a single comprehensive alert to the user
  if (!isValid) {
    if (firstInvalidInput) firstInvalidInput.focus();
    alert(errorMessages.join("\n\n"));
    return false;
  }

  return true;
}

// attach the save logic to the confirm button
document.getElementById("confirm-save-btn").onclick = saveConfiguration;
