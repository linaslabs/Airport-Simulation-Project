const API = {
  summaries: "/api/results/summaries",
  lastResult: "/api/results/lastresult",
  viewOrCompare: (name) =>
    `/api/results/vieworcompare/${encodeURIComponent(name)}`,
  deleteResult: (name) => `/api/results/delete/${encodeURIComponent(name)}`,
};

let simulationSummaries = [];
const simulationDetailsCache = new Map();
let currentSimNames = { A: null, B: null };
let selectingFor = null;

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function enumToLabel(value) {
  if (!value) return "--";
  return String(value)
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatEmergencyType(value) {
  const normalized = value ? String(value).toUpperCase() : "";

  if (normalized === "MECHANICAL") {
    return "Mechanical Failure";
  }

  if (normalized === "PASSENGER") {
    return "Passenger Health";
  }

  if (normalized === "NONE") {
    return "None";
  }

  return enumToLabel(value);
}

function toNumber(value) {
  return typeof value === "number" && Number.isFinite(value) ? value : null;
}

function formatNumber(value, decimals = 2) {
  const n = toNumber(value);
  return n === null ? "--" : n.toFixed(decimals);
}

function formatInteger(value) {
  const n = toNumber(value);
  return n === null ? "--" : Math.round(n).toString();
}

function formatMultiplier(value) {
  const n = toNumber(value);
  return n === null ? "--" : `${n.toFixed(1)}x`;
}

function formatDate(dateValue) {
  if (!dateValue) return "--";
  const date = new Date(dateValue);
  if (Number.isNaN(date.getTime())) return "--";
  return date.toLocaleString();
}

function toReadableText(rawValue) {
  if (rawValue === null || rawValue === undefined || rawValue === "") {
    return "unknown";
  }

  const text = String(rawValue).toLowerCase();
  const parts = text.split("_");
  const outputParts = [];

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i];
    if (!part) continue;
    outputParts.push(part.charAt(0).toUpperCase() + part.slice(1));
  }

  return outputParts.join(" ");
}

function formatRawEventLog(rawEvents) {
  const formatted = [];
  if (!Array.isArray(rawEvents)) {
    return formatted;
  }

  const sorted = rawEvents.slice();
  sorted.sort(function (a, b) {
    const tickA =
      a && typeof a.tick === "number" ? a.tick : Number.MAX_SAFE_INTEGER;
    const tickB =
      b && typeof b.tick === "number" ? b.tick : Number.MAX_SAFE_INTEGER;
    return tickA - tickB;
  });

  for (let i = 0; i < sorted.length; i++) {
    const event = sorted[i] || {};
    const tick = typeof event.tick === "number" ? event.tick : "--";

    if (typeof event.runwayID === "number") {
      const runwayLabel = "Runway " + (event.runwayID + 1);
      const eventType = toReadableText(event.type);
      const status = toReadableText(event.runwayStatus);
      const mode = toReadableText(event.runwayMode);

      let duration = "duration: unknown";
      if (typeof event.duration === "number") {
        if (event.duration < 0) {
          duration = "duration: indefinite";
        } else if (event.duration === 0) {
          duration = "duration: unspecified";
        } else {
          duration = "duration: " + event.duration + " mins";
        }
      }

      formatted.push(
        "Minute " +
          tick +
          ": " +
          runwayLabel +
          " " +
          eventType +
          ". Status: " +
          status +
          ", mode: " +
          mode +
          ", " +
          duration +
          ".",
      );
    } else {
      const callsign = event.callsign
        ? "Aircraft " + event.callsign
        : "an aircraft";
      const eventType = toReadableText(event.type);
      const status = toReadableText(event.status);
      formatted.push(
        "Minute " +
          tick +
          ": " +
          callsign +
          " triggered " +
          eventType +
          " (" +
          status +
          ").",
      );
    }
  }

  return formatted;
}

