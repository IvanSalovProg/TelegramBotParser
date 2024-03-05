package com.codereview.telegrambotparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Bean
    public Executor jobExecutor() {
        return Executors.newCachedThreadPool();
    }
}
