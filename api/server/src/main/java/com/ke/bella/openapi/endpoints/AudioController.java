package com.ke.bella.openapi.endpoints;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ke.bella.openapi.annotations.EndpointAPI;

@EndpointAPI
@RestController
@RequestMapping("/v1/audio")
@Tag(name = "audio能力点")
public class AudioController {

}
