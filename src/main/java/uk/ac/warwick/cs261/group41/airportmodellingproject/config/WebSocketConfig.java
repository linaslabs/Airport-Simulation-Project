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
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint the frontend will use to connect to the websocket
        registry.addEndpoint("/simulation-websocket").setAllowedOriginPatterns("*").withSockJS();
    }
}