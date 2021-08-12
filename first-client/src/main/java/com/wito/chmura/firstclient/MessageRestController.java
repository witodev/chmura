package com.wito.chmura.firstclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message:Default first client}")
    private String message;

    @RequestMapping("/message")
    Map<String, String> getMessage() {
        return Map.of("greetings", message);
    }
}
