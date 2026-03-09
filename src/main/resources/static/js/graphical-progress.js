let stompClient = null;
let startTime;
let wsReady = false;
let isPaused = false;
let isStopping = false;
let isFastForwarding = false;
let planeInfoCard = null;
let planeInfoTitle = null;
let planeInfoBody = null;
const speedLevels = [1, 5, 20];
let speedIndex = 0;
const ALTITUDE_COLORS = [
    '#f97316',
    '#f59e0b',
    '#facc15',
    '#84cc16',
    '#22c55e',
    '#2dd4bf',
    '#38bdf8',
    '#3b82f6',
    '#6366f1',
    '#8b5cf6'
];
const RUNWAY_SLOT_X = 600;
const RUNWAY_SLOT_WIDTH = 300;
const RUNWAY_SLOT_MIN_Y = 100;
const RUNWAY_SLOT_MAX_Y = 516;
const RUNWAY_SLOT_GAP = 4;
const RUNWAY_LABEL_X = RUNWAY_SLOT_X + 20;
const RUNWAY_AIRCRAFT_X = RUNWAY_SLOT_X + RUNWAY_SLOT_WIDTH - 42;
const TAKEOFF_QUEUE_START_X = 610;
const TAKEOFF_QUEUE_Y = 585;
const TAKEOFF_QUEUE_STEP = 24;

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}

function updateControlUi() {
    const pauseButton = document.getElementById('ctrlPause');
    if (pauseButton) {
        pauseButton.textContent = isPaused ? '▶ Play' : '⏸ Pause';
        pauseButton.title = isPaused ? 'Resume' : 'Pause';
        pauseButton.disabled = isStopping || isFastForwarding;
    }

    const speedButtonMap = [
        { id: 'ctrlSpeed1', value: 1 },
        { id: 'ctrlSpeed5', value: 5 },
        { id: 'ctrlSpeed20', value: 20 }
    ];

    speedButtonMap.forEach(({ id, value }) => {
        const button = document.getElementById(id);
        if (!button) return;

        const isActive = speedLevels[speedIndex] === value;
        button.disabled = isStopping || isFastForwarding;
        button.classList.toggle('secondary', !isActive);
        button.classList.toggle('speed-active', isActive);
        button.setAttribute('aria-pressed', isActive ? 'true' : 'false');
    });

    const fastForwardButton = document.getElementById('ctrlFastForward');
    if (fastForwardButton) {
        fastForwardButton.disabled = isStopping || isFastForwarding;
        fastForwardButton.textContent = isFastForwarding ? '⏳ Processing...' : '⏭ Fast Forward';
    }

    const stopButton = document.getElementById('ctrlStop');
    if (stopButton) {
        stopButton.disabled = isStopping;
    }
}

async function postControl(path) {
    const response = await fetch(path, {
        method: 'POST'
    });

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }
}

function sendSimulationCommand(path) {
    if (!stompClient || !wsReady) {
        throw new Error('WebSocket not connected. Please start from config page and use Table View or Graphical View.');
    }

    stompClient.send(path, {}, '');
}

function notifyControlError(action, error) {
    console.error(`${action} failed:`, error);
    alert(`${action} failed: ${error.message}`);
}

async function applySpeedIndex(nextIndex) {
    const safeIndex = Math.max(0, Math.min(speedLevels.length - 1, nextIndex));
    if (safeIndex === speedIndex) return;

    const multiplier = speedLevels[safeIndex];
    await postControl(`/api/simulation/speed/${multiplier}`);
    speedIndex = safeIndex;
    updateControlUi();
}

async function togglePauseResume() {
    if (isStopping) return;

    if (isPaused) {
        await postControl('/api/simulation/resume');
        isPaused = false;
    } else {
        await postControl('/api/simulation/pause');
        isPaused = true;
    }

    updateControlUi();
}

async function stopSimulation() {
    if (isStopping) return;
    isStopping = true;
    updateControlUi();

    try {
        await postControl('/api/simulation/stop');
    } catch (error) {
        console.error('Stop failed:', error);
    }

    if (stompClient !== null) {
        stompClient.disconnect();
    }

    window.location.href = '/index.html';
}

