package com.app.userservice.config;

import com.app.userservice.tenant.TenantContext;
import jakarta.annotation.PostConstruct;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@Configuration
public class TenantDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${app.default-schema:public}")
    private String defaultSchema;

    private DataSource defaultDataSource;
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        defaultDataSource = createDataSource();
        jdbcTemplate = new JdbcTemplate(defaultDataSource);
    }

    private DataSource createDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public DataSource tenantDataSource() {
        MultiTenantDataSource dataSource = new MultiTenantDataSource();
        dataSource.setDefaultTargetDataSource(defaultDataSource);
        dataSource.setTargetDataSources(new HashMap<>());
        dataSource.afterPropertiesSet();
        return dataSource;
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new TenantConnectionProvider();
    }

    // Schema creation method
    public void createSchema(String schema) {
        System.out.println("Creating schema if not exists: " + schema);
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
    }

    // MultiTenantConnectionProvider implementation
    private class TenantConnectionProvider extends AbstractMultiTenantConnectionProvider<String> {
        private static final long serialVersionUID = 1L;

        @Override
        protected ConnectionProvider getAnyConnectionProvider() {
            System.out.println("Getting any connection provider with default schema: " + defaultSchema);
            return new ConnectionProviderImpl(defaultDataSource);
        }

        @Override
        protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
            System.out.println("Selecting connection provider for tenant: " + tenantIdentifier);
            return new ConnectionProviderImpl(defaultDataSource, tenantIdentifier);
        }
    }

    // Simple ConnectionProvider implementation
    private class ConnectionProviderImpl implements ConnectionProvider {
        private static final long serialVersionUID = 1L;
        
        private final DataSource dataSource;
        private final String schema;
        
        public ConnectionProviderImpl(DataSource dataSource) {
            this(dataSource, defaultSchema);
        }
        
        public ConnectionProviderImpl(DataSource dataSource, String schema) {
            this.dataSource = dataSource;
            this.schema = schema;
            System.out.println("Created ConnectionProviderImpl for schema: " + schema);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = dataSource.getConnection();
            System.out.println("Setting connection search_path to: " + schema);
            connection.createStatement().execute("SET search_path TO " + schema);
            return connection;
        }

        @Override
        public void closeConnection(Connection connection) throws SQLException {
            System.out.println("Closing connection and resetting search_path to: " + defaultSchema);
            connection.createStatement().execute("SET search_path TO " + defaultSchema);
            connection.close();
        }

        @Override
        public boolean supportsAggressiveRelease() {
            return false;
        }

        @Override
        public boolean isUnwrappableAs(Class<?> unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            return null;
        }
    }

    // Dynamic data source based on current tenant
    private static class MultiTenantDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            String currentTenant = TenantContext.getCurrentTenant();
            System.out.println("MultiTenantDataSource - Current tenant: " + currentTenant);
            return currentTenant;
        }
    }
}