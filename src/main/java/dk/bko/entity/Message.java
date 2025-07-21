package dk.bko.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Entity class representing a message in the JDBC message store.
 * This maps to the INT_MESSAGE table created by Liquibase.
 */
@Entity
@Table(name = "INT_MESSAGE")
public class Message {

    @Id
    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "REGION")
    private String region;

    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "MESSAGE_BYTES", nullable = false)
    private byte[] messageBytes;

    // Default constructor required by JPA
    public Message() {
    }

    public Message(String messageId, String region, LocalDateTime createdDate, byte[] messageBytes) {
        this.messageId = messageId;
        this.region = region;
        this.createdDate = createdDate;
        this.messageBytes = messageBytes;
    }

    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public void setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", region='" + region + '\'' +
                ", createdDate=" + createdDate +
                ", messageBytes=" + (messageBytes != null ? "[" + messageBytes.length + " bytes]" : "null") +
                '}';
    }
}