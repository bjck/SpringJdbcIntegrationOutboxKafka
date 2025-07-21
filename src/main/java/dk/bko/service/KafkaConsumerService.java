package dk.bko.service;

import dk.bko.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service for consuming messages from Kafka.
 * This demonstrates the consumer side of the outbox pattern.
 */
@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    /**
     * Consumes messages from the message-outbox topic.
     * This method is called automatically by Spring Kafka when a message is received.
     *
     * @param message the message payload
     * @param key the message key
     * @param partition the partition from which the message was received
     * @param topic the topic from which the message was received
     * @param acknowledgment the acknowledgment to manually acknowledge the message
     */
    @KafkaListener(topics = "${kafka.topic.message-outbox}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received message: id={}, content={}, region={}, timestamp={}",
                    message.getId(), message.getContent(), message.getRegion(), message.getTimestamp());
            
            logger.info("Message metadata: key={}, partition={}, topic={}", key, partition, topic);
            
            // Process the message (in a real application, this would do something meaningful)
            processMessage(message);
            
            // Acknowledge the message to mark it as processed
            acknowledgment.acknowledge();
            
            logger.info("Message processed and acknowledged: {}", message.getId());
        } catch (Exception e) {
            logger.error("Error processing message: {}", message.getId(), e);
            // Don't acknowledge the message, so it will be redelivered
        }
    }

    /**
     * Process the message.
     * In a real application, this would perform some business logic.
     *
     * @param message the message to process
     */
    private void processMessage(KafkaMessage message) {
        // Simulate processing time
        try {
            logger.info("Processing message: {}", message.getId());
            Thread.sleep(500); // Simulate some processing time
            logger.info("Message processing completed: {}", message.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Message processing interrupted", e);
        }
    }
}