# 🚀 Acortador de Enlaces de Alto Rendimiento

Sistema completo y funcional de **Acortador de Enlaces** desarrollado con **Java 17+ (Spring Boot 3)** y **React (Vite)**, implementando **Arquitectura Hexagonal (Ports & Adapters)**, el patrón de diseño **Abstract Factory** para la persistencia, base de datos NoSQL **MongoDB**, motor de caché en memoria **Redis** con control nativo de TTL, y comunicación en tiempo real mediante **WebSocket**.

---

## 🏛️ 1. Arquitectura del Sistema

### 1.1. Arquitectura Hexagonal (Puertos y Adaptadores)
La aplicación aísla estrictamente la lógica de negocio del framework y los detalles técnicos:
- **Dominio (`com.example.demo.domain.model`)**: Contiene la entidad pura `Enlace`. Es un POJO sin anotaciones de Spring, Mongo o Redis.
- **Aplicación (`com.example.demo.application`)**:
  - `ports`: Interfaces `EnlaceRepository` (persistencia duradera) y `CachePort` (almacenamiento en caché y consulta de TTL).
  - `service`: `EnlaceService` contiene las reglas de negocio y orquesta los casos de uso. **Cumple con la regla de depender ÚNICAMENTE de los puertos, con CERO importaciones del paquete `infrastructure`**.
- **Infraestructura (`com.example.demo.infrastructure`)**:
  - `persistence`: Adaptadores concretos `EnlaceRepositoryMongo` (MongoDB) y `CacheRedis` (Redis), así como el contrato y la implementación de la Fábrica Abstracta.
  - `web/controllers`: Adaptadores HTTP de entrada `EnlaceController` (API REST) y `RedirectController` (redirección 302).
  - `websocket`: Adaptador en tiempo real `CacheTtlWebSocketHandler` y su configuración en `/ws/cache`.

```
                    +----------------------------------------------------+
                    |                INFRASTRUCTURE                      |
                    |                                                    |
[Cliente Web /] --->|  EnlaceController     RedirectController           |
[Navegador   ]      |        \                  /                        |
                    |         \                /                         |
                    |    +-----v--------------v------+                   |
                    |    |     APPLICATION LAYER     |                   |
                    |    |                           |                   |
                    |    |       EnlaceService       |                   |
                    |    |      /             \      |                   |
                    |    +-----v---------------v-----+                   |
                    |     (Port)              (Port)                     |
                    |  EnlaceRepository      CachePort                   |
                    |         ^                  ^                       |
                    |         |                  |                       |
                    |  EnlaceRepoMongo       CacheRedis                  |
                    |  (MongoDB: 27017)    (Redis: 6379)                 |
                    |         ^                  ^                       |
                    |          \                /                        |
                    |    MongoRedisPersistenceFactory                    |
                    |         (Abstract Factory)                         |
                    +----------------------------------------------------+
```

### 1.2. Patrón de Diseño Abstract Factory
Se define la interfaz:
```java
public interface PersistenceFactory {
    EnlaceRepository crearRepositorioEnlaces();
    CachePort crearCache();
}
```
Y su fábrica concreta `MongoRedisPersistenceFactory`, desacoplando la instanciación de los productos de almacenamiento concretos. En `PersistenceConfig`, Spring inyecta las instancias producidas por la fábrica hacia el servicio:
```java
@Bean
public EnlaceRepository enlaceRepository(PersistenceFactory factory) {
    return factory.crearRepositorioEnlaces();
}
@Bean
public CachePort cachePort(PersistenceFactory factory) {
    return factory.crearCache();
}
```

---

## 📂 2. Árbol Completo del Proyecto