async function triggerFastForward() {
    if (isStopping || isFastForwarding) return;

    isFastForwarding = true;
    updateControlUi();

    try {
        await postControl('/api/simulation/fastforward');
    } catch (error) {
        isFastForwarding = false;
        updateControlUi();
        notifyControlError('Fast Forward', error);
    }
}

function bindControls() {
    const stopButton = document.getElementById('ctrlStop');
    if (stopButton) {
        stopButton.addEventListener('click', function () {
            stopSimulation().catch(error => {
                console.error('Stop error:', error);
            });
        });
    }

    const pauseButton = document.getElementById('ctrlPause');
    if (pauseButton) {
        pauseButton.addEventListener('click', function () {
            togglePauseResume().catch(error => {
                notifyControlError('Pause/Resume', error);
            });
        });
    }

    const speedButtonConfig = [
        { id: 'ctrlSpeed1', targetValue: 1 },
        { id: 'ctrlSpeed5', targetValue: 5 },
        { id: 'ctrlSpeed20', targetValue: 20 }
    ];

    speedButtonConfig.forEach(({ id, targetValue }) => {
        const speedButton = document.getElementById(id);
        if (!speedButton) return;

        speedButton.addEventListener('click', function () {
            const targetIndex = speedLevels.indexOf(targetValue);
            if (targetIndex < 0) return;

            applySpeedIndex(targetIndex).catch(error => {
                notifyControlError('Change speed', error);
            });
        });
    });

    const fastForwardButton = document.getElementById('ctrlFastForward');
    if (fastForwardButton) {
        fastForwardButton.addEventListener('click', function () {
            triggerFastForward().catch(error => {
                notifyControlError('Fast Forward', error);
            });
        });
    }

    updateControlUi();
}

function toLabel(rawValue) {
    if (rawValue === null || rawValue === undefined || rawValue === '') return 'Unknown';

    return String(rawValue)
        .toLowerCase()
        .split('_')
        .map(part => part.charAt(0).toUpperCase() + part.slice(1))
        .join(' ');
}

function updateProgress(progressDecimal) {
    const safeProgress = Number.isFinite(progressDecimal) ? progressDecimal : 0;
    const progressPercentage = Math.max(0, Math.min(100, safeProgress * 100));

    const progressBar = document.getElementById('progressBar');
    if (progressBar) progressBar.style.width = progressPercentage + '%';

    const percentageText = document.getElementById('progressPercentage');
    if (percentageText) percentageText.textContent = Math.round(progressPercentage).toString();
}

function updateElapsedTime() {
    if (!startTime) return;
    const elapsed = Math.floor((Date.now() - startTime) / 1000);
    setText('elapsedTime', elapsed + 's');
}

function clearLayer(layerId) {
    const layer = document.getElementById(layerId);
    if (!layer) return;

    while (layer.firstChild) {
        layer.removeChild(layer.firstChild);
    }
}

function createSvgElement(tagName) {
    const namespace = 'http://www.w3.org/2000/svg';
    return document.createElementNS(namespace, tagName);
}

function createPlaneText(x, y, className) {
    const text = createSvgElement('text');
    text.setAttribute('x', String(x));
    text.setAttribute('y', String(y));
    text.setAttribute('class', `plane-chip ${className}`);
    text.textContent = '✈';
    return text;
}

function closePlaneInfoCard() {
    if (!planeInfoCard) return;
    planeInfoCard.classList.add('is-hidden');
}

function openPlaneInfoCard(title, lines) {
    if (!planeInfoCard || !planeInfoTitle || !planeInfoBody) return;

    planeInfoTitle.textContent = title || 'Aircraft';
    planeInfoBody.innerHTML = '';

    lines.filter(Boolean).forEach(line => {
        const item = document.createElement('li');
        item.textContent = line;
        planeInfoBody.appendChild(item);
    });

    planeInfoCard.classList.remove('is-hidden');
}

function attachPlaneInfoClick(planeElement, title, lines) {
    if (!planeElement) return;

    planeElement.setAttribute('tabindex', '0');
    planeElement.setAttribute('role', 'button');
    planeElement.setAttribute('aria-label', title || 'Aircraft details');

    planeElement.addEventListener('click', event => {
        event.stopPropagation();
        openPlaneInfoCard(title, lines);
    });

    planeElement.addEventListener('keydown', event => {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            openPlaneInfoCard(title, lines);
        }
    });
}

