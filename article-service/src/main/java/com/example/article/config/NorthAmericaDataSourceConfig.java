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
public class NorthAmericaDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.northamerica")
    public DataSource northAmericaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean northAmericaEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(northAmericaDataSource())
                .packages("com.example.shared.model")
                .persistenceUnit("northamerica")
                .build();
    }

    @Bean
    public PlatformTransactionManager northAmericaTransactionManager(
            @Qualifier("northAmericaEntityManager") LocalContainerEntityManagerFactoryBean northAmericaEntityManager) {
        return new JpaTransactionManager(northAmericaEntityManager.getObject());
    }
}