function buildFallbackEventLogFromConfig(config) {
  const lines = [];

  if (!config) {
    return lines;
  }

  const scheduled = [];
  const runwaySettings = Array.isArray(config.runwaySettings)
    ? config.runwaySettings
    : [];

  function getInitialRunwaySetting(runwayId) {
    if (!Number.isInteger(runwayId)) return null;

    for (let i = 0; i < runwaySettings.length; i++) {
      const setting = runwaySettings[i];
      if (
        setting &&
        Number.isInteger(setting.runwayID) &&
        setting.runwayID === runwayId
      ) {
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
      const tick = Number.isFinite(event && event.tick)
        ? event.tick
        : Number.MAX_SAFE_INTEGER;
      const runwayId = Number.isInteger(event && event.runwayID)
        ? event.runwayID
        : Number.parseInt(runwayKey, 10);
      const eventTypeRaw =
        event && event.type ? String(event.type).toUpperCase() : "";

      scheduled.push({
        tick: tick,
        category: "runway",
        event: event || {},
        runwayId: Number.isInteger(runwayId) ? runwayId : null,
      });

      if (
        Number.isFinite(event && event.duration) &&
        event.duration > 0 &&
        eventTypeRaw !== "REVERSION"
      ) {
        const initialSetting = getInitialRunwaySetting(runwayId);
        scheduled.push({
          tick: tick + event.duration,
          category: "runway-reversion",
          runwayId: Number.isInteger(runwayId) ? runwayId : null,
          runwayStatus: initialSetting ? initialSetting.status : null,
          runwayMode: initialSetting ? initialSetting.mode : null,
        });
      }
    });
  });

  const aircraftEventsByCallsign = config.scheduledAircraftEvents || {};
  Object.entries(aircraftEventsByCallsign).forEach(function ([
    callsignKey,
    events,
  ]) {
    if (!Array.isArray(events)) return;

    events.forEach(function (event) {
      const tick = Number.isFinite(event && event.tick)
        ? event.tick
        : Number.MAX_SAFE_INTEGER;
      scheduled.push({
        tick: tick,
        category: "aircraft",
        event: event || {},
        callsign: event && event.callsign ? event.callsign : callsignKey,
      });
    });
  });

  scheduled.sort(function (a, b) {
    return a.tick - b.tick;
  });

  for (let i = 0; i < scheduled.length; i++) {
    const item = scheduled[i];
    const tick = item.tick === Number.MAX_SAFE_INTEGER ? "--" : item.tick;

    if (item.category === "runway") {
      const runwayLabel =
        item.runwayId === null ? "Runway ?" : "Runway " + (item.runwayId + 1);
      const eventType = toReadableText(item.event.type);
      const status = toReadableText(item.event.runwayStatus);
      const mode = toReadableText(item.event.runwayMode);

      let durationText = "duration: unknown";
      if (typeof item.event.duration === "number") {
        if (item.event.duration < 0) {
          durationText = "duration: indefinite";
        } else if (item.event.duration === 0) {
          durationText = "duration: immediate";
        } else {
          durationText = "duration: " + item.event.duration + " mins";
        }
      }

      lines.push(
        "Minute " +
          tick +
          ": " +
          runwayLabel +
          " " +
          eventType +
          ". Status: " +
          status +
          ", mode: " +
          mode +
          ", " +
          durationText +
          ".",
      );
    } else if (item.category === "runway-reversion") {
      const runwayLabel =
        item.runwayId === null ? "Runway ?" : "Runway " + (item.runwayId + 1);
      const status = toReadableText(item.runwayStatus);
      const mode = toReadableText(item.runwayMode);
      lines.push(
        "Minute " +
          tick +
          ": " +
          runwayLabel +
          " Reversion. Status: " +
          status +
          ", mode: " +
          mode +
          ", duration: immediate.",
      );
    } else {
      const callsign = item.callsign
        ? "aircraft " + item.callsign
        : "an aircraft";
      const eventType = toReadableText(item.event.type);
      const status = toReadableText(item.event.status);
      lines.push(
        "Minute " +
          tick +
          ": " +
          callsign +
          " " +
          eventType +
          " (" +
          status +
          ").",
      );
    }
  }

  return lines;
}

