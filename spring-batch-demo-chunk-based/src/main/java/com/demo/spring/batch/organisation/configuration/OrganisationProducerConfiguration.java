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
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.kafka.builder.KafkaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class OrganisationProducerConfiguration {

    private final String[] tokens = {"index", "id", "name", "website", "country", "description", "yearFounded", "industry",
            "numberOfEmployees"};

    private static final String FILE_NAME = "organizations.csv";

    @Autowired
    private KafkaTemplate<String, Organisation> kafkaTemplate;

    @Bean
    public ItemReader<Organisation> organisationProducerItemReader() {
        return new FlatFileItemReaderBuilder<Organisation>()
                .name("organisationCSVReader")
                .resource(new ClassPathResource(FILE_NAME))
                .delimited()
                .delimiter(",")
                .names(tokens)
                .linesToSkip(1)
                .targetType(Organisation.class)
                .build();
    }

    @Bean
    public ItemProcessor<Organisation, Organisation> organisationProducerItemProcessor() {
        return new ItemProcessor<Organisation, Organisation>() {
            @Override
            public Organisation process(Organisation org) throws Exception {
                if (org.numberOfEmployees() <= 1000) {
                    log.info("OrganisationDTO sidelined as didn't satisfy filter condition {}", org);
                    return null;
                }
                return org;
            }
        };
    }

    @Bean
    public ItemWriter<Organisation> organisationProducerItemWriter() {
        return new KafkaItemWriterBuilder<String, Organisation>()
                .kafkaTemplate(kafkaTemplate)
                .itemKeyMapper(Organisation::id)
                .build();
    }


//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(5);
//        taskExecutor.setMaxPoolSize(7);
//        taskExecutor.setQueueCapacity(10);
//        taskExecutor.initialize();
//        return taskExecutor;
//    }


    @Bean
    public Step organisationProducerStep(JobRepository repository, PlatformTransactionManager transactionManager,
                                         @Qualifier("organisationProducerItemReader") ItemReader<Organisation> reader,
                                         @Qualifier("organisationProducerItemProcessor") ItemProcessor<Organisation, Organisation> processor,
                                         @Qualifier("organisationProducerItemWriter") ItemWriter<Organisation> writer) throws Exception {
        return new StepBuilder("CsvToKafkaStep", repository)
                .<Organisation, Organisation>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
//                .taskExecutor(taskExecutor())
                .writer(writer)
                .build();
    }



    @Bean
    public Job organisationProducerJob(JobRepository jobRepository,
                                       @Qualifier("organisationProducerStep") Step organisationProducerStep,
                                       JobExecutionListener listener) throws Exception {
        return new JobBuilder("CsvToKafkaJob", jobRepository)
                .listener(listener)
                .incrementer(new RunIdIncrementer())
                .start(organisationProducerStep).build();

    }

}