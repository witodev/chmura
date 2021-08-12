package com.wito.thirdclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.wito.thirdclient.ThirdClientApplication.call;

@SpringBootApplication
@EnableDiscoveryClient
public class ThirdClientApplication {

    public static void main(String[] args) {
        SpringBootUtil.setRandomPort(5000, 5500);
        SpringApplication.run(ThirdClientApplication.class, args);
    }

    static Flux<Greeting> call(WebClient http, String url) {
        return http.get().uri(url).retrieve().bodyToFlux(Greeting.class);
    }

    @Bean
    @LoadBalanced
    WebClient.Builder builder() {
        return WebClient.builder();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
    private String greetings;
}

@Component
class ClientCaller {
    private static WebClient http = null;
    public ClientCaller(WebClient http) {
        ClientCaller.http = http;
    }
    public static <T> Flux<T> get(String url, Class<T> clazz) {
        return http.get().uri(url).retrieve().bodyToFlux(clazz);
    }
}

@Log4j2
@Component
class ReactiveLoadBalancerFactoryRunner {

    ReactiveLoadBalancerFactoryRunner(ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory) {
        var http = WebClient.builder().build();
        ReactiveLoadBalancer<ServiceInstance> api = serviceInstanceFactory.getInstance("first-client");
        Flux<Response<ServiceInstance>> chosen = Flux.from(api.choose());
        chosen.map(responseServiceInstance -> {
                    ServiceInstance server = responseServiceInstance.getServer();
                    var url = "http://" + server.getHost() + ':' + server.getPort() + "/message";
                    log.info(url);
                    return url;
                })
                .flatMap(url -> call(http, url))
                .subscribe(greeting -> log.info("manual: " + greeting.toString()));

    }
}

//@Component
//@Log4j2
//class WebClientRunner {
//
//    WebClientRunner(ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory) {
//
//        var filter = new ReactorLoadBalancerExchangeFilterFunction(serviceInstanceFactory);
//
//        var http = WebClient.builder()
//                .filter(filter)
//                .build();
//
//        call(http, "http://api/greetings").subscribe(greeting -> log.info("filter: " + greeting.toString()));
//    }
//}

@Component
@Log4j2
class ConfiguredWebClientRunner  {
    ConfiguredWebClientRunner(WebClient http) {
        call(http, "http://first-client/message").subscribe(greeting -> log.info("configured: " + greeting.toString()));
        ClientCaller.get("http://first-client/message", Greeting.class).subscribe(greeting -> log.info("caller1: " + greeting.toString()));
        ClientCaller.get("http://second-client/message", Greeting.class).subscribe(greeting -> log.info("caller2: " + greeting.toString()));
//        ClientCaller.get("http://third-client/message", Greeting.class).subscribe(greeting -> log.info("caller3: " + greeting.toString()));
    }
}

class SpringBootUtil {
    final static Logger log = LoggerFactory.getLogger(SpringBootUtil.class);

    public static void setRandomPort(int minPort, int maxPort) {
        try {
            final String userDefinedPort = System.getProperty("server.port", System.getenv("SERVER_PORT"));
            if(StringUtils.isEmpty(userDefinedPort)) {
                final int port = SocketUtils.findAvailableTcpPort(minPort, maxPort);
                System.setProperty("server.port", String.valueOf(port));
                log.info("Random Server Port is set to {}.", port);
            }
        } catch( final IllegalStateException e) {
            log.warn("No port available in range {}-{}. Default embedded server configuration will be used.", minPort, maxPort);
        }
    }
}