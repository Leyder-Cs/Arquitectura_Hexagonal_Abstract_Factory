package com.example.demo.infrastructure.persistence;

import com.example.demo.application.ports.CachePort;
import com.example.demo.application.ports.EnlaceRepository;
import com.example.demo.application.service.EnlaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Configuración de Persistencia e Inyección de Dependencias en Spring Boot.
 *
 * Rol en la Arquitectura:
 * Es el ensamblador (Composition Root). Conecta la capa de aplicación con la
 * infraestructura a través del patrón Abstract Factory:
 * 1. Registra el Bean de {@link PersistenceFactory} (MongoRedisPersistenceFactory).
 * 2. Obtiene {@link EnlaceRepository} invocando 'factory.crearRepositorioEnlaces()'.
 * 3. Obtiene {@link CachePort} invocando 'factory.crearCache()'.
 * 4. Inyecta ambos puertos en el servicio de aplicación {@link EnlaceService}.
 */
@Configuration
public class PersistenceConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public PersistenceFactory persistenceFactory(EnlaceMongoSpringRepository mongoSpringRepository,
                                                StringRedisTemplate redisTemplate,
                                                ObjectMapper objectMapper) {
        return new MongoRedisPersistenceFactory(mongoSpringRepository, redisTemplate, objectMapper);
    }

    @Bean
    public EnlaceRepository enlaceRepository(PersistenceFactory persistenceFactory) {
        // Obtenido directamente de la Fábrica Abstracta
        return persistenceFactory.crearRepositorioEnlaces();
    }

    @Bean
    public CachePort cachePort(PersistenceFactory persistenceFactory) {
        // Obtenido directamente de la Fábrica Abstracta
        return persistenceFactory.crearCache();
    }

    @Bean
    public EnlaceService enlaceService(EnlaceRepository enlaceRepository,
                                       CachePort cachePort,
                                       @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        // El servicio de aplicación recibe exclusivamente los puertos
        return new EnlaceService(enlaceRepository, cachePort, baseUrl);
    }
}
