package com.ke.bella.openapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Stan Sai Date: 2024/8/13 10:23 description:
 */
@RestController
public class EndpointRequestController {
    @PostMapping("/v1/chat/completions")
    public Object completion(@RequestBody Object request) {
        return null;
    }

    @PostMapping("/v1/embeddings")
    public Object embedding(@RequestBody Object request) {
        return null;
    }

    @PostMapping("/v1/audio/speech")
    public Object speech(@RequestBody Object request) {
        return null;
    }

    @PostMapping("/api/v1/asr/starttask")
    public Object asrTask(@RequestBody Object request) {
        return null;
    }

    @PostMapping("/v1/images/generations")
    public Object generateImage(@RequestBody Object request) {
        return null;
    }
}
