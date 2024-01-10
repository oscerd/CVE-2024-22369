package com.example.camelsql;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.apache.camel.component.sql.SqlComponent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Component
@Service
public class RestApi extends RouteBuilder {

    @Autowired
    DataSource dataSource;


    @Autowired
    @Bean
    public SqlComponent sql(DataSource dataSource) {
        SqlComponent sql = new SqlComponent();
        sql.setDataSource(dataSource);
        return sql;
    }


    public JdbcAggregationRepository myAggregationRepository( PlatformTransactionManager transactionManager,DataSource dataSource) {
        final JdbcAggregationRepository repository = new JdbcAggregationRepository();
        repository.setRepositoryName("employee");
        repository.setTransactionManager(transactionManager);
        repository.setDataSource(dataSource);
        return repository;
    }

    @Override
    public void configure() throws Exception {

        // Select Route
        from("timer:select?repeatCount=1")
                .setHeader("aaa").header("aaa")
                .setBody(body())
                .aggregate(simple("${header.aaa} == 0"),new MyAggregationStrategy())
                .aggregationRepository(myAggregationRepository(new DataSourceTransactionManager(dataSource),dataSource))
                .completionSize(10).log("aggregated exchange id ${exchangeId} with ${body}").to("mock:aggregated")
                // simulate errors the first two times
                .process(new Processor() {
                    public void process(Exchange exchange) {

                        throw new IllegalArgumentException("Damn");

                    }
                }).end();

    }
}
