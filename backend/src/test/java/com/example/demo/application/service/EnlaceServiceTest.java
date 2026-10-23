package com.example.demo.application.service;

import com.example.demo.application.ports.CachePort;
import com.example.demo.application.ports.EnlaceRepository;
import com.example.demo.domain.model.Enlace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias del Servicio de Aplicación: EnlaceServiceTest.
 *
 * Valida la lógica de negocio pura y la orquestación con los puertos
 * EnlaceRepository y CachePort mediante dobles de prueba (Mocks).
 */
class EnlaceServiceTest {

    private EnlaceRepository enlaceRepository;
    private CachePort cachePort;
    private EnlaceService enlaceService;

    @BeforeEach
    void setUp() {
        enlaceRepository = mock(EnlaceRepository.class);
        cachePort = mock(CachePort.class);
        enlaceService = new EnlaceService(enlaceRepository, cachePort, "http://localhost:8080");
    }

    @Test
    @DisplayName("Debe acortar URL válida, guardar en repositorio e INVALIDAR el caché en Redis")
    void testAcortarEnlaceExitoso() {
        String urlOriginal = "https://spring.io/projects/spring-boot";

        when(enlaceRepository.existePorCodigo(anyString())).thenReturn(false);
        when(enlaceRepository.guardar(any(Enlace.class))).thenAnswer(invocation -> {
            Enlace e = invocation.getArgument(0);
            e.setId("mongo-id-123");
            return e;
        });

        Enlace resultado = enlaceService.acortarEnlace(urlOriginal);

        assertNotNull(resultado);
        assertEquals("mongo-id-123", resultado.getId());
        assertEquals(urlOriginal, resultado.getEnlaceOriginal());
        assertTrue(resultado.getEnlaceAcortado().startsWith("http://localhost:8080/"));
        assertEquals(6, resultado.getCodigo().length());

        // Verificar que guardó en el repositorio
        verify(enlaceRepository, times(1)).guardar(any(Enlace.class));
        // Verificar requisito fundamental: INVALIDA (borra) la clave del listado en Redis
        verify(cachePort, times(1)).invalidarListado();
    }

    @Test
    @DisplayName("Debe fallar al intentar acortar una URL vacía o sin protocolo válido")
    void testAcortarUrlInvalida() {
        assertThrows(IllegalArgumentException.class, () -> enlaceService.acortarEnlace(""));
        assertThrows(IllegalArgumentException.class, () -> enlaceService.acortarEnlace("   "));
        assertThrows(IllegalArgumentException.class, () -> enlaceService.acortarEnlace("ftp://mi-servidor.com"));
        assertThrows(IllegalArgumentException.class, () -> enlaceService.acortarEnlace("texto_sin_url"));

        // Asegurar que no se tocó el repositorio ni el caché ante error
        verifyNoInteractions(enlaceRepository);
        verifyNoInteractions(cachePort);
    }

    @Test
    @DisplayName("Cache Hit: Retorna desde Redis sin consultar MongoDB ni reiniciar TTL")
    void testListarEnlacesCacheHit() {
        List<Enlace> listaCacheada = List.of(
                new Enlace("1", "abc123", "https://google.com", "http://localhost:8080/abc123", Instant.now())
        );

        when(cachePort.obtenerListado()).thenReturn(Optional.of(listaCacheada));

        List<Enlace> resultado = enlaceService.listarEnlaces();

        assertEquals(1, resultado.size());
        assertEquals("abc123", resultado.get(0).getCodigo());

        // Debe leer del caché
        verify(cachePort, times(1)).obtenerListado();
        // NO debe consultar la base de datos
        verify(enlaceRepository, never()).obtenerTodos();
        // NO debe sobreescribir ni tocar el TTL
        verify(cachePort, never()).guardarListado(any(), anyLong());
    }

    @Test
    @DisplayName("Cache Miss: Consulta MongoDB y almacena en Redis con TTL de 30 segundos")
    void testListarEnlacesCacheMiss() {
        when(cachePort.obtenerListado()).thenReturn(Optional.empty());

        List<Enlace> listaDesdeMongo = List.of(
                new Enlace("2", "xyz987", "https://github.com", "http://localhost:8080/xyz987", Instant.now())
        );
        when(enlaceRepository.obtenerTodos()).thenReturn(listaDesdeMongo);

        List<Enlace> resultado = enlaceService.listarEnlaces();

        assertEquals(1, resultado.size());
        assertEquals("xyz987", resultado.get(0).getCodigo());

        // Debe intentar leer del caché
        verify(cachePort, times(1)).obtenerListado();
        // Al fallar el caché, consulta MongoDB
        verify(enlaceRepository, times(1)).obtenerTodos();
        // Almacena en Redis con TTL de 30 segundos
        verify(cachePort, times(1)).guardarListado(listaDesdeMongo, 30L);
    }

    @Test
    @DisplayName("Buscar por código delega en el repositorio")
    void testBuscarPorCodigo() {
        Enlace enlace = new Enlace("3", "code99", "https://react.dev", "http://localhost:8080/code99", Instant.now());
        when(enlaceRepository.buscarPorCodigo("code99")).thenReturn(Optional.of(enlace));

        Optional<Enlace> resultado = enlaceService.buscarPorCodigo("code99");

        assertTrue(resultado.isPresent());
        assertEquals("https://react.dev", resultado.get().getEnlaceOriginal());
        verify(enlaceRepository, times(1)).buscarPorCodigo("code99");
    }
}
