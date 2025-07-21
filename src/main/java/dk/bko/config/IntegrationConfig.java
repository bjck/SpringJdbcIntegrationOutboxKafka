package dk.bko.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.messaging.MessageChannel;

/**
 * Configuration class for Spring Integration.
 * Defines message channels and adapters for the integration flow.
 */
@Configuration
public class IntegrationConfig {

    @Value("${kafka.topic.message-outbox}")
    private String messageOutboxTopic;

    /**
     * Message channel for Kafka messages.
     * This is the channel that the inbound channel adapter will write to.
     */
    @Bean
    public DirectChannel kafkaChannel() {
        return new DirectChannel();
    }

    /**
     * Kafka inbound channel adapter.
     * This adapter receives messages from Kafka and puts them on the kafka channel.
     * This is the component that was missing in the original implementation.
     */
    @Bean
    public KafkaMessageDrivenChannelAdapter<String, Object> kafkaInboundChannelAdapter(
            ConsumerFactory<String, Object> consumerFactory) {
        
        ContainerProperties containerProperties = new ContainerProperties(messageOutboxTopic);
        KafkaMessageListenerContainer<String, Object> container = 
                new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        KafkaMessageDrivenChannelAdapter<String, Object> adapter = 
                new KafkaMessageDrivenChannelAdapter<>(container);
        adapter.setOutputChannel(kafkaChannel());
        
        return adapter;
    }
}