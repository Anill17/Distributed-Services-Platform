# Distributed Services Platform

A comprehensive distributed services platform showcasing microservices architecture using Java Spring Boot, Redis, Kafka, and Elasticsearch.

## 🏗️ Architecture Overview

This project demonstrates a microservices architecture with the following components:

### Microservices
1. **user-service** - Manages user data and authentication
2. **order-service** - Handles order processing and management
3. **product-service** - Manages product catalog and inventory

### Technology Stack
- **Java Spring Boot** - Framework for building microservices
- **Redis** - Caching and session management
- **Apache Kafka** - Event-driven communication between services
- **Elasticsearch** - Search and analytics engine
- **Spring Data JPA** - Database persistence
- **Spring Data Elasticsearch** - Elasticsearch integration
- **Spring Data Redis** - Redis integration
- **Spring Kafka** - Kafka integration

## 📁 Project Structure

```
distributed-services-platform/
├── user-service/
│   ├── src/main/java/com/distributedservices/userservice/
│   │   ├── UserServiceApplication.java
│   │   ├── model/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── UserElasticsearchRepository.java
│   │   └── config/
│   │       ├── RedisConfig.java
│   │       ├── KafkaConfig.java
│   │       └── ElasticsearchConfig.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── order-service/
│   ├── src/main/java/com/distributedservices/orderservice/
│   │   ├── OrderServiceApplication.java
│   │   ├── model/
│   │   │   └── Order.java
│   │   ├── repository/
│   │   │   ├── OrderRepository.java
│   │   │   └── OrderElasticsearchRepository.java
│   │   └── config/
│   │       ├── RedisConfig.java
│   │       ├── KafkaConfig.java
│   │       └── ElasticsearchConfig.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── product-service/
│   ├── src/main/java/com/distributedservices/productservice/
│   │   ├── ProductServiceApplication.java
│   │   ├── model/
│   │   │   └── Product.java
│   │   ├── repository/
│   │   │   ├── ProductRepository.java
│   │   │   └── ProductElasticsearchRepository.java
│   │   └── config/
│   │       ├── RedisConfig.java
│   │       ├── KafkaConfig.java
│   │       └── ElasticsearchConfig.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
└── README.md
```

## 🚀 Prerequisites

Before you begin, ensure you have the following installed:

1. **Java Development Kit (JDK)** - Version 17 or higher
   ```bash
   java -version
   ```

2. **Maven** - Version 3.6 or higher
   ```bash
   mvn -version
   ```

3. **Docker and Docker Compose** (Recommended for local development)
   ```bash
   docker --version
   docker-compose --version
   ```

