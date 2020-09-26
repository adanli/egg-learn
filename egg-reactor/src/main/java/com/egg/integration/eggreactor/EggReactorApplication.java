package com.egg.integration.eggreactor;

import com.egg.integration.eggreactor.reactor.multiMulti.MainReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author adanl
 */
@SpringBootApplication
public class EggReactorApplication {
    private static final Logger logger = LoggerFactory.getLogger(EggReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EggReactorApplication.class, args);

        new Thread(MainReactor.provider(8081)).start();

        logger.info("egg reactor start success");
    }
}
