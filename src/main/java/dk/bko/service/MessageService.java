package dk.bko.service;

import dk.bko.entity.Message;
import dk.bko.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for message operations.
 * Provides business logic for working with messages in the JDBC message store.
 */
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Create a new message with the given content.
     *
     * @param region the region for the message
     * @param content the content of the message as a string
     * @return the created message
     */
    @Transactional
    public Message createMessage(String region, String content) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        byte[] messageBytes = content.getBytes();
        
        Message message = new Message(messageId, region, now, messageBytes);
        
        logger.info("Creating new message with ID: {}", messageId);
        return messageRepository.save(message);
    }

    /**
     * Get a message by its ID.
     *
     * @param messageId the ID of the message
     * @return the message if found, or empty optional otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Message> getMessage(String messageId) {
        logger.info("Retrieving message with ID: {}", messageId);
        return messageRepository.findById(messageId);
    }

    /**
     * Get all messages.
     *
     * @return list of all messages
     */
    @Transactional(readOnly = true)
    public List<Message> getAllMessages() {
        logger.info("Retrieving all messages");
        return messageRepository.findAll();
    }

    /**
     * Get messages by region.
     *
     * @param region the region to filter by
     * @return list of messages in the specified region
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesByRegion(String region) {
        logger.info("Retrieving messages for region: {}", region);
        return messageRepository.findByRegion(region);
    }

    /**
     * Get messages created after a specific date.
     *
     * @param date the date to filter by
     * @return list of messages created after the specified date
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesAfterDate(LocalDateTime date) {
        logger.info("Retrieving messages created after: {}", date);
        return messageRepository.findByCreatedDateAfter(date);
    }

    /**
     * Delete a message by its ID.
     *
     * @param messageId the ID of the message to delete
     */
    @Transactional
    public void deleteMessage(String messageId) {
        logger.info("Deleting message with ID: {}", messageId);
        messageRepository.deleteById(messageId);
    }

    /**
     * Get the content of a message as a string.
     *
     * @param messageId the ID of the message
     * @return the content of the message as a string, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<String> getMessageContent(String messageId) {
        return getMessage(messageId)
                .map(message -> new String(message.getMessageBytes()));
    }
}