function getEventLogLines(savedResult, config) {
  if (
    savedResult &&
    Array.isArray(savedResult.eventLog) &&
    savedResult.eventLog.length > 0
  ) {
    return savedResult.eventLog;
  }

  if (
    savedResult &&
    savedResult.log &&
    Array.isArray(savedResult.log.eventLog)
  ) {
    const formatted = formatRawEventLog(savedResult.log.eventLog);
    if (formatted.length > 0) {
      return formatted;
    }
  }

  return buildFallbackEventLogFromConfig(config);
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `Request failed (${response.status})`);
  }
  return response.json();
}

function buildEventRows(config) {
  const runwayRows = [];
  const aircraftRows = [];

  const scheduledRunwayEvents = config?.scheduledRunwayEvents || {};
  Object.entries(scheduledRunwayEvents).forEach(([runwayKey, events]) => {
    if (!Array.isArray(events)) return;

    events.forEach((event) => {
      const runwayId = Number.isInteger(event?.runwayID)
        ? event.runwayID
        : Number.parseInt(runwayKey, 10);
      const runwayLabel = Number.isInteger(runwayId)
        ? `Runway ${runwayId + 1}`
        : `Runway ${runwayKey}`;
      const tick = Number.isFinite(event?.tick) ? event.tick : "--";
      const duration =
        event?.duration === -1 || event?.duration == null
          ? "Indefinite"
          : `${event.duration} mins`;

      runwayRows.push({
        event: `${enumToLabel(event?.type)} on ${runwayLabel}`,
        time: `${tick} mins`,
        duration,
        runwayMode: enumToLabel(event?.runwayMode),
        status: enumToLabel(event?.runwayStatus),
        tickSort: Number.isFinite(event?.tick)
          ? event.tick
          : Number.MAX_SAFE_INTEGER,
      });
    });
  });

  const scheduledAircraftEvents = config?.scheduledAircraftEvents || {};
  Object.entries(scheduledAircraftEvents).forEach(([, events]) => {
    if (!Array.isArray(events)) return;

    events.forEach((event) => {
      const tick = Number.isFinite(event?.tick) ? event.tick : "--";
      const callsign = event?.callsign;

      aircraftRows.push({
        event: callsign
          ? `${enumToLabel(event?.type)} (${callsign})`
          : enumToLabel(event?.type),
        time: `${tick} mins`,
        emergencyType: formatEmergencyType(event?.status),
        tickSort: Number.isFinite(event?.tick)
          ? event.tick
          : Number.MAX_SAFE_INTEGER,
      });
    });
  });

  runwayRows.sort((a, b) => a.tickSort - b.tickSort);
  aircraftRows.sort((a, b) => a.tickSort - b.tickSort);

  return {
    runway: runwayRows,
    aircraft: aircraftRows,
  };
}

function mapSavedResult(savedResult) {
  const config = savedResult?.config || {};
  const stats = savedResult?.stats || {};

  const runwayCount = Array.isArray(config.runwaySettings)
    ? config.runwaySettings.length
    : 0;

  return {
    id: savedResult?.simulationName || "",
    name: savedResult?.simulationName || "Unnamed Simulation",
    timestamp: formatDate(savedResult?.dateExecuted),
    config: {
      "Number of Runways": runwayCount,
      "Inbound Rate": `${config.inboundRate ?? "--"} /hr`,
      "Outbound Rate": `${config.outboundRate ?? "--"} /hr`,
      "Simulation Duration": `${config.duration ?? "--"} mins`,
      "Max Wait Time": `${config.maxWaitTime ?? "--"} mins`,
      "Passenger Health Issue Multiplier": formatMultiplier(
        config.passengerHealthIssueMultiplier ??
          config.passengerHealthIssueRate,
      ),
      "Runway Inspection Multiplier": formatMultiplier(
        config.runwayInspectionMultiplier ?? config.runwayInspectionRate,
      ),
      "Snow Clearance Multiplier": formatMultiplier(
        config.snowClearanceMultiplier ?? config.snowClearanceRate,
      ),
      "Equipment Failure Multiplier": formatMultiplier(
        config.equipmentFailureMultiplier ?? config.equipmentFailureRate,
      ),
    },
    events: buildEventRows(config),
    eventLog: getEventLogLines(savedResult, config),
    statistics: {
      throughput: toNumber(stats.hourlyThroughput),
      depAvgWait: toNumber(stats.avgWaitTime),
      depMaxQueue: toNumber(stats.maxTakeOffQueueSize),
      depMaxDelay: toNumber(stats.maxTakeOffDelay),
      depAvgDelay: toNumber(stats.avgTakeOffDelay),
      depCancelled: toNumber(stats.cancellationCount),
      arrAvgHold: toNumber(stats.avgHoldingTime),
      arrMaxHolding: toNumber(stats.maxHoldingSize),
      arrMaxDelay: toNumber(stats.maxArrivalDelay),
      arrAvgDelay: toNumber(stats.avgArrivalDelay),
      arrDiverted: toNumber(stats.diversionCount),
    },
  };
}

