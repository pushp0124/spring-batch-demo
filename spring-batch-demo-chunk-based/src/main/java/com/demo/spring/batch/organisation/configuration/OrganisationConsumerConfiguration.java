package com.demo.spring.batch.organisation.configuration;

import com.demo.spring.batch.organisation.model.Organisation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.kafka.builder.KafkaItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Properties;

@Configuration
@Slf4j
public class OrganisationConsumerConfiguration {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Bean
    public ItemReader<Organisation> organisationConsumerItemReader() {
        Properties properties = new Properties();
        properties.putAll(kafkaProperties.buildConsumerProperties(null));
        return new KafkaItemReaderBuilder<String, Organisation>()
                .name("organisationKafkaReader")
                .consumerProperties(properties)
                .topic(topicName)
                .partitions(0)
                .build();
    }


    @Bean
    public ItemProcessor<Organisation, Organisation> organisationConsumerItemProcessor() {
        return new ItemProcessor<Organisation, Organisation>() {
            @Override
            public Organisation process(Organisation org) throws Exception {
                if (org.yearFounded() < 1998) {
                    log.info("OrganisationDTO sidelined as didn't satisfy filter condition {}", org);
                    return null;
                }
                return org;
            }
        };
    }


    @Bean
    public ItemWriter<Organisation> organisationConsumerItemWriter() {
        return new MongoItemWriterBuilder<Organisation>()
                .template(mongoTemplate)
                .collection("organisation")
                .build();
    }


    @Bean
    public Step organisationConsumerStep(JobRepository repository, PlatformTransactionManager transactionManager,
                                         @Qualifier("organisationConsumerItemReader") ItemReader<Organisation> reader,
                                         @Qualifier("organisationConsumerItemProcessor") ItemProcessor<Organisation, Organisation> processor,
                                         @Qualifier("organisationConsumerItemWriter") ItemWriter<Organisation> writer) throws Exception {
        return new StepBuilder("KafkaToMongoStep", repository)
                .<Organisation, Organisation>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }


    @Bean
    public Job organisationConsumerJob(JobRepository jobRepository,
                                       @Qualifier("organisationConsumerStep") Step step,
                                       JobExecutionListener listener) throws Exception {
        return new JobBuilder("KafkaToMongoJob", jobRepository)
                .listener(listener)
                .incrementer(new RunIdIncrementer())
                .start(step).build();

    }

}