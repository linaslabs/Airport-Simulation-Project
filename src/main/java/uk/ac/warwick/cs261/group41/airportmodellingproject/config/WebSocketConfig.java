package uk.ac.warwick.cs261.group41.airportmodellingproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Frontend will subscribe to the simulation with topics prefixed with "/simulation", backend pushes new snapshots to this channel e.g. /simulation/snapshot
        config.enableSimpleBroker("/simulation");
        // Following added and can be amended for if the backend wants to subscribe for updates from the frontend
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint the frontend will use to connect to the websocket. It allows requests from localhost only (CHANGE IF WE PUT THIS INTO PRODUCTION)
        registry.addEndpoint("/simulation-websocket").setAllowedOriginPatterns("http://localhost:*", "https://localhost:*").withSockJS();
    }
}