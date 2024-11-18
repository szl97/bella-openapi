package com.ke.bella.openapi;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.ke.bella.openapi.client.OpenapiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringJUnitConfig(TestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "spring.profiles.active=ut"})
@Transactional
public class OpenapiClientTest {
    private List<String> apikeys = Lists.newArrayList("jjhjjnnn", "e7y1uuhi31uhi3", "adadad");

    private final OpenapiClient openapiClient = new OpenapiClient("https://openapi-ait.ke.com");

    @Test
    public void test() {
        for(String apikey : apikeys) {
            openapiClient.validate(apikey);
        }
    }

    public void test1() {
        openapiClient.hasPermission("", "/v1/chat/completions");
        openapiClient.hasPermission("", "/v1/sadad");
        openapiClient.hasPermission("", "/console/adadad");
    }
}
