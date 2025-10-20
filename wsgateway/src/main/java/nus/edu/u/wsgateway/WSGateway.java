package nus.edu.u.wsgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * @author Lu Shuwen
 * @date 2025-10-16
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class WSGateway {

    public static void main(String[] args) {
        SpringApplication.run(WSGateway.class, args);
    }
}
