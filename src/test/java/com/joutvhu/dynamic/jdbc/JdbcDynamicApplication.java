package com.joutvhu.dynamic.jdbc;

import com.joutvhu.dynamic.commons.DynamicQueryTemplateProvider;
import com.joutvhu.dynamic.freemarker.FreemarkerQueryTemplateProvider;
import com.joutvhu.dynamic.jdbc.support.DynamicJdbcRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@EnableTransactionManagement
@EnableJdbcRepositories(
        basePackages = {"com.joutvhu.dynamic.jdbc.repository"},
        repositoryFactoryBeanClass = DynamicJdbcRepositoryFactoryBean.class
)
public class JdbcDynamicApplication {
    public static void main(String[] args) {
        SpringApplication.run(JdbcDynamicApplication.class);
    }

    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "dataSourceInitializer")
    public DataSourceInitializer emdDataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/table.sql"));
        populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
        initializer.setDatabasePopulator(populator);
        initializer.setEnabled(true);
        initializer.afterPropertiesSet();
        return initializer;
    }

    @Bean
    public DynamicQueryTemplateProvider dynamicQueryTemplates() {
        DynamicQueryTemplateProvider queryTemplates = new FreemarkerQueryTemplateProvider();
        queryTemplates.setSuffix(".dsql");
        return queryTemplates;
    }
}
