package dk.bko.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration class for the JDBC message store.
 * Sets up the message store using the existing database tables.
 */
@Configuration
public class JdbcMessageStoreConfig {

    /**
     * Creates a JDBC channel message store bean.
     * This uses the existing INT_MESSAGE, INT_GROUP_MESSAGE, INT_MESSAGE_GROUP, and INT_LOCK tables.
     *
     * @param dataSource the data source
     * @return the configured JDBC channel message store
     */
    @Bean
    public JdbcChannelMessageStore jdbcChannelMessageStore(DataSource dataSource) {
        JdbcChannelMessageStore messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(
                new org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider());
        return messageStore;
    }

    /**
     * Creates a lock registry for distributed locking.
     * This is used to ensure that only one instance processes a message at a time.
     *
     * @param jdbcTemplate the JDBC template
     * @return the configured lock registry
     */
    @Bean
    public LockRegistry lockRegistry(JdbcTemplate jdbcTemplate) {
        return new DefaultLockRegistry();
    }
}