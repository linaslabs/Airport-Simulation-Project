// ===== COMPARE ALL SIMULATIONS SCRIPT =====

const API = {
    summaries: '/api/results/summaries',
    viewOrCompare: (name) => `/api/results/vieworcompare/${encodeURIComponent(name)}`,
    deleteResult: (name) => `/api/results/delete/${encodeURIComponent(name)}`
};

/**
 * @typedef {Object} SortState
 * @property {?string} column Current sorted column.
 * @property {?('asc'|'desc')} direction Current sort direction.
 */

let allSimulations = [];

/** @type {SortState} */
let currentSort = {
    column: null,
    direction: null // 'asc' or 'desc'
};

/**
 * Initialise the compare-all page when the DOM is ready.
 */
document.addEventListener('DOMContentLoaded', async () => {
    await loadAllSimulations();
    setupSortingListeners();
    updateSortIndicators();
});

/**
 * Load all saved simulations and decide whether to show the table or the empty state.
 *
 * @returns {Promise<void>}
 */
async function loadAllSimulations() {
    const loadingMessage = document.getElementById('loadingMessage');
    const noResults = document.getElementById('noResults');
    const tableContainer = document.querySelector('.table-container');

    try {
        loadingMessage.style.display = 'block';
        tableContainer.style.display = 'none';
        noResults.classList.add('hidden');

        const summaries = await fetchJson(API.summaries);
        allSimulations = await fetchSimulationDetailsFromSummaries(summaries);

        loadingMessage.style.display = 'none';

        if (!allSimulations || allSimulations.length === 0) {
            noResults.classList.remove('hidden');
            tableContainer.style.display = 'none';
        } else {
            tableContainer.style.display = 'block';
            renderTable();
        }

    } catch (error) {
        console.error('Error loading simulations:', error);
        loadingMessage.textContent = 'Error loading simulation results. Please try again.';
        loadingMessage.style.color = '#dc2626';
    }
}

/**
 * Fetch JSON from the backend and throw a readable error for failed requests.
 *
 * @param {string} url Endpoint URL.
 * @param {RequestInit} [options={}] Fetch options.
 * @returns {Promise<Object>} Parsed JSON payload.
 */
