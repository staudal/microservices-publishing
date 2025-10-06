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
public class SouthAmericaDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.southamerica")
    public DataSource southAmericaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean southAmericaEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(southAmericaDataSource())
                .packages("com.example.shared.model")
                .persistenceUnit("southamerica")
                .build();
    }

    @Bean
    public PlatformTransactionManager southAmericaTransactionManager(
            @Qualifier("southAmericaEntityManager") LocalContainerEntityManagerFactoryBean southAmericaEntityManager) {
        return new JpaTransactionManager(southAmericaEntityManager.getObject());
    }
}