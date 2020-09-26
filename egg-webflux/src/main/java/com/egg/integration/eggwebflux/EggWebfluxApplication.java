package com.egg.integration.eggwebflux;

import com.egg.integration.eggwebflux.client.GreetingWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class EggWebfluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(EggWebfluxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EggWebfluxApplication.class);
        logger.info("egg webflux start success");

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            logger.error("", e);
        }

        GreetingWebClient client = new GreetingWebClient();
//        System.out.println(client.getResult());
        logger.info("result: {}", client.getResult());

    }
}
