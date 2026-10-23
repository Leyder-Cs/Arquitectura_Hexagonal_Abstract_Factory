package com.example.demo.infrastructure.persistence;

import com.example.demo.application.ports.CachePort;
import com.example.demo.domain.model.Enlace;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador de Caché Redis: CacheRedis.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es el Adaptador de Salida (Output Adapter) para Redis. Implementa el puerto
 * {@link CachePort} de la capa de aplicación.
 *
 * Manejo estricto de TTL y Cache-Aside:
 * - 'guardarListado': escribe el listado serializado en JSON con TTL explícito (30s) SOLO en cache miss.
 * - 'obtenerListado': realiza un simple 'GET' en Redis; NUNCA altera ni resetea el TTL restante.
 * - 'invalidarListado': borra la clave inmediatamente tras guardar un nuevo enlace.
 * - 'obtenerTtlListado': consulta el comando TTL nativo de Redis en segundos.
 */
public class CacheRedis implements CachePort {

    private static final Logger log = LoggerFactory.getLogger(CacheRedis.class);
    public static final String CLAVE_CACHE_LISTADO = "enlaces:listado";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheRedis(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void guardarListado(List<Enlace> enlaces, long ttlSegundos) {
        try {
            String json = objectMapper.writeValueAsString(enlaces);
            redisTemplate.opsForValue().set(CLAVE_CACHE_LISTADO, json, Duration.ofSeconds(ttlSegundos));
            log.debug("Listado de enlaces guardado en Redis con TTL de {} segundos.", ttlSegundos);
        } catch (Exception e) {
            log.error("Error al serializar y guardar listado en Redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<List<Enlace>> obtenerListado() {
        try {
            // Se realiza únicamente lectura (GET). NO se toca ni se reinicia el TTL.
            String json = redisTemplate.opsForValue().get(CLAVE_CACHE_LISTADO);
            if (json == null || json.isBlank()) {
                log.debug("Cache Miss en Redis para la clave '{}'", CLAVE_CACHE_LISTADO);
                return Optional.empty();
            }

            List<Enlace> enlaces = objectMapper.readValue(json, new TypeReference<List<Enlace>>() {});
            log.debug("Cache Hit en Redis para la clave '{}'. Retornando {} elementos.", CLAVE_CACHE_LISTADO, enlaces.size());
            return Optional.of(enlaces);
        } catch (Exception e) {
            log.error("Error al leer listado desde Redis: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void invalidarListado() {
        try {
            Boolean eliminado = redisTemplate.delete(CLAVE_CACHE_LISTADO);
            log.debug("Invalidación de caché para clave '{}': {}", CLAVE_CACHE_LISTADO, eliminado);
        } catch (Exception e) {
            log.error("Error al invalidar caché en Redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public long obtenerTtlListado() {
        try {
            // Comando TTL nativo de Redis en segundos:
            // > 0 : segundos restantes
            // -2  : la clave no existe o ha expirado
            // -1  : la clave existe pero no tiene tiempo de expiración
            Long ttl = redisTemplate.getExpire(CLAVE_CACHE_LISTADO, TimeUnit.SECONDS);
            return (ttl != null) ? ttl : -2L;
        } catch (Exception e) {
            log.error("Error al consultar TTL de Redis: {}", e.getMessage());
            return -2L;
        }
    }
}
