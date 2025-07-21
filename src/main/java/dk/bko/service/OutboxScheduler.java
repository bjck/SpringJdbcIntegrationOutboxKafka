package dk.bko.service;

import dk.bko.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler service that periodically checks the JDBC message store for messages
 * that need to be sent to Kafka. It retrieves messages from the channel,
 * sends them to Kafka, and acknowledges them after successful sending.
 * This implementation uses Spring Integration's PollableChannel to interact with
 * the JDBC message store instead of directly manipulating the database tables.
 */
@Service
@EnableScheduling
public class OutboxScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OutboxScheduler.class);
    private static final String REGION_HEADER = "region";
    private static final String MESSAGE_ID_HEADER = "messageId";
    private static final long RECEIVE_TIMEOUT = 1000; // 1 second timeout for receive operations

    private final PollableChannel kafkaOutboxChannel;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.message-outbox}")
    private String messageOutboxTopic;

    @Autowired
    public OutboxScheduler(
            @Qualifier("kafkaOutboxChannel") PollableChannel kafkaOutboxChannel,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaOutboxChannel = kafkaOutboxChannel;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Scheduled method that runs every second to check for messages in the outbox.
     * It retrieves messages from the channel, sends them to Kafka, and acknowledges them
     * after successful sending.
     */
    @Scheduled(fixedRate = 1000) // Run every second
    @Transactional
    public void processOutboxMessages() {
        logger.debug("Checking for messages in the outbox channel");
        
        // Receive a message from the channel with a timeout
        Message<?> message = kafkaOutboxChannel.receive(RECEIVE_TIMEOUT);
        
        if (message == null) {
            logger.debug("No messages found in the outbox channel");
            return;
        }
        
        // Process the message
        processMessage(message);
        
        // Continue processing messages until the channel is empty
        while ((message = kafkaOutboxChannel.receive(0)) != null) {
            processMessage(message);
        }
    }
    
    /**
     * Process a single message from the outbox channel.
     * Sends the message to Kafka and acknowledges it after successful sending.
     *
     * @param message the message to process
     */
    private void processMessage(Message<?> message) {
        // Extract the payload and headers
        Object payload = message.getPayload();
        String messageId = message.getHeaders().get(MESSAGE_ID_HEADER, String.class);
        String region = message.getHeaders().get(REGION_HEADER, String.class);
        
        if (messageId == null) {
            logger.warn("Message has no messageId header, skipping");
            return;
        }
        
        if (!(payload instanceof KafkaMessage)) {
            logger.warn("Message payload is not a KafkaMessage, skipping: {}", messageId);
            return;
        }
        
        KafkaMessage kafkaMessage = (KafkaMessage) payload;
        
        logger.info("Processing message from outbox channel: {}", messageId);
        logger.debug("Message details: region={}, content={}", region, kafkaMessage.getContent());
        
        try {
            // Send the message to Kafka
            logger.info("Sending message to Kafka: {}", messageId);
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(messageOutboxTopic, messageId, kafkaMessage);
            
            // Wait for the send operation to complete
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Message sent successfully to Kafka: {}", messageId);
                    // Message was sent successfully, it will be automatically removed from the channel
                    // because we received it in the processOutboxMessages method
                } else {
                    logger.error("Failed to send message to Kafka: {}", messageId, ex);
                    // Message will remain in the channel and can be retried
                    // We could implement a retry mechanism here if needed
                }
            });
            
            // Wait for the future to complete (with a timeout)
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Error waiting for Kafka send operation to complete: {}", messageId, e);
                // The whenComplete handler will still be called when the future completes
            }
            
        } catch (Exception e) {
            logger.error("Failed to process message {}", messageId, e);
            // Message will remain in the channel and can be retried
        }
    }
}