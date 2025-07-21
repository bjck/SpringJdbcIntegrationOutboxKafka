package dk.bko;

import dk.bko.repository.MessageRepository;
import dk.bko.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Spring Boot application.
 * These tests verify that the application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
public class MainTests {

    @Autowired
    private MessageService messageService;

    @MockBean
    private MessageRepository messageRepository;

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        assertThat(messageService).isNotNull();
    }
}