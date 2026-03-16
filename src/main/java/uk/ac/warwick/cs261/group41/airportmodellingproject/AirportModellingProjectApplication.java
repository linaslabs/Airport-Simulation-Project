package uk.ac.warwick.cs261.group41.airportmodellingproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Airport Modelling Project application.
 * Starting this class bootstraps the embedded server and loads all Spring components.
 */
@SpringBootApplication
public class AirportModellingProjectApplication {

    /**
     * Launches the application.
     *
     * @param args command-line arguments passed to the Spring Boot application
     */
    public static void main(String[] args) {
        SpringApplication.run(AirportModellingProjectApplication.class, args);
    }

}
