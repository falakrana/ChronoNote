# Versioned Notes API

A production-level RESTful API built with Spring Boot that automatically tracks and versions notes. This application allows users to create, read, update, and retrieve the history of notes with full versioning support.

## 🚀 Features

- **Full CRUD Operations**: Create, Read, Update, and List notes.
- **Automatic Versioning**: Every update to a note creates a new version entry in the history.
- **Persistent Storage**: Integrated with PostgreSQL for reliable data management.
- **Clean Architecture**: Follows the Controller-Service-Repository-Model pattern.

## 🛠️ Tech Stack

- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Tools**: Lombok, Maven

## ⚙️ Setup & Installation

### 1. Prerequisites
- JDK 21
- Maven 3.x
- PostgreSQL

### 2. Database Setup
Create a database named `note_db` in your PostgreSQL instance.

### 3. Configuration
1. Rename `src/main/resources/application.properties.example` to `application.properties`.
2. Update the database credentials:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 4. Run the Application
```bash
mvn spring-boot:run
```

## 📖 API Documentation

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/notes` | Create a new note |
| **GET** | `/api/notes` | Retrieve all notes |
| **GET** | `/api/notes/{id}` | Retrieve a specific note by ID |
| **PUT** | `/api/notes/{id}` | Update a note (creates a new version) |
| **GET** | `/api/notes/{id}/history` | Retrieve version history for a note |

## 🛡️ Security
Sensitive configurations are excluded from version control via `.gitignore`. Always use environment variables for production deployments.

---
*Built for learning and production-level note management.*
