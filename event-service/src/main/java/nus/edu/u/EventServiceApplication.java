package nus.edu.u;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Lu Shuwen
 * @date 2025-10-16
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("nus.edu.u.event.mapper")
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
