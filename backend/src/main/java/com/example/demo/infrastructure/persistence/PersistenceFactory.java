package com.example.demo.infrastructure.persistence;

import com.example.demo.application.ports.CachePort;
import com.example.demo.application.ports.EnlaceRepository;

/**
 * Patrón de Diseño: Abstract Factory (Fábrica Abstracta).
 *
 * Rol en la Arquitectura:
 * Declara una interfaz para la creación de una familia de objetos relacionados
 * o dependientes (repositorio de persistencia y adaptador de caché) sin
 * especificar sus clases concretas.
 *
 * Permite cambiar la familia completa de almacenamiento (por ejemplo, cambiar
 * Mongo/Redis por Postgres/Memcached) simplemente proporcionando otra fábrica concreta.
 */
public interface PersistenceFactory {

    /**
     * Crea y devuelve la instancia del repositorio de enlaces.
     *
     * @return Implementación concreta de {@link EnlaceRepository}.
     */
    EnlaceRepository crearRepositorioEnlaces();

    /**
     * Crea y devuelve la instancia del componente de caché.
     *
     * @return Implementación concreta de {@link CachePort}.
     */
    CachePort crearCache();
}
