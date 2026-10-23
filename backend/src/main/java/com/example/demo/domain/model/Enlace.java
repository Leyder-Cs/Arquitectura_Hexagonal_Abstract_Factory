package com.example.demo.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidad de Dominio: Enlace.
 *
 * Rol en la Arquitectura Hexagonal:
 * Representa el núcleo del modelo de negocio (Domain Layer).
 * Es una clase pura de Java (POJO), completamente agnóstica de frameworks,
 * de bases de datos (MongoDB) o de caché (Redis).
 */
public class Enlace {

    private String id;
    private String codigo;
    private String enlaceOriginal;
    private String enlaceAcortado;
    private Instant fechaCreacion;

    public Enlace() {
    }

    public Enlace(String id, String codigo, String enlaceOriginal, String enlaceAcortado, Instant fechaCreacion) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enlace enlace = (Enlace) o;
        return Objects.equals(id, enlace.id) && Objects.equals(codigo, enlace.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigo);
    }

    @Override
    public String toString() {
        return "Enlace{" +
                "id='" + id + '\'' +
                ", codigo='" + codigo + '\'' +
                ", enlaceOriginal='" + enlaceOriginal + '\'' +
                ", enlaceAcortado='" + enlaceAcortado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
