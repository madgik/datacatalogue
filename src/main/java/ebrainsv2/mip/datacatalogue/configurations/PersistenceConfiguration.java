package ebrainsv2.mip.datacatalogue.configurations;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;


@Configuration
@EnableJpaRepositories({"ebrainsv2.mip.datacatalogue.datamodel","ebrainsv2.mip.datacatalogue.datamodelinfo"})
public class PersistenceConfiguration {
    @Value("${spring.datasource.url}")
    private String db_url;

    @Value("${spring.datasource.username}")
    private String db_username;

    @Value("${spring.datasource.password}")
    private String db_password;

    @Value("${spring.datasource.driver-class-name}")
    private String driver_class_name;

    @Bean(name = "datasource")
    public DataSource datacatalogueDataSource() {
        return DataSourceBuilder.create()
                .url(db_url)
                .username(db_username)
                .password(db_password)
                .driverClassName(driver_class_name)
                .build();
    }
    @Bean(name = "entityManagerFactory")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
        emfb.setDataSource(datacatalogueDataSource());
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emfb.setJpaVendorAdapter(vendorAdapter);
        emfb.setPackagesToScan("ebrainsv2.mip.datacatalogue");
        return emfb;
    }

    @Bean(name = "flyway", initMethod = "migrate")
    public Flyway migrations() {
        return Flyway.configure()
                .dataSource(datacatalogueDataSource())
                .baselineOnMigrate(true)
                .load();

    }
}
