package ru.bionicpro.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ClickHouseConfig {
    @Bean
    @ConfigurationProperties(prefix = "clickhouse.datasource")
    public DataSourceProperties clickHouseDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource() {
        return clickHouseDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "clickHouseJdbcTemplate")
    JdbcTemplate clickHouseJdbcTemplate(@Qualifier("clickHouseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "clickHouseFlyway")
    public Flyway clickHouseFlyway(@Qualifier("clickHouseDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/clickhouse")
                .load();
        flyway.migrate();
        return flyway;
    }
}