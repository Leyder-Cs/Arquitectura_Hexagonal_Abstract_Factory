package com.example.demo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Clase Principal de Arranque: App.
 *
 * Rol en la Arquitectura:
 * Punto de entrada de la aplicación Spring Boot.
 * Inicializa el contexto de la aplicación, el servidor embebido (Tomcat),
 * y configura el escaneo de componentes en todo el paquete base 'com.example.demo'
 * así como los repositorios de Spring Data MongoDB.
 */
@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableMongoRepositories(basePackages = "com.example.demo.infrastructure.persistence")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