async function fetchSummaries() {
  simulationSummaries = await fetchJson(API.summaries);
  return simulationSummaries;
}

async function fetchSimulationByName(name) {
  if (simulationDetailsCache.has(name)) {
    return simulationDetailsCache.get(name);
  }

  const raw = await fetchJson(API.viewOrCompare(name));
  const mapped = mapSavedResult(raw);

  if (!Array.isArray(mapped.eventLog) || mapped.eventLog.length === 0) {
    try {
      const lastResult = await fetchJson(API.lastResult);
      if (lastResult && lastResult.simulationName === name) {
        mapped.eventLog = getEventLogLines(
          lastResult,
          lastResult && lastResult.config,
        );
      }
    } catch (error) {
      console.warn(
        "Unable to fetch last simulation result for event log fallback:",
        error,
      );
    }
  }

  simulationDetailsCache.set(name, mapped);
  return mapped;
}

async function loadComparison(simAName, simBName) {
  const [simA, simB] = await Promise.all([
    fetchSimulationByName(simAName),
    fetchSimulationByName(simBName),
  ]);

  currentSimNames.A = simAName;
  currentSimNames.B = simBName;

  setText("simNameA", simA.name.toUpperCase());
  setText("simNameB", simB.name.toUpperCase());

  hideSelectModal();
  populateSimulationData(simA, "A");
  populateSimulationData(simB, "B");
  compareAndHighlight(simA, simB);
}

async function showViewModal(simName) {
  try {
    const sim = await fetchSimulationByName(simName);
    const stats = sim.statistics;

    setText("viewModalTitle", `Simulation Name: ${sim.name}`);
    populateConfigTable(sim, "viewConfigTable");
    populateRunwayEventsTable(sim.events?.runway, "viewRunwayEvents");
    populateAircraftEventsTable(sim.events?.aircraft, "viewAircraftEvents");
    setText("viewThroughput", formatNumber(stats.throughput));
    setText("viewDepAvgWait", formatNumber(stats.depAvgWait));
    setText("viewDepMaxQueue", formatInteger(stats.depMaxQueue));
    setText("viewDepMaxDelay", formatNumber(stats.depMaxDelay));
    setText("viewDepAvgDelay", formatNumber(stats.depAvgDelay));
    setText("viewDepCancelled", formatInteger(stats.depCancelled));
    setText("viewArrAvgHold", formatNumber(stats.arrAvgHold));
    setText("viewArrMaxHolding", formatInteger(stats.arrMaxHolding));
    setText("viewArrMaxDelay", formatNumber(stats.arrMaxDelay));
    setText("viewArrAvgDelay", formatNumber(stats.arrAvgDelay));
    setText("viewArrDiverted", formatInteger(stats.arrDiverted));

    document.getElementById("viewModal").classList.remove("hidden");
  } catch (error) {
    console.error("Error loading simulation details:", error);
    alert("Failed to load simulation details.");
  }
}

