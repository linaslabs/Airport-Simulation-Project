// ===== COMPARE ALL SIMULATIONS SCRIPT =====

const API = {
    summaries: '/api/results/summaries',
    viewOrCompare: (name) => `/api/results/vieworcompare/${encodeURIComponent(name)}`,
    deleteResult: (name) => `/api/results/delete/${encodeURIComponent(name)}`
};

let allSimulations = [];
let currentSort = {
    column: null,
    direction: null // 'asc' or 'desc'
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
    await loadAllSimulations();
    setupSortingListeners();
});

// Fetch all simulation summaries
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

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Request failed (${response.status})`);
    }
    return response.json();
}

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

// Render the table with simulation data
function renderTable(sortColumn = null, sortDirection = null) {
    const tbody = document.getElementById('tableBody');

    if (!allSimulations || allSimulations.length === 0) {
        tbody.innerHTML = '<tr class="no-data"><td colspan="13">No simulation results available.</td></tr>';
        return;
    }

    // Clone and sort data if needed
    let displayData = [...allSimulations];

    if (sortColumn && sortDirection) {
        displayData = sortData(displayData, sortColumn, sortDirection);
    }

    // Build table rows
    tbody.innerHTML = displayData.map(sim => {
        const stats = sim.stats || {};
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

    // Highlight best values
    highlightBestValues();
}

function bindDeleteButtons() {
    const buttons = document.querySelectorAll('.btn-delete');

    buttons.forEach(button => {
        button.addEventListener('click', async () => {
            const simName = button.getAttribute('data-sim-name');
            if (!simName) return;

            if (!confirm(`Delete simulation ${simName}?`)) {
                return;
            }

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

// Sort data based on column and direction
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

        // Handle null/undefined values
        if (aVal === null || aVal === undefined) aVal = column === 'name' ? '' : -Infinity;
        if (bVal === null || bVal === undefined) bVal = column === 'name' ? '' : -Infinity;

        // String comparison
        if (typeof aVal === 'string' && typeof bVal === 'string') {
            return direction === 'asc'
                ? aVal.localeCompare(bVal)
                : bVal.localeCompare(aVal);
        }

        // Numeric comparison
        return direction === 'asc' ? aVal - bVal : bVal - aVal;
    });
}

// Get nested object value by path (e.g., 'stats.avgWaitTime')
function getNestedValue(obj, path) {
    return path.split('.').reduce((current, prop) =>
        current && current[prop] !== undefined ? current[prop] : null, obj);
}

// Set up click listeners for sortable columns
function setupSortingListeners() {
    const headers = document.querySelectorAll('th.sortable');

    headers.forEach(header => {
        header.addEventListener('click', () => {
            const column = header.dataset.column;
            const bestDirection = header.dataset.best || 'desc';

            // Determine sort direction
            let direction;
            if (currentSort.column === column) {
                // Toggle direction
                direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
            } else {
                // First click: sort by "best" direction
                direction = bestDirection;
            }

            // Update sort state
            currentSort = { column, direction };

            // Update UI indicators
            updateSortIndicators(column, direction);

            // Re-render table
            renderTable(column, direction);
        });
    });
}

// Update sort indicator arrows in headers
function updateSortIndicators(activeColumn, direction) {
    const headers = document.querySelectorAll('th.sortable');

    headers.forEach(header => {
        const indicator = header.querySelector('.sort-indicator');
        const column = header.dataset.column;

        if (column === activeColumn) {
            indicator.classList.add('active', direction);
            indicator.classList.remove(direction === 'asc' ? 'desc' : 'asc');
        } else {
            indicator.classList.remove('active', 'asc', 'desc');
        }
    });
}

// Highlight the best value in each column
function highlightBestValues() {
    const headers = document.querySelectorAll('th.sortable');

    headers.forEach((header, colIndex) => {
        const column = header.dataset.column;
        const bestDirection = header.dataset.best;

        if (!bestDirection || column === 'name') return;

        // Get all cells in this column
        const cells = document.querySelectorAll(`#tableBody tr td:nth-child(${colIndex + 1})`);

        if (cells.length === 0) return;

        // Find best value
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

        // Highlight best cell
        if (bestCell) {
            bestCell.classList.add('best-value');
        }
    });
}

// Utility: Format number with decimals
function formatNumber(value, decimals = 2) {
    if (value === null || value === undefined || isNaN(value)) return '--';
    return Number(value).toFixed(decimals);
}

// Utility: Format integer
function formatInteger(value) {
    if (value === null || value === undefined || isNaN(value)) return '--';
    return Math.round(Number(value)).toString();
}

// Utility: Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
