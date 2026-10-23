package com.example.demo.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuración de WebSocket: WebSocketConfig.
 *
 * Rol en la Arquitectura:
 * Configura la infraestructura de comunicación bidireccional en tiempo real.
 * Registra el handler {@link CacheTtlWebSocketHandler} en el endpoint '/ws/cache'
 * y define las políticas de CORS requeridas para el frontend Vite (http://localhost:5173).
 */
@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private final CacheTtlWebSocketHandler cacheTtlWebSocketHandler;

    public WebSocketConfig(CacheTtlWebSocketHandler cacheTtlWebSocketHandler) {
        this.cacheTtlWebSocketHandler = cacheTtlWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cacheTtlWebSocketHandler, "/ws/cache")
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:3000",
                        "*"
                );
    }
}
