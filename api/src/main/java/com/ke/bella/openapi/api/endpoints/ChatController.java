package com.ke.bella.openapi.api.endpoints;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    @PostMapping("/completions")
    public Object completion(@RequestBody Object request) {
        return null;
    }
}
