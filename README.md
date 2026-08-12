# MenuJPA — Sistema de Gestión de Menú

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=thymeleaf&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=flat&logo=junit5&logoColor=white" />
  <img src="https://img.shields.io/badge/Mockito-C5D9C8?style=flat" />
  <img src="https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=flat&logo=swagger&logoColor=black" />
</p>

Sistema web para la gestión gastronómica de un restaurante. Permite administrar menús, recetas, alimentos, chefs, meseros, gerentes, clientes, pedidos y despensa a través de una interfaz web.

![Diagrama UML](docs/DiagramaMenu.jpeg)

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.2.5 · Spring Data JPA · Hibernate |
| Base de datos | MySQL 8 |
| Documentación API | springdoc-openapi (Swagger UI) |
| Frontend | Thymeleaf · Vanilla CSS · Vanilla JS |
| Testing | JUnit 5 · Mockito |
| Build | Maven |

---

## Módulos

### Gestión
- **Menús** — Creación y gestión de menús con asignación de gerente y recetas
- **Recetas** — Recetas con dificultad, tiempo de preparación y lista de alimentos
- **Alimentos** — Catálogo con tipos: Plato Fuerte, Bebida, Postre, Adicional

### Personal
- **Chefs** — Personal de cocina con especialidad, experiencia y horarios
- **Gerentes** — Responsables de menús y despensa

### Operaciones
- **Clientes** — Registro de clientes con usuario y contraseña
- **Ingredientes** — Stock de ingredientes con descripción y cantidad

---

## Arquitectura

```
MenuJpaApplication
├── controllers/    BaseControllerImpl<E, S>  → endpoints REST
├── services/       BaseServiceImpl<E>        → lógica de negocio
├── repositories/   BaseRepository<E>        → Spring Data JPA
└── entities/       jerarquía de clases del dominio
```

### Jerarquía de entidades

```
Base
└── Persona
    ├── Empleado (abstracto)
    │   ├── Chef
    ├── Gerente
    └── Cliente

Base
├── Alimento (SINGLE_TABLE)
│   ├── PlatoFuerte
│   ├── Bebida
│   ├── Postre
│   └── Adicional
├── Menu
├── Receta
└── Ingrediente
```

---

## Requisitos

- Java 17+
- MySQL 8 corriendo en `localhost:3306`
- Maven 3.8+

---

## Configuración

**1. Crear la base de datos:**

```sql
CREATE DATABASE menujpa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**2. Editar credenciales** en `src/main/resources/application.properties`:

```properties
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

---

## Ejecución

```bash
# Compilar y correr con hot-reload
./mvnw spring-boot:run

# Build completo
./mvnw package -DskipTests
```

La aplicación queda disponible en `http://localhost:8080`.

La documentación interactiva de la API (Swagger UI) queda disponible en `http://localhost:8080/swagger-ui/index.html`.

---

## Endpoints REST

| Entidad | Base URL |
|---|---|
| Menús | `/api/v1/menus` |
| Recetas | `/api/v1/recetas` |
| Alimentos | `/api/v1/alimentos` |
| Chefs | `/api/v1/chefs` |
| Gerentes | `/api/v1/gerentes` |
| Clientes | `/api/v1/clientes` |
| Ingredientes | `/api/v1/ingredientes` |

Todos los endpoints soportan: `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`.


## Endpoints adicionales


POST   /api/v1/menus/{menuId}/recetas/{recetaId}   → agregar receta al menú

DELETE /api/v1/menus/{menuId}/recetas/{recetaId}   → quitar receta del menú



---

## Tests

Cobertura unitaria de la capa de `services` (`MenuServiceImpl`, `RecetaServiceImpl`) con JUnit 5 + Mockito. Los repositorios se mockean, así que la suite no depende de una base de datos real ni de que la app esté corriendo.

```bash
# Correr toda la suite
./mvnw test

# Correr una clase de test puntual
./mvnw test -Dtest=MenuServiceImplTest
```

---

## Estructura del proyecto

```
menujpa-spring/
├── src/
│   ├── main/
│   │   ├── java/com/menujpa/
│   │   │   ├── MenuJpaApplication.java
│   │   │   ├── config/        OpenApiConfig.java
│   │   │   ├── controllers/
│   │   │   ├── services/
│   │   │   ├── repositories/
│   │   │   └── entities/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/style.css
│   │       │   └── js/api.js
│   │       └── templates/
│   │           ├── index.html
│   │           ├── menus.html
│   │           ├── recetas.html
│   │           ├── alimentos.html
│   │           ├── chefs.html
│   │           └── gerentes.html
│   └── test/java/com/menujpa/
│       ├── SmokeTest.java
│       └── services/
│           ├── MenuServiceImplTest.java
│           └── RecetaServiceImplTest.java
├── docs/
│   └── DiagramaMenu.jpeg
├── mvnw / mvnw.cmd
└── pom.xml
```

---

## Validaciones

El sistema aplica validaciones en dos capas:

- **Backend (Jakarta Validation):** `@NotBlank`, `@Size`, `@Email`, `@DecimalMin`, `@Past`, `@Pattern` en las entidades. Los errores se devuelven como JSON con mensaje descriptivo.
- **Frontend (JavaScript):** validaciones antes de enviar cada formulario con mensajes toast.

---

## Notas

- Las tablas se crean/actualizan automáticamente con `spring.jpa.hibernate.ddl-auto=update`.
- La herencia de `Alimento` usa `SINGLE_TABLE` con discriminador `tipo_alimento`.
- `Empleado` es una clase abstracta (`@MappedSuperclass`) que centraliza los campos comunes de `Chef`.

---

## Autor

    Martin Emanuel Zamora
    Instituto Tecnologico Universitario (ITU) — 2026