```
PARCIAL 2/
├── docker-compose.yml
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── example/
│       │   │           └── demo/
│       │   │               ├── app/
│       │   │               │   └── App.java                             <-- @SpringBootApplication
│       │   │               ├── domain/
│       │   │               │   └── model/
│       │   │               │       └── Enlace.java                      <-- Entidad Pura de Dominio
│       │   │               ├── application/
│       │   │               │   ├── ports/
│       │   │               │   │   ├── EnlaceRepository.java            <-- Puerto de persistencia
│       │   │               │   │   └── CachePort.java                   <-- Puerto de caché y TTL
│       │   │               │   └── service/
│       │   │               │       └── EnlaceService.java               <-- Servicio (solo usa los puertos)
│       │   │               └── infrastructure/
│       │   │                   ├── config/
│       │   │                   │   └── WebConfig.java                   <-- Configuración CORS global
│       │   │                   ├── persistence/
│       │   │                   │   ├── PersistenceFactory.java          <-- Interfaz Abstract Factory
│       │   │                   │   ├── MongoRedisPersistenceFactory.java<-- Fábrica Concreta
│       │   │                   │   ├── EnlaceDocument.java              <-- Documento Mongo
│       │   │                   │   ├── EnlaceMongoSpringRepository.java <-- Spring Data Mongo Repo
│       │   │                   │   ├── EnlaceRepositoryMongo.java       <-- Adaptador Mongo
│       │   │                   │   ├── CacheRedis.java                  <-- Adaptador Redis (Cache-Aside)
│       │   │                   │   └── PersistenceConfig.java           <-- Inyección de Beans
│       │   │                   ├── web/
│       │   │                   │   ├── controllers/
│       │   │                   │   │   ├── EnlaceController.java        <-- POST y GET /api/enlaces
│       │   │                   │   │   └── RedirectController.java      <-- GET /{codigo} (Redirect 302)
│       │   │                   │   ├── dto/
│       │   │                   │   │   ├── EnlaceRequest.java
│       │   │                   │   │   ├── EnlaceResponse.java
│       │   │                   │   │   └── ErrorResponse.java
│       │   │                   │   └── exceptions/
│       │   │                   │       └── GlobalExceptionHandler.java  <-- Manejador global @RestControllerAdvice
│       │   │                   └── websocket/
│       │   │                       ├── CacheTtlWebSocketHandler.java    <-- Emisión del TTL de Redis cada 1s
│       │   │                       └── WebSocketConfig.java             <-- Endpoint /ws/cache
│       │   └── resources/
│       │       └── application.properties                               <-- Configuración de puertos, Mongo y Redis
│       └── test/
│           └── java/
│               └── com/
│                   └── example/
│                       └── demo/
│                           └── application/
│                               └── service/
│                                   └── EnlaceServiceTest.java           <-- Pruebas unitarias de puertos y caché
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.jsx
        ├── App.jsx                                                      <-- Componente raíz y WebSocket client
        ├── App.css                                                      <-- Estilos de navegación, tabla y badges
        ├── index.css                                                    <-- Variables y reset global
        └── components/
            ├── CountdownCache.jsx                                       <-- Cuenta regresiva conectada a Redis
            ├── AcortarForm.jsx                                          <-- Formulario de acortado y validación
            └── TablaEnlaces.jsx                                         <-- Tabla con enlaces funcionales <a>
```

---

## ⚡ 3. Reglas de Caché y WebSocket

1. **Lectura estricta sin resetear TTL**: En `CacheRedis.java`, al leer (`GET`) la lista en Redis, nunca se ejecuta ningún comando `SET` ni `EXPIRE`. El TTL de Redis corre libremente.
2. **Escritura únicamente en Cache Miss**: Solo cuando `redisTemplate.opsForValue().get(...)` es nulo, el servicio consulta MongoDB, guarda en Redis con `Duration.ofSeconds(30)` y retorna los datos.
3. **Invalidación Inmediata**: Al registrar un nuevo enlace mediante `POST /api/enlaces`, se borra la clave `enlaces:listado` en Redis (`redisTemplate.delete(...)`).
4. **WebSocket en tiempo real (`/ws/cache`)**:
   - Cada segundo lee el comando nativo `TTL` de Redis.
   - Si TTL > 0: envía `{"cacheado": true, "segundosRestantes": X}`.
   - Si TTL == -2 (clave inexistente/expirada) o TTL == -1: envía `{"cacheado": false}`.
   - **Primera consulta**: el frontend no muestra la cuenta regresiva.
   - **Segunda consulta**: al existir en caché, se muestra el banner: `⚡ Caché expira en: 27 s`.
   - **Recarga del navegador**: se recupera de inmediato el tiempo real directamente desde el servidor.
   - **Expiración**: al llegar a 0, el frontend solicita automáticamente el listado, forzando un nuevo Cache Miss contra MongoDB.

---

## 🛠️ 4. Pasos Exactos para Ejecutar el Proyecto

### Opción A: Levantar TODO el stack con Docker Compose (Recomendado)
Con un solo comando compilas y ejecutas los 4 servicios en contenedores (`MongoDB`, `Redis`, `Backend Spring Boot`, `Frontend React Nginx`):

```bash
docker compose up --build -d
```