function initPlaneInfoCard() {
    planeInfoCard = document.getElementById('planeInfoCard');
    planeInfoTitle = document.getElementById('planeInfoTitle');
    planeInfoBody = document.getElementById('planeInfoBody');
    const closeButton = document.getElementById('planeInfoClose');
    const scene = document.getElementById('simScene');

    if (closeButton) {
        closeButton.addEventListener('click', event => {
            event.stopPropagation();
            closePlaneInfoCard();
        });
    }

    if (planeInfoCard) {
        planeInfoCard.addEventListener('click', event => {
            event.stopPropagation();
        });
    }

    if (scene) {
        scene.addEventListener('click', () => {
            closePlaneInfoCard();
        });
    }
}

function getAltitudeFromAircraft(aircraft, index) {
    if (aircraft && Number.isFinite(aircraft.altitude)) {
        return Math.max(0, aircraft.altitude);
    }

    return (index + 1) * 1000;
}

function getAltitudeColor(altitude) {
    const clamped = Math.max(0, Math.min(10000, altitude));
    const bandIndex = Math.floor(clamped / 1000);
    return ALTITUDE_COLORS[Math.min(bandIndex, ALTITUDE_COLORS.length - 1)];
}

function renderAltitudeLegend() {
    clearLayer('altitudeLegendLayer');

    const layer = document.getElementById('altitudeLegendLayer');
    if (!layer) return;

    const baseX = 96;
    const baseY = 30;
    const barWidth = 38;
    const barHeight = 24;
    const totalWidth = ALTITUDE_COLORS.length * barWidth;

    const title = createSvgElement('text');
    title.setAttribute('x', String(baseX - 18));
    title.setAttribute('y', String(baseY + 12));
    title.setAttribute('class', 'holding-legend-title');
    title.setAttribute('text-anchor', 'end');
    title.textContent = 'Altitude';
    layer.appendChild(title);

    const unit = createSvgElement('text');
    unit.setAttribute('x', String(baseX - 18));
    unit.setAttribute('y', String(baseY + 24));
    unit.setAttribute('class', 'holding-legend-tick');
    unit.setAttribute('text-anchor', 'end');
    unit.textContent = '(ft)';
    layer.appendChild(unit);

    const border = createSvgElement('rect');
    border.setAttribute('x', String(baseX));
    border.setAttribute('y', String(baseY));
    border.setAttribute('width', String(totalWidth));
    border.setAttribute('height', String(barHeight));
    border.setAttribute('fill', 'none');
    border.setAttribute('stroke', '#111827');
    border.setAttribute('stroke-width', '0.8');
    layer.appendChild(border);

    for (let i = 0; i < ALTITUDE_COLORS.length; i++) {
        const rect = createSvgElement('rect');
        rect.setAttribute('x', String(baseX + (i * barWidth)));
        rect.setAttribute('y', String(baseY));
        rect.setAttribute('width', String(barWidth));
        rect.setAttribute('height', String(barHeight));
        rect.setAttribute('fill', ALTITUDE_COLORS[i]);
        layer.appendChild(rect);
    }

    for (let i = 0; i <= ALTITUDE_COLORS.length; i++) {
        const separator = createSvgElement('line');
        const separatorX = baseX + (i * barWidth);
        separator.setAttribute('x1', String(separatorX));
        separator.setAttribute('y1', String(baseY));
        separator.setAttribute('x2', String(separatorX));
        separator.setAttribute('y2', String(baseY + barHeight));
        separator.setAttribute('stroke', '#1f2937');
        separator.setAttribute('stroke-width', i === 0 || i === ALTITUDE_COLORS.length ? '0.9' : '0.7');
        layer.appendChild(separator);

        const tick = createSvgElement('text');
        const isLastLabel = i === ALTITUDE_COLORS.length;
        const isFirstLabel = i === 0;

        tick.setAttribute('x', String(separatorX));
        tick.setAttribute('y', String(baseY + barHeight + 14));
        tick.setAttribute('class', 'holding-legend-tick');
        tick.setAttribute('dominant-baseline', 'middle');

        if (isFirstLabel) {
            tick.setAttribute('text-anchor', 'start');
            tick.setAttribute('x', String(separatorX + 1));
        } else if (isLastLabel) {
            tick.setAttribute('text-anchor', 'end');
            tick.setAttribute('x', String(separatorX - 1));
        } else {
            tick.setAttribute('text-anchor', 'middle');
        }

        tick.textContent = isLastLabel ? '10000+' : String(i * 1000);
        layer.appendChild(tick);
    }
}

