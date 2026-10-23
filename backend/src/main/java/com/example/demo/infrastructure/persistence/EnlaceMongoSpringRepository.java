package com.example.demo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data MongoDB: EnlaceMongoSpringRepository.
 *
 * Rol en la Arquitectura:
 * Componente tecnológico de bajo nivel provisto por Spring Data MongoDB.
 * Ofrece operaciones CRUD automáticas contra la base de datos documental.
 */
@Repository
public interface EnlaceMongoSpringRepository extends MongoRepository<EnlaceDocument, String> {

    /**
     * Busca un documento por su código único indexado.
     */
    Optional<EnlaceDocument> findByCodigo(String codigo);

    /**
     * Comprueba la existencia de un enlace con dicho código.
     */
    boolean existsByCodigo(String codigo);
}
