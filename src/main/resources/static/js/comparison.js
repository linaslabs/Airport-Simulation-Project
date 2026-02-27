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
    }
};

/**
 * Initialize comparison page on load
 */
document.addEventListener('DOMContentLoaded', function() {
    // Load default simulations
    const simA = mockSimulations['sim001'];
    const simB = mockSimulations['sim002'];

    // Populate both simulations
    populateSimulationData(simA, 'A');
    populateSimulationData(simB, 'B');

    // Highlight differences
    compareAndHighlight(simA, simB);
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
