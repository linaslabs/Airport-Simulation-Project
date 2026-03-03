// Mock data storage - In real app, this would come from backend/localStorage
const mockSimulations = {
    'sim001': {
        id: 'sim001',
        name: 'SIM A - Baseline',
        timestamp: '2026-02-20 10:30',
        config: {
            'Number of Runways': '10',
            'Inbound Rate': '15 /hr',
            'Outbound Rate': '15 /hr',
            'Simulation Duration': '120 mins',
            'Max Wait Time': '30 mins',
            'Passenger Health Issue Rate': '0.0',
            'Runway Inspection Rate': '0.02',
            'Snow Clearance Rate': '0.1',
            'Equipment Failure Rate': '15'
        },
        events: [
            { event: 'Equipment Failure on Runway 01', time: '100 minutes', duration: '50 minutes', scheduled: 'Yes' },
            { event: 'Passenger Health in Holding Pattern', time: '250 minutes', duration: 'Indefinite', scheduled: 'Yes' },
            { event: 'Snow Clearance on Runway 08', time: '300 minutes', duration: '45 minutes', scheduled: 'No' },
            { event: 'Runway Maintenance', time: '350 minutes', duration: '30 minutes', scheduled: 'Yes' }
        ],
        statistics: {
            throughput: 17,
            depAvgWait: 7,
            depMaxQueue: 8,
            depMaxDelay: 15,
            depAvgDelay: 8,
            depCancelled: 2,
            arrAvgHold: 11,
            arrMaxHolding: 6,
            arrMaxDelay: 17,
            arrAvgDelay: 10,
            arrDiverted: 4
        }
    },
    'sim002': {
        id: 'sim002',
        name: 'SIM B - Optimized',
        timestamp: '2026-02-20 11:45',
        config: {
            'Number of Runways': '8',
            'Inbound Rate': '20 /hr',
            'Outbound Rate': '18 /hr',
            'Simulation Duration': '120 mins',
            'Max Wait Time': '25 mins',
            'Passenger Health Issue Rate': '0.01',
            'Runway Inspection Rate': '0.01',
            'Snow Clearance Rate': '0.05',
            'Equipment Failure Rate': '10'
        },
        events: [
            { event: 'Snow Clearance', time: '40 mins', duration: '45 mins', scheduled: 'No' }
        ],
        statistics: {
            throughput: 15,
            depAvgWait: 9,
            depMaxQueue: 7,
            depMaxDelay: 18,
            depAvgDelay: 10,
            depCancelled: 2,
            arrAvgHold: 5,
            arrMaxHolding: 2,
            arrMaxDelay: 7,
            arrAvgDelay: 4,
            arrDiverted: 0
        }
    },
    'sim003': {
        id: 'sim003',
        name: 'SIM C - Peak Hours',
        timestamp: '2026-02-20 12:15',
        config: {
            'Number of Runways': '12',
            'Inbound Rate': '25 /hr',
            'Outbound Rate': '22 /hr',
            'Simulation Duration': '120 mins',
            'Max Wait Time': '20 mins',
            'Passenger Health Issue Rate': '0.005',
            'Runway Inspection Rate': '0.015',
            'Snow Clearance Rate': '0.08',
            'Equipment Failure Rate': '12'
        },
        events: [
            { event: 'Weather Advisory', time: '10 mins', duration: '60 mins', scheduled: 'Yes' },
            { event: 'Fuel Supply Issue', time: '90 mins', duration: '15 mins', scheduled: 'No' }
        ],
        statistics: {
            throughput: 22,
            depAvgWait: 5,
            depMaxQueue: 6,
            depMaxDelay: 10,
            depAvgDelay: 5,
            depCancelled: 1,
            arrAvgHold: 8,
            arrMaxHolding: 4,
            arrMaxDelay: 12,
            arrAvgDelay: 7,
            arrDiverted: 2
        }
    },
    'sim004': {
        id: 'sim004',
        name: 'SIM D - Low Traffic',
        timestamp: '2026-02-20 14:30',
        config: {
            'Number of Runways': '6',
            'Inbound Rate': '10 /hr',
            'Outbound Rate': '10 /hr',
            'Simulation Duration': '120 mins',
            'Max Wait Time': '40 mins',
            'Passenger Health Issue Rate': '0.0',
            'Runway Inspection Rate': '0.01',
            'Snow Clearance Rate': '0.0',
            'Equipment Failure Rate': '8'
        },
        events: [],
        statistics: {
            throughput: 10,
            depAvgWait: 3,
            depMaxQueue: 3,
            depMaxDelay: 5,
            depAvgDelay: 2,
            depCancelled: 0,
            arrAvgHold: 2,
            arrMaxHolding: 2,
            arrMaxDelay: 4,
            arrAvgDelay: 1,
            arrDiverted: 0
        }
    },
    'sim005': {
        id: 'sim005',
        name: 'SIM E - High Stress',
        timestamp: '2026-02-20 15:45',
        config: {
            'Number of Runways': '15',
            'Inbound Rate': '30 /hr',
            'Outbound Rate': '28 /hr',
            'Simulation Duration': '120 mins',
            'Max Wait Time': '15 mins',
            'Passenger Health Issue Rate': '0.02',
            'Runway Inspection Rate': '0.03',
            'Snow Clearance Rate': '0.15',
            'Equipment Failure Rate': '20'
        },
        events: [
            { event: 'Multiple Equipment Failures', time: '30 mins', duration: '45 mins', scheduled: 'No' },
            { event: 'Passenger Emergency', time: '75 mins', duration: '20 mins', scheduled: 'Yes' }
        ],
        statistics: {
            throughput: 25,
            depAvgWait: 12,
            depMaxQueue: 12,
            depMaxDelay: 25,
            depAvgDelay: 15,
            depCancelled: 5,
            arrAvgHold: 14,
            arrMaxHolding: 7,
            arrMaxDelay: 22,
            arrAvgDelay: 12,
            arrDiverted: 6
        }
    }
};

