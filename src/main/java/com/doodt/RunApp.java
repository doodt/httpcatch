package com.doodt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author doodt
 * @ClassName RunApp.java
 * @Description TODO
 * @createTime 2023/06/27 16:39:00
 */
@SpringBootApplication
@EnableScheduling
public class RunApp {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RunApp.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
