package nus.edu.u.attendee;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Lu Shuwen
 * @date 2025-10-18
 */
@SpringBootApplication
@EnableDubbo
public class AttendeeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendeeServiceApplication.class, args);
    }
}
