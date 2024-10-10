package com.demo.spring.batch.test;

import com.demo.spring.batch.SpringBatchChunkApplication;
import com.demo.spring.batch.organisation.configuration.OrganisationProducerConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringJUnitConfig({SpringBatchChunkApplication.class, OrganisationProducerConfiguration.class})
@ActiveProfiles("test")
public class OrganisationProducerTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Test
    @DisplayName("GIVEN a file with valid records WHEN job launched THEN records loaded in Apache Kafka")
    @SneakyThrows
    public void shouldReadFromFileAndLoadInKafka() {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

}
