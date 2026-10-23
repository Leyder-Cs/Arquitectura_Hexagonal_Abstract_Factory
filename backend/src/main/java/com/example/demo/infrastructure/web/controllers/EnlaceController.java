package com.example.demo.infrastructure.web.controllers;

import com.example.demo.application.service.EnlaceService;
import com.example.demo.domain.model.Enlace;
import com.example.demo.infrastructure.web.dto.EnlaceRequest;
import com.example.demo.infrastructure.web.dto.EnlaceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST: EnlaceController.
 *
 * Rol en la Arquitectura Hexagonal:
 * Es un Adaptador de Entrada Primario (Driving/Inbound Adapter).
 * Expone los endpoints HTTP bajo '/api/enlaces' para:
 * 1. POST /api/enlaces: Acortar una nueva URL e invalidar el caché.
 * 2. GET  /api/enlaces: Obtener el listado con estrategia Cache-Aside estricta (Redis / MongoDB).
 */
@RestController
@RequestMapping("/api/enlaces")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class EnlaceController {

    private final EnlaceService enlaceService;

    public EnlaceController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    /**
     * Endpoint para acortar una URL.
     * Valida que no esté vacía y cumpla con formato http/https.
     * Genera el código único, guarda en MongoDB e invalida la clave en Redis.
     */
    @PostMapping
    public ResponseEntity<EnlaceResponse> acortarEnlace(@Valid @RequestBody EnlaceRequest request) {
        Enlace enlaceGuardado = enlaceService.acortarEnlace(request.getEnlaceOriginal());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EnlaceResponse.fromDomain(enlaceGuardado));
    }

    /**
     * Endpoint para listar los enlaces.
     * Si está en Redis, lo retorna directamente (sin tocar el TTL).
     * Si no está en Redis (cache miss), consulta MongoDB y lo almacena en Redis con TTL de 30 segundos.
     */
    @GetMapping
    public ResponseEntity<List<EnlaceResponse>> listarEnlaces() {
        List<Enlace> enlaces = enlaceService.listarEnlaces();
        List<EnlaceResponse> respuesta = enlaces.stream()
                .map(EnlaceResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}
