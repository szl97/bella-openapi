package com.ke.bella.openapi;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.ke.bella.openapi.client.OpenapiClient;
import com.ke.bella.openapi.protocol.files.FileUrl;

@SpringJUnitConfig(TestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "spring.profiles.active=ut" })
@Transactional
public class OpenapiClientTest {
    private List<String> apikeys = Lists.newArrayList("jjhjjnnn", "e7y1uuhi31uhi3", "adadad");

    private final OpenapiClient openapiClient = new OpenapiClient("https://openapi-ait.ke.com");

    @Test
    public void test() {
        for (String apikey : apikeys) {
            openapiClient.validate(apikey);
        }
    }

    public void test1() {
        openapiClient.hasPermission("", "/v1/chat/completions");
        openapiClient.hasPermission("", "/v1/sadad");
        openapiClient.hasPermission("", "/console/adadad");
    }

    @Test
    public void testGetFileUrl() {
        FileUrl fileUrl = openapiClient.getFileUrl("c647706c-0d96-4649-b889-f37ac9324d25", "file-2412291635520022000203-2052459596");
        Assertions.assertNotNull(fileUrl);
        Assertions.assertNotNull(fileUrl.getUrl());
    }

    @Test
    public void testRetrieveFileContent() {
        byte[] bytes = openapiClient.retrieveFileContent("c647706c-0d96-4649-b889-f37ac9324d25", "file-2412291635520022000203-2052459596");
        Assertions.assertNotNull(bytes);
    }
}
