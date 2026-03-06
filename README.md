# ✈️ AirSim: Dynamic Airport Operations Modelling System

> **A robust, web-based simulation engine designed to optimise airport throughput, manage air traffic queues, and model complex operational scenarios.** Developed as part of the CS261 Software Engineering module at the University of Warwick, this project addresses a simulated commercial brief provided by Dorset Software. The system allows airport managers to configure runway parameters, inject real-time emergencies, and analyse critical data to maximise revenue, safety, and operational efficiency. 

## Key Features

* **Tick-Based Simulation Engine:** A synchronous, discrete time-step engine that prevents race conditions and handles continuous aircraft generation using normal distribution offsets for realistic scheduling.
* **Complex Queue Management:** * *Holding Patterns:* Implements a priority queue sorting inbound aircraft by emergency status, fuel level, and arrival time. Enforces a strict minimum 1000ft vertical separation.
  * *Take-Off Queues:* Manages outbound traffic using a FIFO structure, monitoring max wait times to trigger automated flight cancellations.
* **Dynamic Runway Allocation:** Supports 1-10 configurable runways operating in Landing, Take-Off, or Mixed Mode. The system safely limits occupancy to one aircraft per runway and dynamically shifts allocation during events like snow clearance or equipment failure.
* **Real-Time Event Scheduling:** Users can schedule or manually inject runway closures, mechanical failures, and fuel emergencies mid-simulation to test system resilience.
* **Data Persistence & Analytics:** Captures rich simulation metrics (throughput, delay times, diversion counts) and serialises configurations and event logs to JSON formats using the Jackson library, allowing for side-by-side scenario comparisons.

## Technical Architecture

The application is built on a strict **Model-View-Controller (MVC)** architectural pattern to ensure high modularity and separation of concerns. 

* **Model:** Houses the `SimulationEngine`, `Airport`, and `AircraftGenerator`. It safely manages domain logic, aircraft generation, fuel consumption, and queue reprioritization.
* **View:** A high-performance, web-based UI utilizing the HTML5 Canvas API for real-time 2D spatial rendering of aircraft zones, accompanied by dynamic data tables.
* **Controller:** Facilitated by Spring Boot REST endpoints, translating user configurations into simulation states and providing real-time data updates to the client.

## Technology Stack

* **Backend:** Java 25 (chosen for strict static typing and object-oriented mapping of complex entities).
* **Framework:** Spring Boot (embeds a web server to decouple frontend/backend via REST APIs).
* **Frontend:** HTML5, CSS3, Vanilla JavaScript, HTML5 Canvas API.
* **Data Storage:** JSON integrated via the Jackson Library for lightweight, human-readable data persistence.
* **Testing & Build:** JUnit 6, Mockito, MockMvc, Maven.

## Interface Previews
<img width="1892" height="985" alt="image" src="https://github.com/user-attachments/assets/ffb97fd6-1887-4154-a829-54d5399e3856" />
<img width="1849" height="921" alt="image" src="https://github.com/user-attachments/assets/3e47b4af-f351-45c7-b6d2-4f6e0c98c290" />
<img width="1855" height="896" alt="image" src="https://github.com/user-attachments/assets/1ec143a9-63ab-40ab-b5dc-7856cdc146ff" />
