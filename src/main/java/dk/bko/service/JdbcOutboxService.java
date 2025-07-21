package dk.bko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.bko.entity.Message;
import dk.bko.model.KafkaMessage;
import dk.bko.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.PollableChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementing the outbox pattern for reliable message delivery to Kafka.
 * Messages are stored in the JDBC message store using Spring Integration's PollableChannel
 * and later sent to Kafka by a scheduler.
 * This ensures that messages are not lost even if the application crashes.
 */
@Service
public class JdbcOutboxService {

    private static final Logger logger = LoggerFactory.getLogger(JdbcOutboxService.class);
    private static final String REGION_HEADER = "region";
    private static final String MESSAGE_ID_HEADER = "messageId";

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final PollableChannel kafkaOutboxChannel;

    @Autowired
    public JdbcOutboxService(
            MessageRepository messageRepository,
            ObjectMapper objectMapper,
            @Qualifier("kafkaOutboxChannel") PollableChannel kafkaOutboxChannel) {
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        this.kafkaOutboxChannel = kafkaOutboxChannel;
    }

    /**
     * Stores a message in the outbox for later delivery to Kafka.
     * The message is stored in the JDBC message store using Spring Integration's PollableChannel.
     * It will be sent to Kafka by a scheduler.
     *
     * @param content the message content
     * @param region the region for the message
     * @return the created message entity
     */
    @Transactional
    public Message sendMessage(String content, String region) {
        // Create a KafkaMessage
        KafkaMessage kafkaMessage = new KafkaMessage(content, region);
        String messageId = kafkaMessage.getId();
        
        try {
            // Serialize the message to JSON
            byte[] messageBytes = objectMapper.writeValueAsBytes(kafkaMessage);
            
            // Store the message in the database using JPA
            Message message = new Message(messageId, region, kafkaMessage.getTimestamp(), messageBytes);
            message = messageRepository.save(message);
            
            // Create a Spring Integration message with the KafkaMessage as the payload
            // and add headers for region and messageId
            org.springframework.messaging.Message<KafkaMessage> integrationMessage = 
                    MessageBuilder.withPayload(kafkaMessage)
                            .setHeader(REGION_HEADER, region)
                            .setHeader(MESSAGE_ID_HEADER, messageId)
                            .build();
            
            // Send the message to the channel, which will store it in the JDBC message store
            boolean sent = kafkaOutboxChannel.send(integrationMessage);
            
            if (sent) {
                logger.info("Message stored in outbox channel: {}", messageId);
            } else {
                logger.error("Failed to store message in outbox channel: {}", messageId);
                throw new RuntimeException("Failed to store message in outbox channel");
            }
            
            return message;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize message", e);
            throw new RuntimeException("Failed to serialize message", e);
        }
    }
}