// Track current simulations being compared
let currentSimIds = { A: 'sim001', B: 'sim002' };
let selectingFor = null; // 'A' or 'B'

/**
 * Load and display comparison for two simulations
 */
function loadComparison(simAId, simBId) {
    const simA = mockSimulations[simAId];
    const simB = mockSimulations[simBId];

    if (!simA || !simB) {
        console.error('Simulation not found');
        return;
    }

    // Update current simulation IDs
    currentSimIds.A = simAId;
    currentSimIds.B = simBId;

    // Update simulation names in column titles
    document.getElementById('simNameA').textContent = simA.name.toUpperCase();
    document.getElementById('simNameB').textContent = simB.name.toUpperCase();

    // Hide modal
    document.getElementById('selectModal').classList.add('hidden');

    // Populate both simulations
    populateSimulationData(simA, 'A');
    populateSimulationData(simB, 'B');

    // Highlight differences
    compareAndHighlight(simA, simB);
}

/**
 * Show view modal with simulation details
 */
function showViewModal(simId) {
    const sim = mockSimulations[simId];
    if (!sim) return;

    // Set title
    document.getElementById('viewModalTitle').textContent = sim.name;

    // Set statistics
    const stats = sim.statistics;
    document.getElementById('viewThroughput').textContent = stats.throughput;
    document.getElementById('viewDepAvgWait').textContent = stats.depAvgWait + ' mins';
    document.getElementById('viewDepMaxQueue').textContent = stats.depMaxQueue;
    document.getElementById('viewDepMaxDelay').textContent = stats.depMaxDelay + ' mins';
    document.getElementById('viewDepAvgDelay').textContent = stats.depAvgDelay + ' mins';
    document.getElementById('viewDepCancelled').textContent = stats.depCancelled;
    document.getElementById('viewArrAvgHold').textContent = stats.arrAvgHold + ' mins';
    document.getElementById('viewArrMaxHolding').textContent = stats.arrMaxHolding;
    document.getElementById('viewArrMaxDelay').textContent = stats.arrMaxDelay + ' mins';
    document.getElementById('viewArrAvgDelay').textContent = stats.arrAvgDelay + ' mins';
    document.getElementById('viewArrDiverted').textContent = stats.arrDiverted;

    // Show modal
    document.getElementById('viewModal').classList.remove('hidden');
}

/**
 * Hide view modal
 */
function hideViewModal() {
    document.getElementById('viewModal').classList.add('hidden');
}

/**
 * Hide select modal
 */
function hideSelectModal() {
    document.getElementById('selectModal').classList.add('hidden');
}

/**
 * Show modal with list of simulations to select for comparison
 */