function getRunwayLayout(runwayCount) {
    const safeCount = Math.max(0, runwayCount);
    if (safeCount === 0) {
        return {
            slotCenters: [],
            slotHeight: 0
        };
    }

    const minY = RUNWAY_SLOT_MIN_Y;
    const maxY = RUNWAY_SLOT_MAX_Y;
    const availableHeight = maxY - minY;
    const slotGap = RUNWAY_SLOT_GAP;

    const rawSlotHeight = (availableHeight - (slotGap * (safeCount - 1))) / safeCount;
    const slotHeight = Math.max(24, Math.min(38, rawSlotHeight));
    const usedHeight = (slotHeight * safeCount) + (slotGap * (safeCount - 1));
    const firstTop = minY + ((availableHeight - usedHeight) / 2);

    const slotCenters = [];
    for (let i = 0; i < safeCount; i++) {
        const top = firstTop + (i * (slotHeight + slotGap));
        slotCenters.push(top + (slotHeight / 2));
    }

    return {
        slotCenters,
        slotHeight
    };
}

function getRunwaySlotStyle(runway) {
    const statusKey = String(runway?.status || '').toUpperCase();
    const hasAircraft = Boolean(runway?.aircraftCallsign);

    let fill = '#e9eef5';
    let stroke = 'rgba(148, 163, 184, 0.55)';

    if (statusKey.includes('SNOW')) {
        fill = '#ecfeff';
        stroke = '#67e8f9';
    } else if (statusKey.includes('INSPECTION')) {
        fill = '#fffbeb';
        stroke = '#fcd34d';
    } else if (statusKey.includes('FAILURE')) {
        fill = '#fef2f2';
        stroke = '#fca5a5';
    } else if (statusKey.includes('AVAILABLE')) {
        fill = '#eff6ff';
        stroke = '#bfdbfe';
    }

    if (hasAircraft) {
        fill = '#ffffff';
        stroke = '#93c5fd';
    }

    return { fill, stroke };
}

function renderRunwaySlots(runways) {
    clearLayer('runwaySlotsLayer');

    const slotLayer = document.getElementById('runwaySlotsLayer');
    if (!slotLayer) return;

    const sortedRunways = Array.isArray(runways)
        ? runways.slice().sort((a, b) => (a.runwayID ?? 0) - (b.runwayID ?? 0))
        : [];

    const layout = getRunwayLayout(sortedRunways.length);

    for (let i = 0; i < sortedRunways.length; i++) {
        const centerY = layout.slotCenters[i];
        const topY = centerY - (layout.slotHeight / 2);
        const runway = sortedRunways[i] || {};
        const slotStyle = getRunwaySlotStyle(runway);

        const slotRect = createSvgElement('rect');
        slotRect.setAttribute('x', String(RUNWAY_SLOT_X));
        slotRect.setAttribute('y', String(topY));
        slotRect.setAttribute('width', String(RUNWAY_SLOT_WIDTH));
        slotRect.setAttribute('height', String(layout.slotHeight));
        slotRect.setAttribute('class', 'runway-slot');
        slotRect.setAttribute('fill', slotStyle.fill);
        slotRect.setAttribute('stroke', slotStyle.stroke);
        slotLayer.appendChild(slotRect);

        const slotLabel = createSvgElement('text');
        slotLabel.setAttribute('x', String(RUNWAY_LABEL_X));
        slotLabel.setAttribute('y', String(centerY + 5));
        slotLabel.setAttribute('text-anchor', 'start');
        slotLabel.setAttribute('class', 'runway-slot-label');
        slotLabel.textContent = String((runway.runwayID ?? i) + 1);
        slotLayer.appendChild(slotLabel);
    }
}

