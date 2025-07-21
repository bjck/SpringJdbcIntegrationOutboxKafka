package dk.bko.repository;

import dk.bko.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for the Message entity.
 * Provides CRUD operations and custom queries for messages.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Find messages by region.
     *
     * @param region the region to search for
     * @return list of messages in the specified region
     */
    List<Message> findByRegion(String region);

    /**
     * Find messages created after a specific date.
     *
     * @param date the date to search after
     * @return list of messages created after the specified date
     */
    List<Message> findByCreatedDateAfter(LocalDateTime date);

    /**
     * Find messages by region created within a date range.
     *
     * @param region the region to search for
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of messages matching the criteria
     */
    @Query("SELECT m FROM Message m WHERE m.region = :region AND m.createdDate BETWEEN :startDate AND :endDate")
    List<Message> findByRegionAndDateRange(
            @Param("region") String region,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}