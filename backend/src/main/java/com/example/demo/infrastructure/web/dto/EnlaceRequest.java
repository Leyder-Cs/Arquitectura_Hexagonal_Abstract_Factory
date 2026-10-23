package com.example.demo.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de Petición para Acortar Enlace: EnlaceRequest.
 *
 * Rol en la Arquitectura:
 * Objeto de transferencia de datos de entrada en el adaptador Web primario.
 * Acepta 'enlaceOriginal' o 'urlOriginal' y valida las restricciones técnicas
 * antes de pasarlo a la capa de aplicación.
 */
public class EnlaceRequest {

    @NotBlank(message = "La URL no puede estar vacía.")
    @Pattern(
            regexp = "^(https?://).+",
            message = "La URL debe comenzar con http:// o https:// y ser válida."
    )
    @JsonAlias({"urlOriginal", "url", "enlace"})
    private String enlaceOriginal;

    public EnlaceRequest() {
    }

    public EnlaceRequest(String enlaceOriginal) {
        this.enlaceOriginal = enlaceOriginal;
    }

    public String getEnlaceOriginal() {
        return enlaceOriginal;
    }

    public void setEnlaceOriginal(String enlaceOriginal) {
        this.enlaceOriginal = enlaceOriginal;
    }
}
