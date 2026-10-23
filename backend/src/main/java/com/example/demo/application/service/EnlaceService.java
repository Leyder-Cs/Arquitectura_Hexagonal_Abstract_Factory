package com.example.demo.application.service;

import com.example.demo.application.ports.CachePort;
import com.example.demo.application.ports.EnlaceRepository;
import com.example.demo.domain.model.Enlace;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Aplicación: EnlaceService.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es el caso de uso central del sistema. Implementa las reglas de negocio
 * orquestando las llamadas a los puertos {@link EnlaceRepository} y {@link CachePort}.
 *
 * Cumplimiento estricto de la regla de dependencias:
 * NO importa ninguna clase del paquete 'infrastructure'. No depende de Spring,
 * ni de MongoDB ni de Redis; solo conoce sus puertos e interfaces.
 */
public class EnlaceService {

    private static final String CARACTERES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_CODIGO = 6;
    private static final long TTL_LISTADO_SEGUNDOS = 30L;

    private final EnlaceRepository enlaceRepository;
    private final CachePort cachePort;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();

    public EnlaceService(EnlaceRepository enlaceRepository, CachePort cachePort, String baseUrl) {
        this.enlaceRepository = enlaceRepository;
        this.cachePort = cachePort;
        this.baseUrl = (baseUrl != null && baseUrl.endsWith("/"))
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : (baseUrl != null ? baseUrl : "http://localhost:8080");
    }

    /**
     * Valida la URL original, genera un código corto único, persiste el nuevo enlace
     * e invalida inmediatamente la clave del listado en el caché de Redis.
     *
     * @param enlaceOriginal URL larga a ser acortada.
     * @return Entidad Enlace creada y persistida.
     */
    public Enlace acortarEnlace(String enlaceOriginal) {
        validarUrl(enlaceOriginal);

        String codigo = generarCodigoUnico();
        String enlaceAcortado = baseUrl + "/" + codigo;

        Enlace nuevoEnlace = new Enlace(
                null,
                codigo,
                enlaceOriginal.trim(),
                enlaceAcortado,
                Instant.now()
        );

        Enlace enlaceGuardado = enlaceRepository.guardar(nuevoEnlace);

        // Regla: Después de guardar, INVALIDA (borra) la clave del listado en Redis.
        cachePort.invalidarListado();

        return enlaceGuardado;
    }

    /**
     * Lista todos los enlaces implementando la estrategia Cache-Aside estricta:
     * - Si existe en Redis, se retorna directo sin tocar ni reiniciar el TTL.
     * - Si no existe (Cache Miss), se consulta MongoDB, se guarda en Redis con TTL de 30s y se retorna.
     *
     * @return Lista de enlaces.
     */
    public List<Enlace> listarEnlaces() {
        // 1. Verificar si la clave existe en el caché
        Optional<List<Enlace>> enlacesEnCache = cachePort.obtenerListado();
        if (enlacesEnCache.isPresent()) {
            return enlacesEnCache.get();
        }

        // 2. Cache Miss: consultar MongoDB
        List<Enlace> enlacesDesdeDb = enlaceRepository.obtenerTodos();

        // 3. Almacenar en Redis con TTL de 30 segundos (únicamente en Cache Miss)
        cachePort.guardarListado(enlacesDesdeDb, TTL_LISTADO_SEGUNDOS);

        return enlacesDesdeDb;
    }

    /**
     * Busca un enlace por su código alfanumérico para procesar la redirección.
     *
     * @param codigo Código de 6 a 8 caracteres alfanuméricos.
     * @return Optional con el enlace si existe.
     */
    public Optional<Enlace> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return enlaceRepository.buscarPorCodigo(codigo.trim());
    }

    /**
     * Consulta el tiempo de vida restante real (TTL) en Redis.
     * Utilizado para exponer el estado en el WebSocket.
     *
     * @return TTL en segundos, -2 si no existe, -1 si no expira.
     */
    public long obtenerTtlCache() {
        return cachePort.obtenerTtlListado();
    }

    /**
     * Valida que la URL no sea nula, no esté en blanco y tenga un formato web válido (http/https).
     */
    private void validarUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL original no puede estar vacía.");
        }
        String urlLimpia = url.trim();
        try {
            URI uri = URI.create(urlLimpia);
            String esquema = uri.getScheme();
            if (esquema == null || (!esquema.equalsIgnoreCase("http") && !esquema.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("La URL debe iniciar con el protocolo http:// o https://");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("La URL no contiene un host o dominio válido.");
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("El formato de la URL es inválido: " + e.getMessage());
        }
    }

    /**
     * Genera un código alfanumérico aleatorio de 6 caracteres y comprueba su unicidad en la base de datos.
     */
    private String generarCodigoUnico() {
        String codigo;
        int intentos = 0;
        do {
            StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                int indice = random.nextInt(CARACTERES.length());
                sb.append(CARACTERES.charAt(indice));
            }
            codigo = sb.toString();
            intentos++;
            if (intentos > 10) {
                // Si llegara a haber colisiones consecutivas, expandir longitud como salvaguarda
                codigo = codigo + CARACTERES.charAt(random.nextInt(CARACTERES.length()));
            }
        } while (enlaceRepository.existePorCodigo(codigo));

        return codigo;
    }
}