async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Request failed (${response.status})`);
    }
    return response.json();
}

/**
 * Expand summary rows into full simulation details so every sortable metric is available.
 * If one detail request fails, keep a minimal fallback object so the table can still load.
 *
 * @param {Array<Object>} summaries Simulation summaries from the backend.
 * @returns {Promise<Array<Object>>} Full simulation results that can be shown in the table.
 */
async function fetchSimulationDetailsFromSummaries(summaries) {
    if (!Array.isArray(summaries) || summaries.length === 0) {
        return [];
    }

    const detailPromises = summaries.map(async (summary) => {
        const name = summary?.simulationName;
        if (!name) return null;

        try {
            return await fetchJson(API.viewOrCompare(name));
        } catch (error) {
            console.warn(`Failed to load detail for simulation ${name}:`, error);
            // Keep a lightweight fallback row instead of dropping the simulation completely.
            return {
                simulationName: name,
                dateExecuted: summary?.dateExecuted || null,
                stats: {
                    hourlyThroughput: summary?.throughput ?? null
                }
            };
        }
    });

    const resolved = await Promise.all(detailPromises);
    return resolved.filter(sim => sim && sim.simulationName);
}

/**
 * Render the comparison table, optionally using the current sort selection.
 *
 * @param {?string} [sortColumn=null] Column key to sort by.
 * @param {?('asc'|'desc')} [sortDirection=null] Sort direction.
 * @returns {void}
 */
function renderTable(sortColumn = null, sortDirection = null) {
    const tbody = document.getElementById('tableBody');

    if (!allSimulations || allSimulations.length === 0) {
        tbody.innerHTML = '<tr class="no-data"><td colspan="13">No simulation results available.</td></tr>';
        return;
    }

    // Work on a copy so sorting the table does not accidentally reorder the source list.
    let displayData = [...allSimulations];

    if (sortColumn && sortDirection) {
        displayData = sortData(displayData, sortColumn, sortDirection);
    }

    // Build the rows as HTML because this table is fully regenerated after each sort or delete.
    tbody.innerHTML = displayData.map(sim => {
        const stats = sim.stats || {};
        // Some fallback rows only have throughput on the summary object, not in stats.
        const throughput = stats.hourlyThroughput ?? sim.throughput ?? null;

        return `
            <tr>
                <td>${escapeHtml(sim.simulationName || 'Unnamed')}</td>
                <td class="numeric" data-value="${throughput || 0}">${formatNumber(throughput, 2)}</td>
                <td class="numeric" data-value="${stats.avgWaitTime || 0}">${formatNumber(stats.avgWaitTime, 2)}</td>
                <td class="numeric" data-value="${stats.maxTakeOffQueueSize || 0}">${formatInteger(stats.maxTakeOffQueueSize)}</td>
                <td class="numeric" data-value="${stats.maxTakeOffDelay || 0}">${formatNumber(stats.maxTakeOffDelay, 2)}</td>
                <td class="numeric" data-value="${stats.avgTakeOffDelay || 0}">${formatNumber(stats.avgTakeOffDelay, 2)}</td>
                <td class="numeric" data-value="${stats.cancellationCount || 0}">${formatInteger(stats.cancellationCount)}</td>
                <td class="numeric" data-value="${stats.avgHoldingTime || 0}">${formatNumber(stats.avgHoldingTime, 2)}</td>
                <td class="numeric" data-value="${stats.maxHoldingSize || 0}">${formatInteger(stats.maxHoldingSize)}</td>
                <td class="numeric" data-value="${stats.maxArrivalDelay || 0}">${formatNumber(stats.maxArrivalDelay, 2)}</td>
                <td class="numeric" data-value="${stats.avgArrivalDelay || 0}">${formatNumber(stats.avgArrivalDelay, 2)}</td>
                <td class="numeric" data-value="${stats.diversionCount || 0}">${formatInteger(stats.diversionCount)}</td>
                <td>
                    <button class="btn-delete" data-sim-name="${escapeHtml(sim.simulationName || '')}" title="Delete this simulation">Delete</button>
                </td>
            </tr>
        `;
    }).join('');

    bindDeleteButtons();

    // Best-value highlighting is currently optional, so leave the hook here.
    // highlightBestValues();
}

/**
 * Attach delete handlers after the table rows have been rendered.
 *
 * @returns {void}
 */
function bindDeleteButtons() {
    const buttons = document.querySelectorAll('.btn-delete');

    buttons.forEach(button => {
        button.addEventListener('click', async () => {
            const simName = button.getAttribute('data-sim-name');
            if (!simName) return;

            if (!confirm(`Delete simulation ${simName}?`)) {
                return;
            }

            // Lock the button immediately so the same delete request cannot be sent twice.
            button.disabled = true;
            button.textContent = 'Deleting...';

            try {
                await deleteSimulation(simName);
            } catch (error) {
                console.error('Error deleting simulation:', error);
                alert('Failed to delete simulation.');
            }
        });
    });
}

/**
 * Delete one saved simulation and refresh the table state.
 *
 * @param {string} simName Simulation name to delete.
 * @returns {Promise<void>}
 */
async function deleteSimulation(simName) {
    const response = await fetch(API.deleteResult(simName), { method: 'DELETE' });
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Delete failed');
    }

    allSimulations = allSimulations.filter(sim => sim?.simulationName !== simName);

    if (allSimulations.length === 0) {
        document.querySelector('.table-container').style.display = 'none';
        document.getElementById('noResults').classList.remove('hidden');
        return;
    }

    renderTable(currentSort.column, currentSort.direction);
}

/**
 * Sort simulation data by a requested column.
 *
 * @param {Array<Object>} data Simulation list to sort.
 * @param {string} column Column key from the table header.
 * @param {'asc'|'desc'} direction Sort direction.
 * @returns {Array<Object>} Sorted array.
 */
function sortData(data, column, direction) {
    const columnMap = {
        'name': 'simulationName',
        'throughput': 'stats.hourlyThroughput',
        'avgWaitTime': 'stats.avgWaitTime',
        'maxTakeOffQueue': 'stats.maxTakeOffQueueSize',
        'maxTakeOffDelay': 'stats.maxTakeOffDelay',
        'avgTakeOffDelay': 'stats.avgTakeOffDelay',
        'cancellationCount': 'stats.cancellationCount',
        'avgHoldingTime': 'stats.avgHoldingTime',
        'maxHoldingSize': 'stats.maxHoldingSize',
        'maxArrivalDelay': 'stats.maxArrivalDelay',
        'avgArrivalDelay': 'stats.avgArrivalDelay',
        'diversionCount': 'stats.diversionCount'
    };

    const path = columnMap[column];
    if (!path) return data;

    return data.sort((a, b) => {
        let aVal = getNestedValue(a, path);
        let bVal = getNestedValue(b, path);

        // Give missing values a predictable fallback so sorting stays stable.
        if (aVal === null || aVal === undefined) aVal = column === 'name' ? '' : -Infinity;
        if (bVal === null || bVal === undefined) bVal = column === 'name' ? '' : -Infinity;

        // Name uses locale-aware string sorting; the metrics use numeric sorting.
        if (typeof aVal === 'string' && typeof bVal === 'string') {
            return direction === 'asc'
                ? aVal.localeCompare(bVal)
                : bVal.localeCompare(aVal);
        }

        return direction === 'asc' ? aVal - bVal : bVal - aVal;
    });
}

/**
 * Read a nested property using a dotted path such as stats.avgWaitTime.
 *
 * @param {Object} obj Source object.
 * @param {string} path Dotted property path.
 * @returns {*} Nested value or null when any level is missing.
 */
function getNestedValue(obj, path) {
    return path.split('.').reduce((current, prop) =>
        current && current[prop] !== undefined ? current[prop] : null, obj);
}

/**
 * Wire up the sortable column headers.
 * First click follows the column's preferred "best" direction, then toggles.
 *
 * @returns {void}
 */
function setupSortingListeners() {
    const headers = document.querySelectorAll('th.sortable');

    headers.forEach(header => {
        header.addEventListener('click', () => {
            const column = header.dataset.column;
            const bestDirection = header.dataset.best || 'desc';

            let direction;
            if (currentSort.column === column) {
                // Clicking the same header again just flips the order.
                direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
            } else {
                // New headers start from the direction that matches the metric definition.
                direction = bestDirection;
            }

            currentSort = { column, direction };

            updateSortIndicators(column, direction);

            renderTable(column, direction);
        });
    });
}

/**
 * Update the active sort arrow in the table header.
 *
 * @param {?string} activeColumn Column currently being sorted.
 * @param {?('asc'|'desc')} direction Current sort direction.
 * @returns {void}
 */
function updateSortIndicators(activeColumn, direction) {
    const headers = document.querySelectorAll('th.sortable');

    headers.forEach(header => {
        const indicator = header.querySelector('.sort-indicator');
        const column = header.dataset.column;

        if (!indicator) return;

        if (activeColumn && direction && column === activeColumn) {
            indicator.classList.add('active', direction);
            indicator.classList.remove(direction === 'asc' ? 'desc' : 'asc');
        } else {
            indicator.classList.remove('active', 'asc', 'desc');
        }
    });
}

/**
 * Highlight the best cell in each metric column.
 * This helper is currently unused but kept for future visual emphasis.
 *
 * @returns {void}
 */
function highlightBestValues() {
    const headers = document.querySelectorAll('th.sortable');

    headers.forEach((header, colIndex) => {
        const column = header.dataset.column;
        const bestDirection = header.dataset.best;

        if (!bestDirection || column === 'name') return;

        const cells = document.querySelectorAll(`#tableBody tr td:nth-child(${colIndex + 1})`);

        if (cells.length === 0) return;

        let bestValue = null;
        let bestCell = null;

        cells.forEach(cell => {
            const value = parseFloat(cell.dataset.value);
            if (isNaN(value)) return;

            if (bestValue === null) {
                bestValue = value;
                bestCell = cell;
            } else {
                const isBetter = bestDirection === 'desc'
                    ? value > bestValue
                    : value < bestValue;

                if (isBetter) {
                    bestValue = value;
                    bestCell = cell;
                }
            }
        });

        if (bestCell) {
            bestCell.classList.add('best-value');
        }
    });
}

/**
 * Format a numeric metric with a fixed number of decimal places.
 *
 * @param {*} value Raw value.
 * @param {number} [decimals=2] Number of decimal places.
 * @returns {string} Formatted number or '--'.
 */
function formatNumber(value, decimals = 2) {
    if (value === null || value === undefined || isNaN(value)) return '--';
    return Number(value).toFixed(decimals);
}

/**
 * Format a numeric value as a whole number.
 *
 * @param {*} value Raw value.
 * @returns {string} Rounded integer or '--'.
 */
function formatInteger(value) {
    if (value === null || value === undefined || isNaN(value)) return '--';
    return Math.round(Number(value)).toString();
}

/**
 * Escape text before inserting it into an HTML string.
 *
 * @param {string} text Raw text.
 * @returns {string} Escaped HTML-safe text.
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
