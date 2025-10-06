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
public class AsiaDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.asia")
    public DataSource asiaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean asiaEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(asiaDataSource())
                .packages("com.example.shared.model")
                .persistenceUnit("asia")
                .build();
    }

    @Bean
    public PlatformTransactionManager asiaTransactionManager(
            @Qualifier("asiaEntityManager") LocalContainerEntityManagerFactoryBean asiaEntityManager) {
        return new JpaTransactionManager(asiaEntityManager.getObject());
    }
}