function renderHoldingAircraft(holdingAircraft) {
    clearLayer('holdingAircraftLayer');

    const layer = document.getElementById('holdingAircraftLayer');
    if (!layer || !Array.isArray(holdingAircraft) || holdingAircraft.length === 0) return;

    const cx = 250;
    const cy = 330;
    const radius = 210;

    for (let i = 0; i < holdingAircraft.length; i++) {
        const aircraft = holdingAircraft[i] || {};
        const angle = (Math.PI * 2 * i) / holdingAircraft.length;
        const x = cx + Math.cos(angle) * radius;
        const y = cy + Math.sin(angle) * radius;
        const altitude = getAltitudeFromAircraft(aircraft, i);
        const altitudeColor = getAltitudeColor(altitude);

        const plane = createPlaneText(x, y, 'holding-plane-altitude');
        plane.setAttribute('fill', altitudeColor);
        plane.setAttribute('transform', `rotate(${(angle * 180) / Math.PI + 90} ${x} ${y})`);
        const fuelText = Number.isFinite(aircraft.fuelLevel) ? aircraft.fuelLevel.toFixed(1) : '--';
        attachPlaneInfoClick(plane, 'Holding Aircraft', [
            `Callsign: ${aircraft.callsign || 'N/A'}`,
            `Emergency: ${toLabel(aircraft.emergencyStatus)}`,
            `Fuel: ${fuelText}`,
            `Altitude: ${Math.round(altitude)} ft`
        ]);
        layer.appendChild(plane);
    }
}

function renderRunwayAircraft(runways) {
    clearLayer('runwayAircraftLayer');

    const layer = document.getElementById('runwayAircraftLayer');
    if (!layer || !Array.isArray(runways)) return;

    const sortedRunways = runways.slice().sort((a, b) => (a.runwayID ?? 0) - (b.runwayID ?? 0));
    const layout = getRunwayLayout(sortedRunways.length);

    for (let i = 0; i < sortedRunways.length; i++) {
        const runway = sortedRunways[i];
        if (!runway || !runway.aircraftCallsign) continue;

        const y = layout.slotCenters[i] ?? 113;
        const plane = createPlaneText(RUNWAY_AIRCRAFT_X, y, 'runway-plane');
        attachPlaneInfoClick(plane, 'Runway Aircraft', [
            `Callsign: ${runway.aircraftCallsign || 'N/A'}`,
            `Runway: ${String((runway.runwayID ?? i) + 1)}`,
            `Status: ${toLabel(runway.status)}`,
            `Mode: ${toLabel(runway.mode)}`
        ]);
        layer.appendChild(plane);
    }
}

function renderTakeoffAircraft(takeoffAircraft, currentTick = 0) {
    clearLayer('takeoffAircraftLayer');

    const layer = document.getElementById('takeoffAircraftLayer');
    if (!layer || !Array.isArray(takeoffAircraft) || takeoffAircraft.length === 0) return;

    const visibleCount = Math.min(takeoffAircraft.length, 10);
    for (let i = 0; i < visibleCount; i++) {
        const aircraft = takeoffAircraft[i] || {};
        const x = TAKEOFF_QUEUE_START_X + (i * TAKEOFF_QUEUE_STEP);
        const y = TAKEOFF_QUEUE_Y;
        const plane = createPlaneText(x, y, 'takeoff-plane');
        plane.setAttribute('transform', `rotate(-90 ${x} ${y})`);
        const timeInQueue = Number.isFinite(aircraft.entryTick) ? Math.max(0, currentTick - aircraft.entryTick) : 0;
        attachPlaneInfoClick(plane, 'Take-off Queue Aircraft', [
            `Callsign: ${aircraft.callsign || 'N/A'}`,
            `Status: ${toLabel(aircraft.status)}`,
            `Time in Queue: ${timeInQueue} mins`
        ]);
        layer.appendChild(plane);
    }
}

