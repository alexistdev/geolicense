# Geolicense v1.0

Geolicense is a Java Springboot and Vue Js based software, which is used to manage online licenses for your applications or digital works.

## Frontend

### Specifications
- Built with Vue 3.5.32

### Installation Instructions
1. Open your terminal.
2. Run the following command to install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. For testing purposes, the following credentials can be used. Please ensure the backend is running before attempting to login:
   ```bash
   Role User:
   Username: alexistdev@gmail.com
   Password: 1234

   Role Admin:
   Username: -
   Password: -
   ```

## Backend

### Specifications
- OpenJDK 21
- Spring Boot 3
- MySQL Database
- Redis

### Installation Instructions
1. Open your terminal.
2. Run the following command to install dependencies:
   ```bash
   mvn install
   ```
3. Create an empty MySQL database named `geolicense`.
4. Edit the `application.properties` file to configure your database credentials:
   ```properties
   spring.datasource.username=[your_database_username]
   spring.datasource.password=[your_database_password]
   ```
5. Create Run Redis and edit the `application.properties` file to configure your redis credentials:
   ```properties
   spring.data.redis.host=localhost
   spring.data.redis.port=[your_redis_port_configuration]
   spring.data.redis.password=[your_redis_password]
   ```
6. To Avoid CORS please edit the `application.properties` , adjust with url from the frontend:
   ```properties
   cors.allowed-origins=http://localhost:5173
   ```
7. Start the backend server:
   ```bash
   mvn spring-boot:run
   ```
8. Set up Authentication using Cookie
   ```Headers
   Key: Cookie
   Value: SID=[SessionToken-From-Login]
   ```

With these steps, you'll have the Geolicense application up and running, ready for customization and use.

## Screenshots

#### Administrator Page:


-- test trigger jenkins --
