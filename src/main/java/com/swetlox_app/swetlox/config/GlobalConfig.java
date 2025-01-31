package com.swetlox_app.swetlox.config;

import com.cloudinary.Cloudinary;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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


    @Value("${cloudinary.cloud_name}")
    private static String CLOUD_NAME;
    @Value("${cloudinary.api_key}")
    private static String CLOUD_API_KEY;
    @Value("${cloudinary.api_secret}")
    private static String CLOUD_API_SECRET;

    @Bean(name = "taskExecutor")
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
        config.put("cloud_name",CLOUD_NAME);
        config.put("api_key",CLOUD_API_KEY);
        config.put("api_secret",CLOUD_API_SECRET);
        return new Cloudinary(config);
    }

    @Bean
    public ModelMapper mapper(){
        return new ModelMapper();
    }
}
