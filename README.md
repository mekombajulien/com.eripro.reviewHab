# gestionDesAvis

API REST de gestion d'avis clients avec authentification JWT et gestion des rôles/permissions (RBAC), construite avec Spring Boot.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Lancer le projet](#lancer-le-projet)
- [Documentation API](#documentation-api)
- [Authentification](#authentification)
- [Rôles et permissions](#rôles-et-permissions)
- [Tests](#tests)
- [Structure du projet](#structure-du-projet)

## Fonctionnalités

- Inscription et activation de compte par email (code de validation)
- Connexion avec JWT (access token + refresh token)
- Réinitialisation de mot de passe
- Gestion des avis clients : création, modification, suppression, consultation (les siens ou tous, selon le rôle)
- Administration des utilisateurs (CRUD, activation/désactivation, attribution de rôle)
- Gestion des rôles et permissions (RBAC) : création de rôles, attribution/retrait de permissions
- Documentation interactive via Swagger / OpenAPI

## Stack technique

- Java 17
- Spring Boot 4.1.0 (Spring Web, Spring Security, Spring Data JPA, Spring Mail, Validation)
- MySQL
- JWT (jjwt)
- Lombok
- Springdoc OpenAPI (Swagger UI)
- JUnit 5, Mockito, MockMvc (tests)

## Prérequis

- JDK 17+
- Maven 3.9+
- MySQL (ou WAMP/XAMPP en local)
- Un compte [Mailtrap](https://mailtrap.io) (ou tout autre serveur SMTP) pour l'envoi des emails d'activation

## Installation

```bash
git clone <url-du-depot>
cd gestionDesAvis
```

## Configuration

Les identifiants sensibles (base de données, SMTP, secret JWT) ne doivent **jamais** être committés. Créez votre propre `src/main/resources/application.properties` à partir du modèle ci-dessous, ou surchargez ces valeurs via des variables d'environnement.

```properties
spring.application.name=gestionDesAvis

# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/gestionDesAvisUser?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Port et contexte
server.port=8083
server.servlet.context-path=/api

# SMTP (Mailtrap ou autre)
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=VOTRE_USERNAME
spring.mail.password=VOTRE_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# JWT
jwt.secret=VOTRE_SECRET_BASE64
jwt.expiration=1200000

admin.default-password=ChangeMoi123
logging.level.org.springframework.security=INFO
```

> ⚠️ Si des identifiants réels ont déjà été committés dans l'historique Git de ce projet, ajouter `application.properties` au `.gitignore` ne les efface pas rétroactivement : pensez à régénérer le secret JWT et le mot de passe SMTP concernés.

## Lancer le projet

```bash
mvn spring-boot:run
```

L'application démarre par défaut sur `http://localhost:8083/api`.

## Documentation API

Une fois l'application lancée, la documentation Swagger est disponible sur :

```
http://localhost:8083/api/swagger-ui.html
```

## Authentification

| Endpoint | Méthode | Description | Accès |
|---|---|---|---|
| `/inscription` | POST | Créer un compte | Public |
| `/activation` | POST | Activer un compte via un code reçu par email | Public |
| `/connection` | POST | Se connecter et obtenir un access token + refresh token | Public |
| `/refresh` | POST | Rafraîchir l'access token | Public |
| `/updatePassword` | POST | Demander une réinitialisation de mot de passe | Public |
| `/newPassWord` | POST | Définir un nouveau mot de passe | Public |
| `/deconnection` | POST | Se déconnecter (invalide le token) | Authentifié |

Toutes les autres routes attendent un header :

```
Authorization: Bearer <access_token>
```

## Rôles et permissions

Le contrôle d'accès repose sur des permissions nommées (ex. `AVIS_CREATE`, `USER_READ`, `ROLE_CREATE`) attribuées à des rôles, eux-mêmes attribués aux utilisateurs. Principales routes protégées :

| Ressource | Route | Permission requise |
|---|---|---|
| Avis | `POST /avis` | `AVIS_CREATE` |
| Avis | `PUT /avis/**` | `AVIS_UPDATE` |
| Avis | `GET /avis/mes` | `AVIS_READ_OWN` |
| Avis | `GET /avis` | `AVIS_READ_ALL` |
| Avis | `DELETE /avis/**` | `AVIS_DELETE` |
| Utilisateurs | `/admin/users/**` | `USER_CREATE` / `USER_READ` / `USER_UPDATE` / `USER_DELETE` |
| Rôles | `/admin/roles/**` | `ROLE_CREATE` / `ROLE_READ` / `ROLE_UPDATE` / `ROLE_DELETE` / `PERMISSION_ASSIGN` |

## Tests

```bash
mvn test
```

Le projet couvre :
- **Tests unitaires** des services (`service/impl`) avec JUnit 5 et Mockito
- **Tests de contrôleurs** (`controller`) avec `@WebMvcTest` et `MockMvc`
- **Tests de repository** (`repository`) avec `@DataJpaTest`

## Structure du projet

```
src/main/java/com/eriapro/gestionDesAvis
├── config/          # Configuration Swagger, initialisation de données
├── contoller/        # Contrôleurs REST
├── DTO/              # Objets de transfert (requêtes / réponses)
├── entite/            # Entités JPA
├── exception/         # Exceptions métier + gestionnaire global
├── mapper/            # Conversion entité <-> DTO
├── repository/        # Interfaces Spring Data JPA
├── securite/           # JWT, filtres, configuration Spring Security
└── service/           # Interfaces et implémentations métier
```
## author : codeur brute scofil