package com.ke.bella.openapi.endpoints;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ke.bella.openapi.annotations.EndpointAPI;

@EndpointAPI
@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    @PostMapping("/completions")
    public Object completion(@RequestBody Object request) {
        return null;
    }
}
