package dk.bko.controller;

import dk.bko.entity.Message;
import dk.bko.service.MessageService;
import dk.bko.service.JdbcOutboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for message operations.
 * Provides HTTP endpoints for working with messages.
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message", description = "Message management APIs")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;
    private final JdbcOutboxService outboxService;

    @Autowired
    public MessageController(MessageService messageService, JdbcOutboxService outboxService) {
        this.messageService = messageService;
        this.outboxService = outboxService;
    }

    /**
     * Create a new message.
     *
     * @param requestBody Map containing 'region' and 'content' fields
     * @return the created message
     */
    @Operation(summary = "Create a new message", description = "Creates a new message with the provided content and optional region")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody Map<String, String> requestBody) {
        String region = requestBody.get("region");
        String content = requestBody.get("content");
        
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        region = region != null ? region : "default";
        
        logger.info("Creating message with region: {}", region);
        Message message = messageService.createMessage(region, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Get a message by its ID.
     *
     * @param messageId the ID of the message
     * @return the message if found, or 404 otherwise
     */
    @Operation(summary = "Get a message by ID", description = "Retrieves a message by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))),
        @ApiResponse(responseCode = "404", description = "Message not found", content = @Content)
    })
    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessage(
            @Parameter(description = "ID of the message to retrieve", required = true) 
            @PathVariable String messageId) {
        logger.info("Getting message with ID: {}", messageId);
        return messageService.getMessage(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get the content of a message as a string.
     *
     * @param messageId the ID of the message
     * @return the content of the message if found, or 404 otherwise
     */
    @GetMapping("/{messageId}/content")
    public ResponseEntity<String> getMessageContent(@PathVariable String messageId) {
        logger.info("Getting content for message with ID: {}", messageId);
        return messageService.getMessageContent(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all messages.
     *
     * @return list of all messages
     */
    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages() {
        logger.info("Getting all messages");
        List<Message> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    /**
     * Get messages by region.
     *
     * @param region the region to filter by
     * @return list of messages in the specified region
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<List<Message>> getMessagesByRegion(@PathVariable String region) {
        logger.info("Getting messages for region: {}", region);
        List<Message> messages = messageService.getMessagesByRegion(region);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get messages created after a specific date.
     *
     * @param date the date to filter by (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * @return list of messages created after the specified date
     */
    @GetMapping("/after")
    public ResponseEntity<List<Message>> getMessagesAfterDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        logger.info("Getting messages after date: {}", date);
        List<Message> messages = messageService.getMessagesAfterDate(date);
        return ResponseEntity.ok(messages);
    }

    /**
     * Delete a message by its ID.
     *
     * @param messageId the ID of the message to delete
     * @return 204 No Content if successful
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        logger.info("Deleting message with ID: {}", messageId);
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Send a message through the outbox pattern.
     * The message is first stored in the database and then sent to Kafka.
     * It is only removed from the database after successful processing.
     *
     * @param requestBody Map containing 'region' and 'content' fields
     * @return the created message
     */
    @Operation(summary = "Send a message through the outbox pattern", 
               description = "Creates a message and sends it to Kafka using the outbox pattern")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message created and sent successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PostMapping("/outbox")
    public ResponseEntity<Message> sendMessageThroughOutbox(@RequestBody Map<String, String> requestBody) {
        String region = requestBody.get("region");
        String content = requestBody.get("content");
        
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        region = region != null ? region : "default";
        
        logger.info("Sending message through outbox with region: {}", region);
        Message message = outboxService.sendMessage(content, region);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}