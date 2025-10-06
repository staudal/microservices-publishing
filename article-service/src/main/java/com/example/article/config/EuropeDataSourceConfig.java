package com.example.article.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class EuropeDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.europe")
    public DataSource europeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean europeEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(europeDataSource())
                .packages("com.example.shared.model")
                .persistenceUnit("europe")
                .build();
    }

    @Bean
    public PlatformTransactionManager europeTransactionManager(
            @Qualifier("europeEntityManager") LocalContainerEntityManagerFactoryBean europeEntityManager) {
        return new JpaTransactionManager(europeEntityManager.getObject());
    }
}