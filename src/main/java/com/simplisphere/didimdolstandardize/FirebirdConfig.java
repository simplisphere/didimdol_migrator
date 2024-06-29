package com.simplisphere.didimdolstandardize;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.simplisphere.didimdolstandardize.firebird",
        entityManagerFactoryRef = "firebirdEntityManagerFactory",
        transactionManagerRef = "firebirdTransactionManager"
)
public class FirebirdConfig {

    @Bean
    @Primary
    public DataSource firebirdDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:firebirdsql://localhost:3055//firebird/data/demo.fdb?authPlugins=Legacy_Auth&encoding=KSC_5601")
                .username("sysdba")
                .password("masterkey")
                .driverClassName("org.firebirdsql.jdbc.FBDriver")
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean firebirdEntityManagerFactory(
            @Qualifier("firebirdDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.simplisphere.didimdolstandardize.firebird");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager firebirdTransactionManager(
            @Qualifier("firebirdEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}