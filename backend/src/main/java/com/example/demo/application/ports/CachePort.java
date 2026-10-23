package com.example.demo.application.ports;

import com.example.demo.domain.model.Enlace;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida: CachePort.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es una interfaz que desacopla la lógica de aplicación del motor de caché en memoria (Redis).
 * Provee operaciones para consultar, almacenar con tiempo de vida (TTL), invalidar
 * y leer el TTL real directamente del motor de caché.
 */
public interface CachePort {

    /**
     * Almacena la lista de enlaces en caché con un tiempo de expiración (TTL) en segundos.
     * Solo debe invocarse durante un cache miss.
     *
     * @param enlaces     Lista de enlaces a cachear.
     * @param ttlSegundos Tiempo de vida en segundos (ej. 30 segundos).
     */
    void guardarListado(List<Enlace> enlaces, long ttlSegundos);

    /**
     * Obtiene la lista de enlaces cacheados si la clave existe.
     * NO modifica ni reinicia el TTL de la clave.
     *
     * @return Optional con la lista de enlaces si está en caché, o vacío si hubo cache miss.
     */
    Optional<List<Enlace>> obtenerListado();

    /**
     * Invalida (elimina) la clave del listado en el caché.
     * Se debe invocar inmediatamente después de registrar un nuevo enlace.
     */
    void invalidarListado();

    /**
     * Obtiene el tiempo de vida restante real (TTL) en segundos directamente del motor de caché.
     *
     * @return TTL en segundos (> 0), -2 si la clave no existe, o -1 si no tiene tiempo de expiración.
     */
    long obtenerTtlListado();
}