function renderTableRows(tableId, rows, rowMapper) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const tbody = table.querySelector('tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!Array.isArray(rows) || rows.length === 0) {
        const tr = document.createElement('tr');
        tr.innerHTML = '<td class="table-empty" colspan="4">No data</td>';
        tbody.appendChild(tr);
        return;
    }

    rows.forEach(row => {
        const values = rowMapper(row);
        const tr = document.createElement('tr');
        values.forEach(value => {
            const td = document.createElement('td');
            td.textContent = value;
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });
}

function renderSnapshot(snapshotData) {
    const currentTick = Number.isFinite(snapshotData.currentTick) ? snapshotData.currentTick : 0;
    const holdingCount = Number.isFinite(snapshotData.holdingPatternSize) ? snapshotData.holdingPatternSize : 0;
    const takeoffCount = Number.isFinite(snapshotData.takeoffQueueSize) ? snapshotData.takeoffQueueSize : 0;
    const landedCount = Number.isFinite(snapshotData.totalLanded) ? snapshotData.totalLanded : 0;
    const departedCount = Number.isFinite(snapshotData.totalDeparted) ? snapshotData.totalDeparted : 0;
    const diversionCount = Number.isFinite(snapshotData.diversionCount) ? snapshotData.diversionCount : 0;
    const cancellationCount = Number.isFinite(snapshotData.cancellationCount) ? snapshotData.cancellationCount : 0;

    updateProgress(snapshotData.progressPercent);
    updateElapsedTime();

    setText('tickCount', String(currentTick));
    setText('holdingCount', String(holdingCount));
    setText('takeoffCount', String(takeoffCount));
    setText('holdingCountCenter', String(holdingCount));
    setText('takeoffCountCenter', String(takeoffCount));
    setText('landedCount', String(landedCount));
    setText('departedCount', String(departedCount));
    setText('diversionCount', String(diversionCount));
    setText('cancellationCount', String(cancellationCount));

    const throughput = currentTick > 0 ? (((landedCount + departedCount) * 60) / currentTick) : 0;
    setText('throughputLive', throughput.toFixed(1) + ' /hr');

    const holdingAircraft = Array.isArray(snapshotData.holdingAircraft) ? snapshotData.holdingAircraft : [];
    const runwayData = Array.isArray(snapshotData.runways) ? snapshotData.runways : [];
    const takeoffAircraft = Array.isArray(snapshotData.takeoffAircraft) ? snapshotData.takeoffAircraft : [];

    renderHoldingAircraft(holdingAircraft);
    renderRunwaySlots(runwayData);
    renderRunwayAircraft(runwayData);
    renderTakeoffAircraft(takeoffAircraft, currentTick);

    renderTableRows(
        'runwayTable',
        runwayData.slice().sort((a, b) => (a.runwayID ?? 0) - (b.runwayID ?? 0)),
        runway => [
            String((runway.runwayID ?? 0) + 1),
            toLabel(runway.status),
            toLabel(runway.mode),
            runway.aircraftCallsign || '--'
        ]
    );

    renderTableRows('takeoffTable', takeoffAircraft, aircraft => {
        const timeInQueue = Number.isFinite(aircraft.entryTick) ? Math.max(0, currentTick - aircraft.entryTick) : 0;
        return [
            aircraft.callsign || 'N/A',
            toLabel(aircraft.status),
            `${timeInQueue} mins`
        ];
    });

    renderTableRows('holdingTable', holdingAircraft, aircraft => {
        const fuel = Number.isFinite(aircraft.fuelLevel) ? aircraft.fuelLevel.toFixed(1) : '--';
        return [
            aircraft.callsign || 'N/A',
            fuel,
            toLabel(aircraft.emergencyStatus)
        ];
    });
}

function connectWebSocket() {
    startTime = Date.now();
    window.setInterval(updateElapsedTime, 1000);
    wsReady = false;
    updateControlUi();

    const socket = new SockJS('/simulation-websocket');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket: ' + frame);
        wsReady = true;
        updateControlUi();

        stompClient.subscribe('/simulation/snapshot', function (message) {
            const snapshotData = JSON.parse(message.body);
            renderSnapshot(snapshotData);
        });

        stompClient.subscribe('/simulation/complete', function () {
            if (isStopping) return;
            console.log('Simulation complete. Redirecting to results...');

            if (stompClient !== null) {
                wsReady = false;
                stompClient.disconnect();
                updateControlUi();
            }

            setTimeout(() => {
                window.location.href = '/results.html';
            }, 2000);
        });

        stompClient.send('/app/simulation/ready', {}, '');
    }, function (error) {
        wsReady = false;
        updateControlUi();
        console.error('WebSocket Error: ', error);
        alert('Simulation connection failed. Please start again from config page.');
    });
}

document.addEventListener('DOMContentLoaded', function () {
    bindControls();
    initPlaneInfoCard();
    renderAltitudeLegend();
    connectWebSocket();
});

window.addEventListener('beforeunload', function () {
    if (stompClient !== null) {
        wsReady = false;
        stompClient.disconnect();
    }
});