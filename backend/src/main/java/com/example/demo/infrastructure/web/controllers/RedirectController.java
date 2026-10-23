package com.example.demo.infrastructure.web.controllers;

import com.example.demo.application.service.EnlaceService;
import com.example.demo.domain.model.Enlace;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.Optional;

/**
 * Controlador de Redirección: RedirectController.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es un Adaptador de Entrada Web. Captura las peticiones dirigidas a la URL corta (/{codigo}),
 * consulta el enlace original en la base de datos a través del servicio de aplicación,
 * y responde con una redirección HTTP 302 (Found) hacia la URL original.
 * Si el código no existe en la base de datos, retorna un estado HTTP 404 (Not Found).
 */
@Controller
public class RedirectController {

    private final EnlaceService enlaceService;

    public RedirectController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    /**
     * Endpoint de Redirección HTTP 302.
     * Mapea códigos alfanuméricos para permitir que los enlaces acortados sean totalmente funcionales.
     *
     * @param codigo Código corto generado para el enlace.
     * @return Redirección HTTP 302 con cabecera Location hacia la URL destino, o 404 Not Found.
     */
    @GetMapping("/{codigo:[a-zA-Z0-9]{5,12}}")
    public ResponseEntity<Void> redirigir(@PathVariable("codigo") String codigo) {
        Optional<Enlace> enlaceOptional = enlaceService.buscarPorCodigo(codigo);

        if (enlaceOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Enlace enlace = enlaceOptional.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(enlace.getEnlaceOriginal()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
