package com.example.article.config;

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
public class AfricaDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.africa")
    public DataSource africaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean africaEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(africaDataSource())
                .packages("com.example.shared.model")
                .persistenceUnit("africa")
                .build();
    }

    @Bean
    public PlatformTransactionManager africaTransactionManager(
            @Qualifier("africaEntityManager") LocalContainerEntityManagerFactoryBean africaEntityManager) {
        return new JpaTransactionManager(africaEntityManager.getObject());
    }
}