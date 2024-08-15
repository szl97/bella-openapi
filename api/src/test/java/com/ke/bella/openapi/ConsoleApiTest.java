package com.ke.bella.openapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.ke.bella.openapi.BellaContext.Operator;
import com.ke.bella.openapi.api.BellaResponseAdvice;
import com.ke.bella.openapi.utils.JacksonUtils;

import lombok.Data;

/**
 * Author: Stan Sai Date: 2024/8/8 17:56 description:
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringJUnitConfig(TestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "spring.profiles.active=dev"})
@AutoConfigureMockMvc
@Transactional
public class ConsoleApiTest {

    private static final List<String> paths = ImmutableList.of("metadata.txt");

    @Autowired
    private MockMvc mockMvc;

    private String currentFile = null;

    @BeforeAll
    public static void beforeAll(){
        BellaContext.setOperator(Operator.SYS);
    }
    @AfterAll
    public static void afterAll(){
        BellaContext.clearAll();
    }

    @Test
    void testApi() throws Exception {
        RequestDef def = new RequestDef();
        for(String path : paths) {
            currentFile = path;
            List<String> lines = readResourceLines(path);
            for(int i = 0; i < lines.size(); i++) {
                if(i % 4 == 0) {
                    String[] strs = lines.get(i).split(" ");
                    def.setMethod(strs[0]);
                    def.setUrl(strs[1]);
                } else if(i % 4 == 1) {
                    def.setRequest(lines.get(i));
                } else if(i % 4 == 2) {
                    def.setRes(lines.get(i));
                } else {
                    requestAndCheck(def, i - 2);
                }
            }
        }
    }

    private void requestAndCheck(RequestDef def, int lines) throws Exception {
        MvcResult mvcResult = mockMvc.perform(requestBuilder(def.method, def.url, def.request))
                .andReturn();
        MockHttpServletResponse servletResponse = mvcResult.getResponse();
        BellaResponseAdvice.BellaResponse bellaResponse = JacksonUtils.deserialize(servletResponse.getContentAsString(), BellaResponseAdvice.BellaResponse.class);
        if(bellaResponse == null) {
            throw new RuntimeException(currentFile + " 第"+ lines + "行执行结果不符合预期，不是BellaResponse:" + servletResponse.getContentAsString());
        }
        if(StringUtils.isNumeric(def.res)) {
            Assert.assertEquals(currentFile + " 第"+ lines + "行执行结果不符合预期, res code:" + bellaResponse.getCode(), Integer.valueOf(def.res).intValue(), bellaResponse.getCode());
        } else {
            Map<String, Object> expected = JacksonUtils.toMap(def.res);
            Map<String, Object> real = JacksonUtils.toMap(bellaResponse.getData());
            for(Map.Entry<String, Object> entry : expected.entrySet()) {
                if(!real.containsKey(entry.getKey())) {
                    throw new RuntimeException(currentFile + " 第"+ lines + "行执行结果不符合预期, res:"+JacksonUtils.serialize(def.res));
                }
                Object obj = real.get(entry.getKey());
                Assert.assertEquals(currentFile + " 第"+ lines + "行执行结果不符合预期, res:"+JacksonUtils.serialize(def.res), obj, entry.getValue());

            }
        }
    }

    private MockHttpServletRequestBuilder requestBuilder(String method, String url, String body) {
        if(method.equals("GET")) {
            return get(url);
        } else if(method.equals("POST")) {
            return post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        } else if(method.equals("PUT")) {
            return put(url).contentType(MediaType.APPLICATION_JSON).content(body);
        } else if(method.equals("DELETE")) {
            return delete(url);
        }
        throw new RuntimeException(currentFile + ": unsupported http method");
    }

    @Data
     static class RequestDef {
         String method;
         String url;
         String request;
         String res;
    }

    private List<String> readResourceLines(String path) {
        try {
            // 第1行 POST/PUT url    GET url    (n+1)%4 = 1
            // 第2行 request         {}         (n+1)%4 = 2
            // 第3行 res code        res        (n+1)%4 = 3
            // 第4行 空格             空格        (n+1)%4 = 0
            File file = new File(Thread.currentThread()
                    .getContextClassLoader()
                    .getResource("metadata.txt").getFile());
            return FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
