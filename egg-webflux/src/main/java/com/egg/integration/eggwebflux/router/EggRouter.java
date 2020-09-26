package com.egg.integration.eggwebflux.router;

import com.egg.integration.eggwebflux.handler.DemoGreetingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
public class EggRouter {

    @Bean
    public RouterFunction<ServerResponse> route(DemoGreetingHandler handler) {
        return RouterFunctions
                .route(
                        RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        handler::hello
                );
    }

}

