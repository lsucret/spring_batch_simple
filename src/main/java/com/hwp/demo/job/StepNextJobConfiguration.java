package com.hwp.demo.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StepNextJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job stepNextJob() {
        return jobBuilderFactory.get("stepNextJob")
                .start(step1())
                .next(step2())
                .next(step3())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step1");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }//1

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
//
//    @Bean
//    public Job scopeJob() {
//        return jobBuilderFactory.get("scopeJob")
//                .start(scopeStep1(null))
//                .next(scopeStep2())
//                .build();
//    }
//
//    @Bean
//    @JobScope
//    public Step scopeStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
//
//        return stepBuilderFactory.get("scopeStep1")
//        .tasklet((contribution, chunkContext) -> {
//            log.info(">>>>> This is scopeStep1");
//            log.info(">>>>> requestDate = {}", requestDate);
//            return RepeatStatus.FINISHED;
//        })
//        .build();
//    }
//
//    @Bean
//    public Step scopeStep2() {
//        return stepBuilderFactory.get("scopeStep2")
//                .tasklet(scopeStep2Tasklet(null))
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public ListItemReader<Integer> simpleWriterReader() {
//        List<Integer> items = new ArrayList<>();
//
//        for (int i = 0; i < 100; i++) {
//            items.add(i);
//        }
//
//        return new ListItemReader<>(items);
//    }
//
//    @Bean
//    @StepScope
//    public Tasklet scopeStep2Tasklet(@Value("#{jobParameters[requestDate]}") String requestDate) {
//
//        return (contribution, chunkContext) -> {
//            log.info(">>>>> This is scopeStep1");
//            log.info(">>>>> requestDate = {}", requestDate);
//            return RepeatStatus.FINISHED;
//        };
//    }
}
