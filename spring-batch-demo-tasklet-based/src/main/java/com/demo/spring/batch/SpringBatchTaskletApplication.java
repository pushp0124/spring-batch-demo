package com.demo.spring.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
@Slf4j
public class SpringBatchTaskletApplication {


    @Bean("order")
    public Order createOrder() {
        Order order = new Order();
        order.setOrderId("11");
        order.setProduct("Apple Macbook");
        order.setPrice(1001.00);
        order.setValid(true);
        return order;
    }
    @Bean
    public Step validateOrderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Order order) {
        return new StepBuilder("validateOrderStep", jobRepository).tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                logStep(contribution,chunkContext);
                if(!order.isValid())
                    contribution.setExitStatus(ExitStatus.FAILED);
                return RepeatStatus.FINISHED;
            }
        }, transactionManager)
                .build();
    }

    @Bean
    public Step deliverOrderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deliverOrderStep", jobRepository).tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                logStep(contribution,chunkContext);
                return RepeatStatus.FINISHED;
            }
        }, transactionManager).build();
    }

    @Bean
    public Step sendErrorNotificationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendErrorNotificationStep", jobRepository).tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                logStep(contribution,chunkContext);
                return RepeatStatus.FINISHED;
            }
        }, transactionManager).build();
    }

    @Bean
    public Job processOrderJob(JobRepository jobRepository, Step validateOrderStep, Step deliverOrderStep, Step sendErrorNotificationStep) {
        return new JobBuilder("processOrderJob", jobRepository)
                .start(validateOrderStep)
                .on(ExitStatus.COMPLETED.getExitCode()).to(deliverOrderStep)
                .from(validateOrderStep).on("*").to(sendErrorNotificationStep)
                .end()
                .build();
    }

    private void logStep(StepContribution contribution, ChunkContext chunkContext) {
         log.info("Tasklet logger mentioning job name : {} and step name : {} ", chunkContext.getStepContext().getJobName(), contribution.getStepExecution().getStepName());
    }
    public static void main(String[] args) {
       SpringApplication.run(SpringBatchTaskletApplication.class, args);
    }

}
