package com.wito.thirdclient;

import com.wito.chmura.commonclass.Greeting;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootApplication
@EnableDiscoveryClient
@Log4j2
public class FourthClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(FourthClientApplication.class, args);
        ClientCaller.get("http://third-client/message", Greeting.class).subscribe(greeting -> log.info("caller3: " + greeting.toString()));
    }

    @Bean("loadBalancedBuilder")
    @LoadBalanced
    WebClient.Builder builder() {
        return WebClient.builder();
    }

    @Bean("loadBalancedWebClient")
    @DependsOn("loadBalancedBuilder")
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}

@Component
@DependsOn("loadBalancedWebClient")
class ClientCaller {
    private static WebClient http = null;

    private ClientCaller(WebClient http) {
        ClientCaller.http = http;
    }

    public static <T> Flux<T> get(String url, Class<T> clazz) {
        return http.get().uri(url).retrieve().bodyToFlux(clazz);
    }
}