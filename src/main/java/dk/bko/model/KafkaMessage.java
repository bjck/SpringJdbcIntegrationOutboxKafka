package dk.bko.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Model class for messages sent to Kafka.
 * This is the payload that will be serialized and sent to Kafka.
 */
public class KafkaMessage implements Serializable {

    private String id;
    private String content;
    private String region;
    private LocalDateTime timestamp;

    // Default constructor required for JSON deserialization
    public KafkaMessage() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public KafkaMessage(String content, String region) {
        this();
        this.content = content;
        this.region = region;
    }

    public KafkaMessage(String id, String content, String region, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.region = region;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaMessage that = (KafkaMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "KafkaMessage{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", region='" + region + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}