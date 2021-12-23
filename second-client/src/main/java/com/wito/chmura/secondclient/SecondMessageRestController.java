package com.wito.chmura.secondclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope
@RestController
class SecondMessageRestController {

    @Value("${message:Default second client}")
    private String message;

    @GetMapping("/message")
    Map<String, String> getMessage() {
        return Map.of("greetings", message);
    }
}
