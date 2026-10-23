package com.example.demo.infrastructure.persistence;

import com.example.demo.application.ports.CachePort;
import com.example.demo.application.ports.EnlaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Fábrica Concreta del Patrón Abstract Factory: MongoRedisPersistenceFactory.
 *
 * Rol en la Arquitectura:
 * Es la implementación concreta de {@link PersistenceFactory}.
 * Se encarga de instanciar y ensamblar la familia concreta de adaptadores:
 * - {@link EnlaceRepositoryMongo} para persistencia duradera en MongoDB.
 * - {@link CacheRedis} para almacenamiento en memoria de alto rendimiento y TTL en Redis.
 *
 * De este modo, la capa de aplicación desconoce los detalles de creación
 * de cada componente tecnológico.
 */
public class MongoRedisPersistenceFactory implements PersistenceFactory {

    private final EnlaceMongoSpringRepository mongoSpringRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MongoRedisPersistenceFactory(EnlaceMongoSpringRepository mongoSpringRepository,
                                        StringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper) {
        this.mongoSpringRepository = mongoSpringRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public EnlaceRepository crearRepositorioEnlaces() {
        return new EnlaceRepositoryMongo(mongoSpringRepository);
    }

    @Override
    public CachePort crearCache() {
        return new CacheRedis(redisTemplate, objectMapper);
    }
}
