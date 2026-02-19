package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

public enum EmergencyStatus {
    FUEL,               // Highest Priority
    MECHANICAL,
    PASSENGER,
    NONE                // Lowest Priority
}

// The values are written in a specific order for the compareTo method in Aircraft class.