function showSelectModal(columnLetter = null) {
    selectingFor = columnLetter; // Set which column we're selecting for
    const tbody = document.querySelector('#selectList tbody');
    tbody.innerHTML = '';

    // Update modal title based on which column we're selecting for
    if (selectingFor) {
        document.getElementById('selectModalTitle').textContent = `Select simulation for SIMULATION ${selectingFor}`;
    } else {
        document.getElementById('selectModalTitle').textContent = 'Select simulations to compare';
    }

    // Get all simulations for the list
    let simsList = Object.values(mockSimulations);

    // If selecting for a specific column, exclude the other column's current simulation
    if (selectingFor === 'A') {
        simsList = simsList.filter(sim => sim.id !== currentSimIds.B);
    } else if (selectingFor === 'B') {
        simsList = simsList.filter(sim => sim.id !== currentSimIds.A);
    }

    // Render table rows
    function renderTable(sims) {
        tbody.innerHTML = '';
        sims.forEach(sim => {
            const tr = document.createElement('tr');

            // Extract simulation info
            const infoLines = [
                `Runways: ${sim.config['Number of Runways']}`,
                `Inbound Rate: ${sim.config['Inbound Rate']}`,
                `Outbound Rate: ${sim.config['Outbound Rate']}`,
                `Scheduled Events: ${sim.events.length}`,
                `Throughput: ${sim.statistics.throughput} /hr`
            ];

            tr.innerHTML = `
                <td class="sim-name">${sim.name}</td>
                <td class="sim-date">${sim.timestamp}</td>
                <td class="sim-info">${infoLines.join('<br>')}</td>
                <td class="sim-options">
                    <button class="btn-option btn-view" data-sim-id="${sim.id}" title="View this simulation">View</button>
                    <button class="btn-option btn-compare" data-sim-id="${sim.id}" title="Compare with this simulation">Compare</button>
                    <button class="btn-option btn-delete" data-sim-id="${sim.id}" title="Delete this simulation">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Bind click handlers
        tbody.querySelectorAll('button.btn-view').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const simId = e.target.getAttribute('data-sim-id');
                showViewModal(simId);
            });
        });

        tbody.querySelectorAll('button.btn-compare').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const simId = e.target.getAttribute('data-sim-id');
                if (selectingFor === 'A') {
                    loadComparison(simId, currentSimIds.B);
                } else if (selectingFor === 'B') {
                    loadComparison(currentSimIds.A, simId);
                } else {
                    // Original behavior when not selecting for a specific column
                    loadComparison('sim001', simId);
                }
            });
        });

        tbody.querySelectorAll('button.btn-delete').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const simId = e.target.getAttribute('data-sim-id');
                const sim = mockSimulations[simId];
                if (confirm('Delete simulation ' + sim.name + '?')) {
                    delete mockSimulations[simId];
                    showSelectModal(selectingFor); // Refresh the list
                }
            });
        });
    }

    // Initial render
    renderTable(simsList);

    // Add sorting functionality to headers
    const thead = document.querySelector('#selectList thead');
    const headers = thead.querySelectorAll('th.sortable');

    let sortState = { column: null, ascending: true };

    headers.forEach(header => {
        header.addEventListener('click', () => {
            const columnIndex = Array.from(header.parentNode.children).indexOf(header);
            const columnName = columnIndex === 0 ? 'name' : 'date';

            // Toggle sort direction if clicking same column
            if (sortState.column === columnName) {
                sortState.ascending = !sortState.ascending;
            } else {
                sortState.column = columnName;
                sortState.ascending = true;
            }

            // Sort the list
            if (columnName === 'name') {
                simsList.sort((a, b) => {
                    const comparison = a.name.localeCompare(b.name);
                    return sortState.ascending ? comparison : -comparison;
                });
            } else if (columnName === 'date') {
                simsList.sort((a, b) => {
                    const dateA = new Date(a.timestamp);
                    const dateB = new Date(b.timestamp);
                    return sortState.ascending ? dateA - dateB : dateB - dateA;
                });
            }

            // Update arrow indicators
            headers.forEach(h => {
                const arrow = h.querySelector('.sort-arrow');
                if (arrow) arrow.textContent = '↑↓';
            });
            const activeArrow = header.querySelector('.sort-arrow');
            if (activeArrow) {
                activeArrow.textContent = sortState.ascending ? '↑' : '↓';
            }

            // Re-render table
            renderTable(simsList);
        });
    });

    document.getElementById('selectModal').classList.remove('hidden');
}

/**
 * Initialize comparison page on load
 */
document.addEventListener('DOMContentLoaded', function() {
    // Load initial comparison
    loadComparison(currentSimIds.A, currentSimIds.B);

    // Add event listeners for switch buttons
    document.getElementById('switchBtnA').addEventListener('click', () => {
        showSelectModal('A');
    });

    document.getElementById('switchBtnB').addEventListener('click', () => {
        showSelectModal('B');
    });

    // Close select modal
    document.getElementById('closeSelectModal').addEventListener('click', hideSelectModal);

    // Close select modal if click outside content
    document.getElementById('selectModal').addEventListener('click', (e) => {
        if (e.target.id === 'selectModal') hideSelectModal();
    });

    // Close view modal
    document.getElementById('closeViewModal').addEventListener('click', hideViewModal);

    // Close view modal if click outside content
    document.getElementById('viewModal').addEventListener('click', (e) => {
        if (e.target.id === 'viewModal') hideViewModal();
    });
});

/**
 * Populate all data for a simulation
 */
function populateSimulationData(simData, suffix) {
    // Configuration Table
    populateConfigTable(simData, `config${suffix}`);

    // Events Table
    populateEventsTable(simData, `events${suffix}`);

    // Simulation Statistics
    const stats = simData.statistics;
    document.getElementById(`throughput${suffix}`).textContent = stats.throughput + ' /hr';
    document.getElementById(`depAvgWait${suffix}`).textContent = stats.depAvgWait + ' mins';
    document.getElementById(`depMaxQueue${suffix}`).textContent = stats.depMaxQueue;
    document.getElementById(`depMaxDelay${suffix}`).textContent = stats.depMaxDelay + ' mins';
    document.getElementById(`depAvgDelay${suffix}`).textContent = stats.depAvgDelay + ' mins';
    document.getElementById(`depCancelled${suffix}`).textContent = stats.depCancelled;
    document.getElementById(`arrAvgHold${suffix}`).textContent = stats.arrAvgHold + ' mins';
    document.getElementById(`arrMaxHolding${suffix}`).textContent = stats.arrMaxHolding;
    document.getElementById(`arrMaxDelay${suffix}`).textContent = stats.arrMaxDelay + ' mins';
    document.getElementById(`arrAvgDelay${suffix}`).textContent = stats.arrAvgDelay + ' mins';
    document.getElementById(`arrDiverted${suffix}`).textContent = stats.arrDiverted;
}

/**
 * Populate configuration table for a simulation
 */
function populateConfigTable(simData, targetId) {
    const table = document.getElementById(targetId);
    table.innerHTML = '';

    for (const [key, value] of Object.entries(simData.config)) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${key}</td>
            <td>${value}</td>
        `;
        row.id = `config-${simData.id}-${key.toLowerCase().replace(/\s+/g, '-')}`;
        table.appendChild(row);
    }
}

/**
 * Populate events table for a simulation
 */
function populateEventsTable(simData, targetId) {
    const tbody = document.querySelector(`#${targetId} tbody`);
    tbody.innerHTML = '';

    if (!simData.events || simData.events.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="4" style="text-align: center; color: #999;">No events scheduled</td>';
        tbody.appendChild(row);
        return;
    }

    simData.events.forEach(event => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${event.event}</td>
            <td>${event.time}</td>
            <td>${event.duration}</td>
            <td>${event.scheduled}</td>
        `;
        tbody.appendChild(row);
    });
}

/**
 * Compare two simulations and highlight differences
 */
function compareAndHighlight(simA, simB) {
    const metricConfig = {
        // Higher is better
        'throughput': { higherBetter: true },
        // Lower is better
        'depAvgWait': { higherBetter: false },
        'depMaxQueue': { higherBetter: false },
        'depMaxDelay': { higherBetter: false },
        'depAvgDelay': { higherBetter: false },
        'depCancelled': { higherBetter: false },
        'arrAvgHold': { higherBetter: false },
        'arrMaxHolding': { higherBetter: false },
        'arrMaxDelay': { higherBetter: false },
        'arrAvgDelay': { higherBetter: false },
        'arrDiverted': { higherBetter: false }
    };

    // Compare each metric
    Object.keys(metricConfig).forEach(metric => {
        const valA = simA.statistics[metric];
        const valB = simB.statistics[metric];

        if (valA === undefined || valB === undefined) return;

        const config = metricConfig[metric];
        let aIsBetter, bIsBetter;

        if (config.higherBetter) {
            // Higher is better
            aIsBetter = valA > valB;
            bIsBetter = valB > valA;
        } else {
            // Lower is better
            aIsBetter = valA < valB;
            bIsBetter = valB < valA;
        }

        // Apply highlighting
        const elementA = document.getElementById(metric + 'A');
        const elementB = document.getElementById(metric + 'B');

        if (elementA && elementB) {
            // Remove all highlight classes first
            elementA.classList.remove('better', 'worse');
            elementB.classList.remove('better', 'worse');

            if (aIsBetter) {
                elementA.classList.add('better');
                elementB.classList.add('worse');
            } else if (bIsBetter) {
                elementB.classList.add('better');
                elementA.classList.add('worse');
            }
        }
    });
}
