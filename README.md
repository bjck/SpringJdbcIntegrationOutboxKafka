# JDBC Message Store with Kafka Outbox Pattern

This project demonstrates the implementation of the outbox pattern using Spring JDBC message store and Kafka. The outbox pattern is a reliable way to ensure that messages are delivered to Kafka even if there are failures in the process.

## Architecture

The project implements the following components:

1. **JDBC Message Store**: Uses Spring Integration's JDBC message store to store messages in a PostgreSQL database.
2. **Outbox Service**: Implements the outbox pattern by first storing messages in the database and then sending them to Kafka.
3. **Kafka Integration**: Configures Kafka producers and consumers for sending and receiving messages.
4. **REST API**: Provides endpoints for creating messages and sending them through the outbox pattern.

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven

## Getting Started

### 1. Start the Services

Start the PostgreSQL database, Kafka, and Zookeeper using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Zookeeper on port 2181
- Kafka on ports 9092 (internal) and 29092 (external)
- Kafka UI on port 8090

### 2. Build and Run the Application

Build and run the Spring Boot application:

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on port 8080.

### 3. Access the Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui
```

This will show the API documentation and allow you to test the endpoints.

## Testing the Outbox Pattern

### 1. Send a Message Through the Outbox Pattern

Use the following endpoint to send a message through the outbox pattern:

```
POST /api/messages/outbox
```

Request body:
```json
{
  "content": "Hello, Kafka!",
  "region": "test-region"
}
```

This will:
1. Store the message in the database
2. Send the message to Kafka
3. Remove the message from the database after successful delivery

### 2. Verify Message Processing

The application logs will show:
1. The message being stored in the database
2. The message being sent to Kafka
3. The consumer receiving and processing the message
4. The message being removed from the database after successful processing

You can also check the Kafka UI at http://localhost:8090 to see the messages in the `message-outbox` topic.

## API Endpoints

### Regular Message Operations

- `POST /api/messages`: Create a new message (stored only in the database)
- `GET /api/messages`: Get all messages
- `GET /api/messages/{messageId}`: Get a message by ID
- `GET /api/messages/{messageId}/content`: Get the content of a message
- `GET /api/messages/region/{region}`: Get messages by region
- `GET /api/messages/after?date={date}`: Get messages created after a specific date
- `DELETE /api/messages/{messageId}`: Delete a message

### Outbox Pattern

- `POST /api/messages/outbox`: Send a message through the outbox pattern

## Implementation Details

### Database Schema

The project uses the following tables:
- `INT_MESSAGE`: Stores the actual message content
- `INT_GROUP_MESSAGE`: Maps messages to message groups
- `INT_MESSAGE_GROUP`: Stores information about message groups
- `INT_LOCK`: Provides distributed locking mechanism

### Outbox Pattern Flow

1. A message is received via the REST API
2. The message is stored in the database within a transaction
3. After the transaction is committed, the message is sent to Kafka
4. If the send is successful, the message is removed from the database
5. If the send fails, the message remains in the database and can be retried

### Kafka Consumer

The Kafka consumer:
1. Listens for messages on the `message-outbox` topic
2. Processes the received messages
3. Manually acknowledges messages after successful processing
4. Handles errors by not acknowledging messages, so they will be redelivered

## Troubleshooting

### Kafka Connection Issues

If the application cannot connect to Kafka, make sure:
1. The Kafka service is running (`docker-compose ps`)
2. The bootstrap server is correctly configured in `application.properties`
3. The topic exists (it should be auto-created, but you can check in the Kafka UI)

### Database Issues

If there are database issues:
1. Check that PostgreSQL is running (`docker-compose ps`)
2. Verify the database connection properties in `application.properties`
3. Check the Liquibase migration logs for any errors

## License

This project is licensed under the MIT License - see the LICENSE file for details.