4. **Redis** - Version 7.0 or higher
   - Option 1: Using Docker
     ```bash
     docker run -d -p 6379:6379 redis:7-alpine
     ```
   - Option 2: Install locally
     - macOS: `brew install redis`
     - Linux: `sudo apt-get install redis-server`
     - Windows: Download from [redis.io](https://redis.io/download)

5. **Apache Kafka** - Version 3.5 or higher
   - Option 1: Using Docker
     ```bash
     docker run -d -p 9092:9092 apache/kafka:3.5
     ```
   - Option 2: Download from [kafka.apache.org](https://kafka.apache.org/downloads)

6. **Elasticsearch** - Version 8.0 or higher
   - Option 1: Using Docker
     ```bash
     docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.11.0
     ```
   - Option 2: Download from [elastic.co](https://www.elastic.co/downloads/elasticsearch)

## 📦 Quick Start with Docker Compose

The easiest way to get all infrastructure components running is using Docker Compose:

1. Create a `docker-compose.yml` file in the project root:
```yaml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es_data:/usr/share/elasticsearch/data

volumes:
  es_data:
```

2. Start all services:
```bash
docker-compose up -d
```

3. Verify services are running:
```bash
# Check Redis
redis-cli ping  # Should return PONG

# Check Kafka (if you have kafka tools installed)
kafka-topics --list --bootstrap-server localhost:9092

# Check Elasticsearch
curl http://localhost:9200
```

## 🔧 Manual Setup

### Setting up Redis

1. **Start Redis server:**
   ```bash
   redis-server
   ```

2. **Test connection:**
   ```bash
   redis-cli ping
   ```

3. **Default configuration:**
   - Host: `localhost`
   - Port: `6379`
   - No password (for development)

### Setting up Kafka

1. **Download and extract Kafka:**
   ```bash
   wget https://downloads.apache.org/kafka/3.5.0/kafka_2.13-3.5.0.tgz
   tar -xzf kafka_2.13-3.5.0.tgz
   cd kafka_2.13-3.5.0
   ```

2. **Start Zookeeper:**
   ```bash
   bin/zookeeper-server-start.sh config/zookeeper.properties
   ```

3. **Start Kafka broker:**
   ```bash
   bin/kafka-server-start.sh config/server.properties
   ```

4. **Create topics (optional, will be auto-created):**
   ```bash
   bin/kafka-topics.sh --create --topic user-events --bootstrap-server localhost:9092
   bin/kafka-topics.sh --create --topic order-events --bootstrap-server localhost:9092
   bin/kafka-topics.sh --create --topic product-events --bootstrap-server localhost:9092
   ```

### Setting up Elasticsearch

1. **Download and extract Elasticsearch:**
   ```bash
   wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.11.0-linux-x86_64.tar.gz
   tar -xzf elasticsearch-8.11.0-linux-x86_64.tar.gz
   cd elasticsearch-8.11.0
   ```

2. **Start Elasticsearch:**
   ```bash
   ./bin/elasticsearch
   ```

3. **Verify it's running:**
   ```bash
   curl http://localhost:9200
   ```

4. **For development, disable security (add to config/elasticsearch.yml):**
   ```yaml
   xpack.security.enabled: false
   ```

## 🏃 Running the Microservices

### Build all services:
```bash
# Build user-service
cd user-service
mvn clean install

# Build order-service
cd ../order-service
mvn clean install

# Build product-service
cd ../product-service
mvn clean install
```

### Run each service:

**Terminal 1 - User Service:**
```bash
cd user-service
mvn spring-boot:run
```
Service will run on: `http://localhost:8081`

**Terminal 2 - Order Service:**
```bash
cd order-service
mvn spring-boot:run
```
Service will run on: `http://localhost:8082`

**Terminal 3 - Product Service:**
```bash
cd product-service
mvn spring-boot:run
```
Service will run on: `http://localhost:8083`

## 📝 Configuration

### Application Properties

Each service has its own `application.yml` file. Key configurations:

- **Server Port**: Each service runs on a different port (8081, 8082, 8083)
- **Database**: Configure your database connection (H2 for development, PostgreSQL/MySQL for production)
- **Redis**: Connection to Redis instance
- **Kafka**: Kafka broker configuration
- **Elasticsearch**: Elasticsearch cluster configuration

### Environment Variables

You can override configurations using environment variables:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export ELASTICSEARCH_HOST=localhost
export ELASTICSEARCH_PORT=9200
```

## 🎯 Next Steps for Implementation

Now that the basic structure is in place, here's what you can implement:

### 1. Service Layer
- Create service interfaces and implementations
- Implement business logic
- Add validation and error handling

### 2. Controller Layer
- Create REST controllers
- Implement CRUD operations
- Add request/response DTOs
- Implement API documentation (Swagger/OpenAPI)

### 3. Kafka Integration
- Create Kafka producers for publishing events
- Create Kafka consumers for consuming events
- Implement event-driven communication between services

### 4. Redis Integration
- Implement caching strategies
- Cache frequently accessed data
- Implement session management (if needed)

### 5. Elasticsearch Integration
- Implement search functionality
- Create custom queries
- Implement full-text search

### 6. Database Setup
- Configure database connection
- Create database schema
- Run migrations (Flyway/Liquibase)

### 7. Testing
- Unit tests for services
- Integration tests
- Test Kafka producers/consumers
- Test Redis caching

### 8. API Gateway (Optional)
- Implement Spring Cloud Gateway
- Add routing and load balancing
- Implement authentication/authorization

## 🔍 Key Concepts to Implement

### Event-Driven Architecture
- **User Service** publishes events when users are created/updated
- **Order Service** consumes user events and publishes order events
- **Product Service** consumes order events to update inventory

### Caching Strategy
- Cache user data in Redis
- Cache product catalog
- Implement cache invalidation on updates

### Search Functionality
- Index users in Elasticsearch for search
- Index products for full-text search
- Implement advanced search queries

## 🐛 Troubleshooting

### Redis Connection Issues
- Ensure Redis is running: `redis-cli ping`
- Check firewall settings
- Verify connection details in `application.yml`

### Kafka Connection Issues
- Ensure Zookeeper and Kafka are running
- Check Kafka logs for errors
- Verify broker address in configuration

### Elasticsearch Connection Issues
- Ensure Elasticsearch is running: `curl http://localhost:9200`
- Check cluster health: `curl http://localhost:9200/_cluster/health`
- Verify security settings (disabled for development)

### Port Conflicts
- If ports 8081, 8082, 8083 are in use, change them in `application.yml`
- Ensure no other services are using Redis (6379), Kafka (9092), or Elasticsearch (9200) ports

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch)
- [Redis Documentation](https://redis.io/documentation)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)

## 🤝 Contributing

This is a demo project for learning purposes. Feel free to extend it with:
- Additional microservices
- More complex business logic
- Advanced caching strategies
- Complex search queries
- Monitoring and logging (Prometheus, Grafana)
- Distributed tracing (Zipkin, Jaeger)

## 📄 License

This project is for educational purposes.

---

Happy Coding! 🚀
