package dk.bko.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service for demonstrating how to use the message channel.
 * This service subscribes to the Kafka channel and processes messages received from Kafka.
 */
@Service
public class MessageChannelService {

    private static final Logger logger = LoggerFactory.getLogger(MessageChannelService.class);

    private final DirectChannel kafkaChannel;

    @Autowired
    public MessageChannelService(@Qualifier("kafkaChannel") DirectChannel kafkaChannel) {
        this.kafkaChannel = kafkaChannel;
    }

    /**
     * Initialize the service by subscribing to the Kafka channel.
     * This method is called automatically after the bean is created.
     */
    @PostConstruct
    public void init() {
        // Subscribe to the Kafka channel to receive messages
        kafkaChannel.subscribe(new KafkaMessageHandler());
        logger.info("Subscribed to Kafka channel");
    }

    /**
     * Send a message to the Kafka channel.
     * This demonstrates how to send a message to the channel.
     *
     * @param payload the message payload
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(Object payload) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        logger.info("Sending message to Kafka channel: {}", message);
        return kafkaChannel.send(message);
    }

    /**
     * Handler for messages received from the Kafka channel.
     * This demonstrates how to process messages from the channel.
     */
    private class KafkaMessageHandler implements MessageHandler {
        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            logger.info("Received message from Kafka channel: {}", message);
            // Process the message here
            Object payload = message.getPayload();
            logger.info("Message payload: {}", payload);
        }
    }
}