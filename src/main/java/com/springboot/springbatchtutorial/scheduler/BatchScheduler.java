package com.springboot.springbatchtutorial.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component // Spring Bean으로 등록됨을 나타낸다.
public class BatchScheduler {

    @Autowired
    private JobLauncher jobLauncher; // JobLauncher 주입

    @Autowired
    private JobRegistry jobRegistry; // JobRegistry 주입

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        // JobRegistryBeanPostProcessor 빈을 생성한다.
        JobRegistryBeanPostProcessor jobProcessor = new JobRegistryBeanPostProcessor();
        jobProcessor.setJobRegistry(jobRegistry);
        return jobProcessor;
    }

    @Scheduled(cron = "0/10 * * * * *") // 10초마다 실행
    public void runJob() {
        String time = LocalDateTime.now().toString(); // 현재 시간을 문자열로 변환한다.
        try {
            Job job = jobRegistry.getJob("testJob"); // jobRegistry에서 "testJob"을 가져온다.
            JobParametersBuilder jobParam = new JobParametersBuilder().addString("time", time); // JobParametersBuilder에 현재 시간을 추가한다.
            jobLauncher.run(job, jobParam.toJobParameters()); // Job을 실행한다.
        } catch (NoSuchJobException e) {
            throw new RuntimeException(e); // Job을 찾을 수 없는 예외를 처리한다.
        } catch (JobInstanceAlreadyCompleteException |
                 JobExecutionAlreadyRunningException |
                 JobParametersInvalidException |
                 JobRestartException e) {
            throw new RuntimeException(e); // 여러 예외를 처리한다.
        }
    }
}
