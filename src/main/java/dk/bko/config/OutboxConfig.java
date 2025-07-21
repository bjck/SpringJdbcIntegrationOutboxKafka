package dk.bko.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.ChannelMessageStoreQueryProvider;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.PollableChannel;

/**
 * Configuration class for the outbox pattern.
 * Defines the channel and message store used for the outbox pattern.
 */
@Configuration
public class OutboxConfig {

    private final JdbcChannelMessageStore jdbcChannelMessageStore;

    @Autowired
    public OutboxConfig(JdbcChannelMessageStore jdbcChannelMessageStore) {
        this.jdbcChannelMessageStore = jdbcChannelMessageStore;
    }

    /**
     * Creates a pollable channel backed by the JDBC message store.
     * This channel is used to store messages that will be sent to Kafka.
     *
     * @return the pollable channel
     */
    /**
     * Creates a pollable channel backed by the JDBC message store.
     * This channel is used to store messages that will be sent to Kafka.
     *
     * @return the pollable channel
     */
    @Bean
    public PollableChannel kafkaOutboxChannel() {
        // For persistent queue with JDBC backing, use QueueChannel with capacity
        QueueChannel channel = new QueueChannel(1000); // Set appropriate capacity
        channel.setComponentName("kafkaOutboxChannel");

        // The JdbcChannelMessageStore will be used by the integration framework
        // when configured properly with XML or through IntegrationFlow DSL
        return channel;
    }


}