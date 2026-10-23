package com.example.demo.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entidad de Persistencia MongoDB: EnlaceDocument.
 *
 * Rol en la Arquitectura:
 * Pertenece a la capa de Infraestructura. Modela el documento tal como se almacena
 * en la colección 'enlaces' de MongoDB, aislando las anotaciones técnicas de Spring Data
 * fuera del núcleo de Dominio.
 */
@Document(collection = "enlaces")
public class EnlaceDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private String enlaceOriginal;
    private String enlaceAcortado;
    private Instant fechaCreacion;

    public EnlaceDocument() {
    }

    public EnlaceDocument(String id, String codigo, String enlaceOriginal, String enlaceAcortado, Instant fechaCreacion) {
        this.id = id;
        this.codigo = codigo;
        this.enlaceOriginal = enlaceOriginal;
        this.enlaceAcortado = enlaceAcortado;
        this.fechaCreacion = fechaCreacion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
