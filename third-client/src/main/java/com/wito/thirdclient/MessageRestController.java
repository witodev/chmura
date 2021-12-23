package com.wito.thirdclient;

import com.wito.chmura.commonclass.Greeting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message:no conf 3}")
    private String message;

    @GetMapping("/message")
    Greeting getMessage() {
        return new Greeting(message);
    }
}
