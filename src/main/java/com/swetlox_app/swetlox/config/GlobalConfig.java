package com.swetlox_app.swetlox.config;

import com.cloudinary.Cloudinary;
import org.modelmapper.ModelMapper;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAsync
public class GlobalConfig {
    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutorBuilder().concurrencyLimit(10)
                .virtualThreads(true)
                .build();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(){
        SimpleApplicationEventMulticaster applicationEventMulticaster=new SimpleApplicationEventMulticaster();
        applicationEventMulticaster.setTaskExecutor(taskExecutor());
        return applicationEventMulticaster;
    }

    @Bean
    public Cloudinary cloudinaryTemplate(){
        Map<String,String> config=new HashMap<>();
        config.put("cloud_name","CLOUD NAME");
        config.put("api_key","API KEY");
        config.put("api_secret","API SECRET");
        return new Cloudinary(config);
    }

    @Bean
    public ModelMapper mapper(){
        return new ModelMapper();
    }
}
