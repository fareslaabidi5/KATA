package com.astrelya.katabank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EntityScan(basePackages = "com.astrelya.katabank.Entities")
@EnableJpaRepositories(basePackages = "com.astrelya.katabank.Repositories")
@EnableRetry
public class KataBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(KataBankApplication.class, args);
    }

}
