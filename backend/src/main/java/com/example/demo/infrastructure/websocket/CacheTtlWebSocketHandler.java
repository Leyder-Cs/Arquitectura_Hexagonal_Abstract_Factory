package com.example.demo.infrastructure.websocket;

import com.example.demo.application.ports.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manejador de WebSocket para TTL en Tiempo Real: CacheTtlWebSocketHandler.
 *
 * Rol en la Arquitectura:
 * Adaptador de Entrada/Salida en tiempo real (WebSocket Adapter).
 * Lee el TTL REAL directamente de Redis (comando TTL) a través del puerto {@link CachePort}.
 *
 * Reglas de Envío:
 * 1. Al conectarse un cliente, responde inmediatamente con el TTL real de Redis.
 * 2. Cada segundo emite un pulso a todos los clientes conectados:
 *    - Si TTL > 0: {"cacheado": true, "segundosRestantes": X}
 *    - Si TTL == -2 o TTL <= 0 o TTL == -1: {"cacheado": false}
 * 3. Si el usuario recarga la página, la nueva conexión recupera el tiempo restante del servidor.
 */
@Component
public class CacheTtlWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CacheTtlWebSocketHandler.class);
    private final CachePort cachePort;
    private final Set<WebSocketSession> sesiones = ConcurrentHashMap.newKeySet();

    public CacheTtlWebSocketHandler(CachePort cachePort) {
        this.cachePort = cachePort;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sesiones.add(session);
        log.info("Nueva conexión WebSocket establecida desde: {} (ID: {})", session.getRemoteAddress(), session.getId());

        // Al conectarse, enviar de inmediato el estado del TTL real de Redis
        long ttlActual = cachePort.obtenerTtlListado();
        String jsonInicial = construirJsonTtl(ttlActual);
        enviarMensaje(session, jsonInicial);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.remove(session);
        log.info("Conexión WebSocket cerrada: {} con estado {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Error en transporte WebSocket para la sesión {}: {}", session.getId(), exception.getMessage());
        sesiones.remove(session);
        try {
            if (session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Tarea programada ejecutada cada segundo (1000 ms).
     * Lee el comando TTL real de Redis y lo emite a las sesiones activas.
     */
    @Scheduled(fixedRate = 1000)
    public void emitirTtlPeriodicamente() {
        if (sesiones.isEmpty()) {
            return;
        }

        long ttlActual = cachePort.obtenerTtlListado();
        String jsonPayload = construirJsonTtl(ttlActual);

        for (WebSocketSession session : sesiones) {
            if (session.isOpen()) {
                enviarMensaje(session, jsonPayload);
            } else {
                sesiones.remove(session);
            }
        }
    }

    /**
     * Construye el JSON exacto requerido:
     * - Si TTL > 0: {"cacheado": true, "segundosRestantes": X}
     * - Si TTL <= 0 (incluyendo -2 clave inexistente y -1 sin expiración): {"cacheado": false}
     */
    private String construirJsonTtl(long ttl) {
        if (ttl > 0) {
            return "{\"cacheado\":true,\"segundosRestantes\":" + ttl + "}";
        } else {
            return "{\"cacheado\":false}";
        }
    }

    private synchronized void enviarMensaje(WebSocketSession session, String mensaje) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(mensaje));
            }
        } catch (IOException e) {
            log.error("Error al enviar mensaje WebSocket a la sesión {}: {}", session.getId(), e.getMessage());
            sesiones.remove(session);
        }
    }
}
