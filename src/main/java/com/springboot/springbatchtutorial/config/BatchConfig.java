package com.springboot.springbatchtutorial.config;

import com.springboot.springbatchtutorial.entity.Person;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfiguration {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job csvFileToDatabaseJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // Job을 정의하고 시작할 Step을 지정
        return new JobBuilder("csvFileToDatabaseJob")
                .repository(jobRepository)
                .start(csvFileToDatabaseStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step csvFileToDatabaseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // Step을 정의하고 Chunk 지향 작업을 지정
        return new StepBuilder("csvFileToDatabaseStep")
                .repository(jobRepository)
                .<Person, Person>chunk(10)
                .reader(csvItemReader())
                .processor(csvItemProcessor())
                .writer(jpaItemWriter())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public ItemReader<Person> csvItemReader() {
        // CSV 파일에서 데이터를 읽어오는 ItemReader를 정의
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data.csv"));
        reader.setLinesToSkip(1); // 첫 번째 라인을 건너뜀 (헤더)
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("name", "email");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<Person, Person> csvItemProcessor() {
        // 데이터를 처리하는 ItemProcessor를 정의
        return person -> {
            person.setName(person.getName().toUpperCase());
            person.setEmail(person.getEmail().toUpperCase());
            return person;
        };
    }

    @Bean
    public ItemWriter<Person> jpaItemWriter() {
        // JPA를 사용하여 데이터를 저장하는 ItemWriter를 정의
        JpaItemWriter<Person> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job testJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws DuplicateJobException {
        // Job을 정의하고 시작할 Step을 지정한다.
        return new JobBuilder("testJob")
                .repository(jobRepository)
                .start(testStep(jobRepository, transactionManager))
                .build();
    }

    public Step testStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // Step을 정의하고 Tasklet을 지정한다.
        return new StepBuilder("testStep")
                .repository(jobRepository)
                .tasklet(testTasklet())
                .transactionManager(transactionManager)
                .build();
    }

    public Tasklet testTasklet() {
        // Tasklet을 정의하고 비즈니스 로직을 작성
        return ((contribution, chunkContext) -> {
            System.out.println("***** 10초마다 'Hello batch' 출력!! *****"); // 콘솔에 출력합니다.
            // 원하는 비지니스 로직 작성
            return RepeatStatus.FINISHED; // 작업이 완료되었음을 나타낸다.
        });
    }
}
