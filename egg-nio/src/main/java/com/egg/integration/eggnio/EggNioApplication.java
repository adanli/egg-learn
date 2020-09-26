package com.egg.integration.eggnio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author adanl
 */
@SpringBootApplication
public class EggNioApplication {
    private static final Logger logger = LoggerFactory.getLogger(EggNioApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EggNioApplication.class, args);
        logger.info("egg nio start success");
    }

}
