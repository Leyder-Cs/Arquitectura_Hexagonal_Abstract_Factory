package com.example.demo.infrastructure.web.dto;

import com.example.demo.domain.model.Enlace;

/**
 * DTO de Respuesta de Enlace: EnlaceResponse.
 *
 * Rol en la Arquitectura:
 * Modela la respuesta JSON devuelta a los clientes de la API REST,
 * garantizando el contrato esperado: {id, enlaceOriginal, enlaceAcortado}.
 */
public class EnlaceResponse {

    private String id;
    private String enlaceOriginal;
    private String enlaceAcortado;

    public EnlaceResponse() {
    }

    public EnlaceResponse(String id, String enlaceOriginal, String enlaceAcortado) {
        this.id = id;
        this.enlaceOriginal = enlaceOriginal;
        this.enlaceAcortado = enlaceAcortado;
    }

    public static EnlaceResponse fromDomain(Enlace enlace) {
        return new EnlaceResponse(
                enlace.getId(),
                enlace.getEnlaceOriginal(),
                enlace.getEnlaceAcortado()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnlaceOriginal() {
        return enlaceOriginal;
    }

    public void setEnlaceOriginal(String enlaceOriginal) {
        this.enlaceOriginal = enlaceOriginal;
    }

    public String getEnlaceAcortado() {
        return enlaceAcortado;
    }

    public void setEnlaceAcortado(String enlaceAcortado) {
        this.enlaceAcortado = enlaceAcortado;
    }
}
