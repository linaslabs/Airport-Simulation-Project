package uk.ac.warwick.cs261.group41.airportmodellingproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for WebSocket messaging using the STOMP protocol.
 * Sets up a simple in-memory message broker and registers the STOMP endpoint
 * that clients use to establish a WebSocket connection.
 *
 * Note: allowed origin patterns are restricted to localhost and must be updated
 * before deploying to production.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker used to route messages between the server and clients.
     * Enables a simple in-memory broker on the /simulation destination prefix, allowing
     * the backend to push updates to channels such as /simulation/snapshot.
     * Also sets /app as the application destination prefix for any messages intended
     * to be handled by @MessageMapping methods on the backend.
     *
     * @param config the MessageBrokerRegistry used to configure broker options
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Frontend will subscribe to the simulation with topics prefixed with "/simulation", backend pushes new snapshots to this channel e.g. /simulation/snapshot
        config.enableSimpleBroker("/simulation");
        // Following added and can be amended for if the backend wants to subscribe for updates from the frontend
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers the STOMP WebSocket endpoint that clients connect to.
     * Exposes /simulation-websocket with SockJS fallback support, allowing clients
     * without native WebSocket support to use alternative transports.
     * Only permits connections from localhost origins.
     *
     * @param registry the StompEndpointRegistry used to register STOMP endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint the frontend will use to connect to the websocket. It allows requests from localhost only (CHANGE IF WE PUT THIS INTO PRODUCTION)
        registry.addEndpoint("/simulation-websocket").setAllowedOriginPatterns("http://localhost:*", "https://localhost:*").withSockJS();
    }
}