import com.demo.spring.batch.Order;
import com.demo.spring.batch.SpringBatchTaskletApplication;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBootTest
@SpringBatchTest
@SpringJUnitConfig(SpringBatchTaskletApplication.class)
public class SpringBatchTaskletTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @MockBean
    private Order order;

    @Test
    @SneakyThrows
    public void whenInvalidOrder() {
        Mockito.when(order.isValid()).thenReturn(false);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        long count =
                jobExecution.getStepExecutions().stream().filter(stepExecution -> stepExecution.getStepName().equals("sendErrorNotificationStep")).count();
        Assertions.assertEquals(1, count);
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

    @Test
    @SneakyThrows
    public void whenValidOrder() {
        Mockito.when(order.isValid()).thenReturn(true);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        long count =
                jobExecution.getStepExecutions().stream().filter(stepExecution -> stepExecution.getStepName().equals("deliverOrderStep")).count();
        Assertions.assertEquals(1, count);
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

}
