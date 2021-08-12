package com.wito.thirdclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message:no conf 3}")
    private String message;

    @RequestMapping("/message")
    Map<String, String> getMessage() {
        return Map.of("greetings", message);
    }
}
