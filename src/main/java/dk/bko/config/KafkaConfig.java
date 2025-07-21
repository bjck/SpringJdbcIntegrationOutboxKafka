package dk.bko.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Configuration class for Kafka.
 * Sets up Kafka topics, templates, and listeners.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.message-outbox}")
    private String messageOutboxTopic;

    /**
     * Creates the message outbox topic.
     *
     * @return the configured topic
     */
    @Bean
    public NewTopic messageOutboxTopic() {
        return TopicBuilder.name(messageOutboxTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Creates a Kafka template for sending messages.
     *
     * @param producerFactory the producer factory
     * @return the Kafka template
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Configures the Kafka listener container factory with manual acknowledgment.
     *
     * @param consumerFactory the consumer factory
     * @return the configured listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}