package dk.bko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.bko.entity.Message;
import dk.bko.model.KafkaMessage;
import dk.bko.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service implementing the outbox pattern for reliable message delivery to Kafka.
 * Messages are first stored in the database and then sent to Kafka.
 * They are only removed from the database after successful processing.
 */
@Service
public class OutboxService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxService.class);

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.message-outbox}")
    private String messageOutboxTopic;

    @Autowired
    public OutboxService(MessageRepository messageRepository, 
                         KafkaTemplate<String, Object> kafkaTemplate,
                         ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Stores a message in the outbox and sends it to Kafka.
     * The message is first stored in the database to ensure it's not lost.
     *
     * @param content the message content
     * @param region the region for the message
     * @return the created message
     */
    @Transactional
    public Message sendMessage(String content, String region) {
        // Create a KafkaMessage
        KafkaMessage kafkaMessage = new KafkaMessage(content, region);
        
        // Store the message in the database
        Message message = storeMessage(kafkaMessage);
        
        // Send the message to Kafka
        sendToKafka(kafkaMessage, message.getMessageId());
        
        return message;
    }

    /**
     * Stores a message in the database.
     *
     * @param kafkaMessage the Kafka message to store
     * @return the stored message entity
     */
    private Message storeMessage(KafkaMessage kafkaMessage) {
        try {
            String messageId = kafkaMessage.getId();
            byte[] messageBytes = objectMapper.writeValueAsBytes(kafkaMessage);
            
            Message message = new Message(
                    messageId,
                    kafkaMessage.getRegion(),
                    kafkaMessage.getTimestamp(),
                    messageBytes
            );
            
            logger.info("Storing message in outbox: {}", messageId);
            return messageRepository.save(message);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize message", e);
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    /**
     * Sends a message to Kafka.
     * If the send fails, the message remains in the database and can be retried.
     *
     * @param kafkaMessage the Kafka message to send
     * @param messageId the ID of the message in the database
     */
    private void sendToKafka(KafkaMessage kafkaMessage, String messageId) {
        logger.info("Sending message to Kafka: {}", messageId);
        
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(messageOutboxTopic, messageId, kafkaMessage);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Message sent successfully to Kafka: {}", messageId);
                // Message was sent successfully, we can remove it from the database
                messageRepository.deleteById(messageId);
                logger.info("Message removed from outbox: {}", messageId);
            } else {
                logger.error("Failed to send message to Kafka: {}", messageId, ex);
                // Message will remain in the database and can be retried
            }
        });
    }
}