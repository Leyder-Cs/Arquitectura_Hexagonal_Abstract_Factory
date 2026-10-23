package com.example.demo.infrastructure.persistence;

import com.example.demo.application.ports.EnlaceRepository;
import com.example.demo.domain.model.Enlace;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de Persistencia MongoDB: EnlaceRepositoryMongo.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es el Adaptador de Salida (Output Adapter) para MongoDB. Implementa el puerto
 * {@link EnlaceRepository} definido en la capa de aplicación.
 * Traduce entre la entidad pura del dominio ({@link Enlace}) y el documento
 * técnico de MongoDB ({@link EnlaceDocument}), manteniendo la inversión de dependencias.
 */
public class EnlaceRepositoryMongo implements EnlaceRepository {

    private final EnlaceMongoSpringRepository springMongoRepository;

    public EnlaceRepositoryMongo(EnlaceMongoSpringRepository springMongoRepository) {
        this.springMongoRepository = springMongoRepository;
    }

    @Override
    public Enlace guardar(Enlace enlace) {
        EnlaceDocument doc = toDocument(enlace);
        EnlaceDocument guardado = springMongoRepository.save(doc);
        return toDomain(guardado);
    }

    @Override
    public Optional<Enlace> buscarPorCodigo(String codigo) {
        return springMongoRepository.findByCodigo(codigo)
                .map(this::toDomain);
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return springMongoRepository.existsByCodigo(codigo);
    }

    @Override
    public List<Enlace> obtenerTodos() {
        return springMongoRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private EnlaceDocument toDocument(Enlace enlace) {
        return new EnlaceDocument(
                enlace.getId(),
                enlace.getCodigo(),
                enlace.getEnlaceOriginal(),
                enlace.getEnlaceAcortado(),
                enlace.getFechaCreacion()
        );
    }

    private Enlace toDomain(EnlaceDocument doc) {
        return new Enlace(
                doc.getId(),
                doc.getCodigo(),
                doc.getEnlaceOriginal(),
                doc.getEnlaceAcortado(),
                doc.getFechaCreacion()
        );
    }
}
