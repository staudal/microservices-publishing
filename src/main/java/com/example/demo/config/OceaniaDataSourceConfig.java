package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class OceaniaDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.oceania")
    public DataSource oceaniaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean oceaniaEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(oceaniaDataSource())
                .packages("com.example.demo")
                .persistenceUnit("oceania")
                .build();
    }

    @Bean
    public PlatformTransactionManager oceaniaTransactionManager(
            @Qualifier("oceaniaEntityManager") LocalContainerEntityManagerFactoryBean oceaniaEntityManager) {
        return new JpaTransactionManager(oceaniaEntityManager.getObject());
    }
}