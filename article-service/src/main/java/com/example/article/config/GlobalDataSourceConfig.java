package com.example.article.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.article",
        entityManagerFactoryRef = "globalEntityManager",
        transactionManagerRef = "globalTransactionManager"
)
public class GlobalDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.global")
    public DataSource globalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean globalEntityManager(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(globalDataSource())
                .packages("com.example.shared.model")
                .persistenceUnit("global")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager globalTransactionManager(
            @Qualifier("globalEntityManager") LocalContainerEntityManagerFactoryBean globalEntityManager) {
        return new JpaTransactionManager(globalEntityManager.getObject());
    }
}