function hideViewModal() {
  document.getElementById("viewModal").classList.add("hidden");
}

function hideSelectModal() {
  document.getElementById("selectModal").classList.add("hidden");
}

function getSummaryByName(name) {
  return simulationSummaries.find((sim) => sim.simulationName === name);
}

async function deleteSimulation(simName) {
  await fetch(API.deleteResult(simName), { method: "DELETE" }).then(
    async (response) => {
      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Delete failed");
      }
    },
  );

  simulationDetailsCache.delete(simName);
  await fetchSummaries();

  if (currentSimNames.A === simName) currentSimNames.A = null;
  if (currentSimNames.B === simName) currentSimNames.B = null;

  await ensureCurrentSelectionAndLoad();
}

function renderSelectTableRows(summaries, tbody) {
  tbody.innerHTML = "";

  if (!summaries.length) {
    const tr = document.createElement("tr");
    tr.innerHTML =
      '<td colspan="4" style="text-align: center; color: #999;">No saved simulations found</td>';
    tbody.appendChild(tr);
    return;
  }

  summaries.forEach((sim) => {
    const tr = document.createElement("tr");
    const infoLines = [
      `Runways: ${sim.runwayCount ?? "--"}`,
      `Inbound Rate: ${sim.inboundRate ?? "--"} /hr`,
      `Outbound Rate: ${sim.outboundRate ?? "--"} /hr`,
      `Scheduled Events: ${sim.scheduledEventsCount ?? "--"}`,
      `Throughput: ${formatNumber(sim.throughput)} /hr`,
    ];

    tr.innerHTML = `
            <td class="sim-name">${sim.simulationName}</td>
            <td class="sim-date">${formatDate(sim.dateExecuted)}</td>
            <td class="sim-info">${infoLines.join("<br>")}</td>
            <td class="sim-options">
                <button class="btn-option btn-view" data-sim-name="${sim.simulationName}" title="View this simulation">View</button>
                <button class="btn-option btn-compare" data-sim-name="${sim.simulationName}" title="Compare with this simulation">Compare</button>
                <button class="btn-option btn-delete" data-sim-name="${sim.simulationName}" title="Delete this simulation">Delete</button>
            </td>
        `;
    tbody.appendChild(tr);
  });

  tbody.querySelectorAll("button.btn-view").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const simName = e.target.getAttribute("data-sim-name");
      showViewModal(simName);
    });
  });

  tbody.querySelectorAll("button.btn-compare").forEach((btn) => {
    btn.addEventListener("click", async (e) => {
      const simName = e.target.getAttribute("data-sim-name");
      try {
        if (selectingFor === "A") {
          await loadComparison(simName, currentSimNames.B || simName);
        } else if (selectingFor === "B") {
          await loadComparison(currentSimNames.A || simName, simName);
        } else {
          await loadComparison(simName, currentSimNames.B || simName);
        }
      } catch (error) {
        console.error("Error loading comparison:", error);
        alert("Failed to load comparison data.");
      }
    });
  });

  tbody.querySelectorAll("button.btn-delete").forEach((btn) => {
    btn.addEventListener("click", async (e) => {
      const simName = e.target.getAttribute("data-sim-name");
      if (!confirm(`Delete simulation ${simName}?`)) return;

      try {
        await deleteSimulation(simName);
        await showSelectModal(selectingFor);
      } catch (error) {
        console.error("Error deleting simulation:", error);
        alert("Failed to delete simulation.");
      }
    });
  });
}

