# URL Shortener API

A robust and scalable URL shortening service built with Spring Boot, featuring user authentication, analytics dashboard, and comprehensive URL management capabilities.

## 🚀 Features

- **URL Shortening**: Convert long URLs into short, manageable links
- **User Authentication**: Secure JWT-based authentication system
- **Analytics Dashboard**: Comprehensive statistics and insights
- **Expiration Management**: Set custom expiration dates for URLs
- **Usage Tracking**: Monitor click counts and performance metrics
- **RESTful API**: Clean and intuitive API design

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.6
- **Database**: MySQL with JPA/Hibernate
- **Security**: Spring Security with JWT authentication
- **Build Tool**: Gradle
- **Java Version**: 21
- **Dependencies**: Lombok, BCrypt, JJWT

## 📋 Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Gradle 7.0 or higher

## ⚙️ Configuration

1. Copy the example configuration file:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```

2. Configure your database and JWT settings in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   jwt.secret=your_jwt_secret_key
   ```

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/montemurro19/url-shortener.git
   cd url-shortener
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080/api`

## 📚 API Documentation

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### URL Shortening

#### Shorten URL
```http
POST /api/url/shorten
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "originalUrl": "https://www.example.com/very-long-url"
}
```

#### Redirect to Original URL
```http
GET /api/url/{shortCode}
```

### Dashboard & Analytics

#### Get Dashboard Summary
```http
GET /api/dashboard/summary
Authorization: Bearer {jwt_token}
```

#### Get User URLs
```http
GET /api/dashboard/urls
Authorization: Bearer {jwt_token}
```

#### Get Top Performing URLs
```http
GET /api/dashboard/urls/top?limit=10
Authorization: Bearer {jwt_token}
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/montes/url_shortener/
│   │   ├── application/          # Application services
│   │   │   ├── dashboard/        # Dashboard business logic
│   │   │   ├── url/             # URL shortening logic
│   │   │   └── user/            # User management logic
│   │   ├── domain/              # Domain models
│   │   │   ├── dashboard/       # Dashboard entities
│   │   │   ├── url/            # URL entities
│   │   │   └── user/           # User entities
│   │   ├── infrastructure/      # Infrastructure layer
│   │   │   ├── url/            # URL repositories
│   │   │   └── user/           # User repositories & utilities
│   │   ├── presentation/        # REST controllers
│   │   │   ├── dashboard/      # Dashboard endpoints
│   │   │   ├── url/           # URL endpoints
│   │   │   └── user/          # Authentication endpoints
│   │   └── config/            # Configuration classes
│   └── resources/
│       └── application.properties
└── test/                      # Test classes
```

## 🔧 Development

### Running Tests
```bash
./gradlew test
```

### Building for Production
```bash
./gradlew bootJar
```

## 📊 API Testing

A comprehensive Postman collection is available in the `documentation/` folder:
- Import `documentation/postman_collection.json` into Postman
- Set the `base_url` variable to your API endpoint
- Use the collection to test all endpoints

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**Matheus Montemurro**
- GitHub: [@montemurro19](https://github.com/montemurro19)
