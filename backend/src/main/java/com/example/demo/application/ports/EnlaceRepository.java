package com.example.demo.application.ports;

import com.example.demo.domain.model.Enlace;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida: EnlaceRepository.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es una interfaz que define el contrato de persistencia requerido por el negocio.
 * La capa de aplicación depende de este puerto, permitiendo intercambiar la
 * base de datos real (MongoDB, PostgreSQL, en memoria) sin alterar la lógica de negocio.
 */
public interface EnlaceRepository {

    /**
     * Guarda un nuevo enlace acortado en el sistema de almacenamiento.
     *
     * @param enlace La entidad de dominio a persistir.
     * @return La entidad de dominio persistida, con su ID asignado.
     */
    Enlace guardar(Enlace enlace);

    /**
     * Busca un enlace por su código alfanumérico corto.
     *
     * @param codigo Código alfanumérico generado.
     * @return Optional con el enlace si existe, o vacío si no se encuentra.
     */
    Optional<Enlace> buscarPorCodigo(String codigo);

    /**
     * Verifica si ya existe un enlace registrado con el código indicado.
     *
     * @param codigo Código alfanumérico.
     * @return true si ya existe, false en caso contrario.
     */
    boolean existePorCodigo(String codigo);

    /**
     * Recupera todos los enlaces registrados en el almacenamiento.
     *
     * @return Lista completa de enlaces.
     */
    List<Enlace> obtenerTodos();
}
