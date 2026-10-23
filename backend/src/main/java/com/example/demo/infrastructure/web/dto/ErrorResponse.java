package com.example.demo.infrastructure.web.dto;

import java.time.Instant;

/**
 * DTO para Formato Estándar de Errores: ErrorResponse.
 *
 * Rol en la Arquitectura:
 * Proporciona un esquema consistente para respuestas de error (400, 404, 500)
 * hacia los clientes web y la interfaz de usuario.
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String mensaje;
    private Instant timestamp;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String error, String mensaje) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = Instant.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