- **Frontend Web**: Accede en tu navegador a: [http://localhost:5173](http://localhost:5173)
- **Backend API**: Disponible en: [http://localhost:8080](http://localhost:8080)
- **WebSocket TTL**: Disponible en: `ws://localhost:8080/ws/cache`

Para detener todos los contenedores:
```bash
docker compose down
```

---

### Opción B: Ejecución Híbrida / Desarrollo Local
Si deseas ejecutar MongoDB y Redis en Docker, pero compilar el código localmente:

1. **Levantar únicamente bases de datos**:
   ```bash
   docker compose up -d mongodb redis
   ```

2. **Iniciar el Backend (Spring Boot)**:
   ```powershell
   cd backend
   mvn spring-boot:run
   ```

3. **Iniciar el Frontend (React con Vite)**:
   ```powershell
   cd frontend
   npm install
   npm run dev
   ```
   Disponible en [http://localhost:5173](http://localhost:5173).

---

## 🧪 5. Guía de Prueba Manual Paso a Paso

Sigue esta secuencia para demostrar al docente cada una de las funcionalidades requeridas:

| Paso | Acción | Comportamiento Esperado | Verificación Técnica |
|---|---|---|---|
| **1. Crear Enlace** | En la sección **"Acortar enlace"**, ingresa `https://spring.io` y presiona **"Acortar"**. | Muestra la tarjeta con el enlace acortado `http://localhost:8080/XXXXXX` clickeable. | Se guarda en MongoDB y se invalida la clave `enlaces:listado` en Redis. |
| **2. Probar Redirección** | Haz clic en el enlace acortado generado. | El navegador abre una nueva pestaña y redirige (HTTP 302 Found) inmediatamente a `https://spring.io`. | `RedirectController` resolvió el código en MongoDB y emitió 302. |
| **3. Primera Consulta** | Haz clic en el botón superior **"Ver enlaces"**. | Se lista el enlace en la tabla. **NO aparece la cuenta regresiva**. | Ocurrió un **Cache Miss** en MongoDB. El backend guardó en Redis con TTL de 30s. |
| **4. Segunda Consulta** | Haz clic en el botón **"🔄 Actualizar listado"** (o presiona nuevamente "Ver enlaces"). | Se muestra el banner amarillo destacado: `⚡ Caché expira en: 27 s`. | Ocurrió un **Cache Hit** en Redis. El frontend ahora muestra el TTL real. |
| **5. Tercera Consulta** | Espera 4 segundos y haz clic nuevamente en **"🔄 Actualizar listado"**. | El contador marca aprox. `23 s` (continúa disminuyendo segundo a segundo). | **Demuestra que la lectura NO resetea el TTL a 30s**. |
| **6. Recarga del Navegador** | Presiona `F5` o Ctrl+R en el navegador para recargar la página y ve a "Ver enlaces". | El contador se reanuda en el segundo real del servidor (ej. `18 s`), **NO vuelve a 30s**. | El WebSocket envía el TTL actual de Redis al reconectarse. |
| **7. Expiración del Caché** | Deja que el contador llegue a `0 s`. | El banner desaparece y automáticamente la tabla refresca los datos. | Al expirar el TTL en Redis, el frontend pide el listado generando un nuevo Cache Miss. |
| **8. Invalidación al Acortar** | Vuelve a consultar el listado para cachear. Luego ve a "Acortar enlace" y acorta una nueva URL (`https://github.com`). Vuelve a "Ver enlaces". | La cuenta regresiva desaparece en la primera consulta del nuevo listado. | `cachePort.invalidarListado()` borró la clave de Redis tras el guardado. |

---

## 👨‍🏫 6. Resumen de Explicación para el Docente

1. **¿Por qué la capa `application` no importa nada de `infrastructure`?**
   Porque la Arquitectura Hexagonal dicta que las reglas de negocio no deben estar acopladas a frameworks o librerías externas. La capa de aplicación define interfaces (Puertos), y la infraestructura las implementa (Adaptadores).
2. **¿Cuál es el rol del patrón Abstract Factory en este proyecto?**
   La interfaz `PersistenceFactory` centraliza la creación de la familia de almacenamiento (`EnlaceRepository` y `CachePort`). La clase `MongoRedisPersistenceFactory` es la encargada de instanciar `EnlaceRepositoryMongo` y `CacheRedis`. Spring conecta el servicio `EnlaceService` recibiendo únicamente los puertos creados por la fábrica mediante métodos `@Bean`.
3. **¿Cómo se asegura que la cuenta regresiva es real y no simulada?**
   El componente React no tiene un timer local; se suscribe al WebSocket `/ws/cache`. En el backend, el handler invoca `redisTemplate.getExpire("enlaces:listado", TimeUnit.SECONDS)` cada segundo y transmite el valor exacto del comando TTL del motor de Redis.
