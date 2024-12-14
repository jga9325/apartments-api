package com.auger.apartments.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    HikariDataSource hikariDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
}