async function showSelectModal(columnLetter = null) {
  selectingFor = columnLetter;
  const tbody = document.querySelector("#selectList tbody");

  if (selectingFor) {
    setText(
      "selectModalTitle",
      `Select simulation for SIMULATION ${selectingFor}`,
    );
  } else {
    setText("selectModalTitle", "Select simulations to compare");
  }

  let simsList = [...simulationSummaries];

  let sortState = { column: null, ascending: true };

  const renderAndBind = () => {
    renderSelectTableRows(simsList, tbody);
  };

  renderAndBind();

  const thead = document.querySelector("#selectList thead");
  const headers = thead.querySelectorAll("th.sortable");

  headers.forEach((header) => {
    header.onclick = () => {
      const columnIndex = Array.from(header.parentNode.children).indexOf(
        header,
      );
      const columnName = columnIndex === 0 ? "name" : "date";

      if (sortState.column === columnName) {
        sortState.ascending = !sortState.ascending;
      } else {
        sortState.column = columnName;
        sortState.ascending = true;
      }

      if (columnName === "name") {
        simsList.sort((a, b) => {
          const comparison = (a.simulationName || "").localeCompare(
            b.simulationName || "",
          );
          return sortState.ascending ? comparison : -comparison;
        });
      } else {
        simsList.sort((a, b) => {
          const timeA = new Date(a.dateExecuted).getTime();
          const timeB = new Date(b.dateExecuted).getTime();
          return sortState.ascending ? timeA - timeB : timeB - timeA;
        });
      }

      headers.forEach((h) => {
        const arrow = h.querySelector(".sort-arrow");
        if (arrow) arrow.textContent = "↑↓";
      });

      const activeArrow = header.querySelector(".sort-arrow");
      if (activeArrow)
        activeArrow.textContent = sortState.ascending ? "↑" : "↓";

      renderAndBind();
    };
  });

  document.getElementById("selectModal").classList.remove("hidden");
}

async function ensureCurrentSelectionAndLoad() {
  const names = simulationSummaries.map((s) => s.simulationName);

  if (!names.length) {
    throw new Error("No saved simulations available for comparison");
  }

  if (!currentSimNames.A || !names.includes(currentSimNames.A)) {
    currentSimNames.A = names[0];
  }

  if (!currentSimNames.B || !names.includes(currentSimNames.B)) {
    currentSimNames.B =
      names.find((name) => name !== currentSimNames.A) || currentSimNames.A;
  }

  await loadComparison(currentSimNames.A, currentSimNames.B);
}

function populateSimulationData(simData, suffix) {
  populateConfigTable(simData, `config${suffix}`);
  populateRunwayEventsTable(simData.events?.runway, `runwayEvents${suffix}`);
  populateAircraftEventsTable(
    simData.events?.aircraft,
    `aircraftEvents${suffix}`,
  );
  populateEventLog(simData.eventLog, `eventLog${suffix}`);

  const stats = simData.statistics;
  setText(`throughput${suffix}`, `${formatNumber(stats.throughput)} /hr`);
  setText(`depAvgWait${suffix}`, `${formatNumber(stats.depAvgWait)} mins`);
  setText(`depMaxQueue${suffix}`, formatInteger(stats.depMaxQueue));
  setText(`depMaxDelay${suffix}`, `${formatNumber(stats.depMaxDelay)} mins`);
  setText(`depAvgDelay${suffix}`, `${formatNumber(stats.depAvgDelay)} mins`);
  setText(`depCancelled${suffix}`, formatInteger(stats.depCancelled));
  setText(`arrAvgHold${suffix}`, `${formatNumber(stats.arrAvgHold)} mins`);
  setText(`arrMaxHolding${suffix}`, formatInteger(stats.arrMaxHolding));
  setText(`arrMaxDelay${suffix}`, `${formatNumber(stats.arrMaxDelay)} mins`);
  setText(`arrAvgDelay${suffix}`, `${formatNumber(stats.arrAvgDelay)} mins`);
  setText(`arrDiverted${suffix}`, formatInteger(stats.arrDiverted));
}

function populateEventLog(eventLog, targetId) {
  const list = document.getElementById(targetId);
  if (!list) return;

  list.innerHTML = "";

  if (!Array.isArray(eventLog) || eventLog.length === 0) {
    const emptyItem = document.createElement("li");
    emptyItem.className = "event-log-empty";
    emptyItem.textContent = "No events logged.";
    list.appendChild(emptyItem);
    return;
  }

  eventLog.forEach((line) => {
    const item = document.createElement("li");
    item.textContent = line;
    list.appendChild(item);
  });
}

