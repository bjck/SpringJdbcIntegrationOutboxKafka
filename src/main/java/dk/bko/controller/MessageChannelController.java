package dk.bko.controller;

import dk.bko.service.MessageChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for message channel operations.
 * Provides HTTP endpoints for testing the message channel.
 */
@RestController
@RequestMapping("/api/channel")
@Tag(name = "Message Channel", description = "Message channel testing APIs")
public class MessageChannelController {

    private static final Logger logger = LoggerFactory.getLogger(MessageChannelController.class);

    private final MessageChannelService messageChannelService;

    @Autowired
    public MessageChannelController(MessageChannelService messageChannelService) {
        this.messageChannelService = messageChannelService;
    }

    /**
     * Send a message to the Kafka channel.
     * This demonstrates how to use the message channel.
     *
     * @param requestBody Map containing 'content' field
     * @return success or failure message
     */
    @Operation(summary = "Send a message to the Kafka channel", 
               description = "Sends a message to the Kafka channel using Spring Integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/send")
    public ResponseEntity<String> sendMessageToChannel(@RequestBody Map<String, String> requestBody) {
        String content = requestBody.get("content");
        
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest().body("Content is required");
        }
        
        logger.info("Sending message to Kafka channel: {}", content);
        boolean sent = messageChannelService.sendMessage(content);
        
        if (sent) {
            return ResponseEntity.ok("Message sent to channel successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send message to channel");
        }
    }
}