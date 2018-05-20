package com.github.tiwarivikash.dbreadwrite.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.github.tiwarivikash.dbreadwrite.config.DataSourceType.MASTER;
import static com.github.tiwarivikash.dbreadwrite.config.DataSourceType.READ_REPLICA;

enum DataSourceType {
    READ_REPLICA, MASTER;
}

@Configuration
@EnableJpaRepositories(basePackages = {"com.github.tiwarivikash.dbreadwrite.dao"})
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean(name = "readDataSource")
    public DataSource readDataSource(@Value("${read.datasource.url}") String url,
                                     @Value("${read.datasource.username}") String username,
                                     @Value("${read.datasource.password}") String password) {
        BasicDataSource readDataSource = new BasicDataSource();
        readDataSource.setUrl(url);
        readDataSource.setUsername(username);
        readDataSource.setPassword(password);
        return readDataSource;
    }

    @Bean(name = "writeDataSource")
    public DataSource writeDataSource(@Value("${write.datasource.url}") String url,
                                      @Value("${write.datasource.username}") String username,
                                      @Value("${write.datasource.password}") String password) {
        BasicDataSource writeDataSource = new BasicDataSource();
        writeDataSource.setUrl(url);
        writeDataSource.setUsername(username);
        writeDataSource.setPassword(password);
        return writeDataSource;
    }

    @Bean(name = "routingDataSource")
    @DependsOn({"writeDataSource", "readDataSource"})
    public DataSource routingDataSource(@Qualifier("writeDataSource") DataSource writeDataSource,
                                        @Qualifier("readDataSource") DataSource readDataSource) {
        final Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(MASTER, writeDataSource);
        dataSourceMap.put(READ_REPLICA, readDataSource);

        final AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {

            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? READ_REPLICA : MASTER;
            }
        };

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        return routingDataSource;
    }

    @Bean(name = "dataSource")
    @DependsOn("routingDataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(@Qualifier("dataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.github.tiwarivikash.dbreadwrite.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(additionalProperties());
        em.afterPropertiesSet();
        return em.getObject();
    }

    Map<String, Object> additionalProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL94Dialect");
        return properties;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }


}