function populateConfigTable(simData, targetId) {
  const table = document.getElementById(targetId);
  table.innerHTML = "";

  Object.entries(simData.config).forEach(([key, value]) => {
    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${key}</td>
            <td>${value}</td>
        `;
    table.appendChild(row);
  });
}

function populateRunwayEventsTable(events, targetId) {
  const tbody = document.querySelector(`#${targetId} tbody`);
  tbody.innerHTML = "";

  if (!Array.isArray(events) || events.length === 0) {
    const row = document.createElement("tr");
    row.innerHTML =
      '<td colspan="5" style="text-align: center; color: #999;">No scheduled runway events</td>';
    tbody.appendChild(row);
    return;
  }

  events.forEach((event) => {
    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${event.event}</td>
            <td>${event.time}</td>
            <td>${event.duration}</td>
            <td>${event.runwayMode}</td>
            <td>${event.status}</td>
        `;
    tbody.appendChild(row);
  });
}

function populateAircraftEventsTable(events, targetId) {
  const tbody = document.querySelector(`#${targetId} tbody`);
  tbody.innerHTML = "";

  if (!Array.isArray(events) || events.length === 0) {
    const row = document.createElement("tr");
    row.innerHTML =
      '<td colspan="3" style="text-align: center; color: #999;">No scheduled aircraft events</td>';
    tbody.appendChild(row);
    return;
  }

  events.forEach((event) => {
    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${event.event}</td>
            <td>${event.time}</td>
            <td>${event.emergencyType}</td>
        `;
    tbody.appendChild(row);
  });
}

function compareAndHighlight(simA, simB) {
  const metricConfig = {
    throughput: { higherBetter: true },
    depAvgWait: { higherBetter: false },
    depMaxQueue: { higherBetter: false },
    depMaxDelay: { higherBetter: false },
    depAvgDelay: { higherBetter: false },
    depCancelled: { higherBetter: false },
    arrAvgHold: { higherBetter: false },
    arrMaxHolding: { higherBetter: false },
    arrMaxDelay: { higherBetter: false },
    arrAvgDelay: { higherBetter: false },
    arrDiverted: { higherBetter: false },
  };

  Object.keys(metricConfig).forEach((metric) => {
    const valA = simA.statistics[metric];
    const valB = simB.statistics[metric];

    const elementA = document.getElementById(metric + "A");
    const elementB = document.getElementById(metric + "B");

    if (!elementA || !elementB) return;

    elementA.classList.remove("better", "worse");
    elementB.classList.remove("better", "worse");

    if (valA == null || valB == null || valA === valB) return;

    const { higherBetter } = metricConfig[metric];
    const aIsBetter = higherBetter ? valA > valB : valA < valB;
    const bIsBetter = higherBetter ? valB > valA : valB < valA;

    if (aIsBetter) {
      elementA.classList.add("better");
      elementB.classList.add("worse");
    } else if (bIsBetter) {
      elementB.classList.add("better");
      elementA.classList.add("worse");
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await fetchSummaries();
    await ensureCurrentSelectionAndLoad();
  } catch (error) {
    console.error("Error initialising comparison page:", error);
    alert(
      "No saved simulation results found. Save at least one result from the results page first.",
    );
    window.location.href = "/";
  }

  document.getElementById("switchBtnA").addEventListener("click", () => {
    showSelectModal("A");
  });

  document.getElementById("switchBtnB").addEventListener("click", () => {
    showSelectModal("B");
  });

  document
    .getElementById("closeSelectModal")
    .addEventListener("click", hideSelectModal);
  document.getElementById("selectModal").addEventListener("click", (e) => {
    if (e.target.id === "selectModal") hideSelectModal();
  });

  document
    .getElementById("closeViewModal")
    .addEventListener("click", hideViewModal);
  document.getElementById("viewModal").addEventListener("click", (e) => {
    if (e.target.id === "viewModal") hideViewModal();
  });
});
