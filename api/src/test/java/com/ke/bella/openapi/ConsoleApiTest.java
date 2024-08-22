package com.ke.bella.openapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.ke.bella.openapi.console.ConsoleContext;
import com.ke.bella.openapi.console.ConsoleContext.Operator;
import com.ke.bella.openapi.intercept.BellaApiResponseAdvice;
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

    private static final List<String> paths = ImmutableList.of("metadata.txt", "apikey.txt");

    @Autowired
    private MockMvc mockMvc;

    private String currentFile = null;

    @BeforeAll
    public static void beforeAll(){
        ConsoleContext.setOperator(Operator.SYS);
    }
    @AfterAll
    public static void afterAll(){
        ConsoleContext.clearAll();
    }

    @Test
    void testApi() throws Exception {
        RequestDef def = new RequestDef();
        for(String path : paths) {
            currentFile = path;
            List<String> lines = readResourceLines(path);
            Map<String, Object> answers = new HashMap<>();
            for(int i = 0; i < lines.size(); i++) {
                if(i % 5 == 0) {
                    String[] strs = lines.get(i).split(" ");
                    def.setMethod(strs[0]);
                    def.setUrl(strs[1]);
                } else if (i % 5 == 1) {
                    def.setHeaders(JacksonUtils.toMap(formatRequest(lines.get(i), answers)));
                } else if(i % 5 == 2) {
                    def.setRequest(formatRequest(lines.get(i), answers));
                } else if(i % 5 == 3) {
                    def.setRes(lines.get(i));
                } else {
                    answers.put(String.valueOf(i-3), requestAndCheck(def, i - 3));
                }
            }
        }
    }

    private String formatRequest(String input, Map<String, Object> answers) {
        String extract = extract(input);
        if(extract == null) {
            return input;
        }
        String replace = "%{"+extract+"}%";
        String[] strs = extract.split("-");
        Object answer = answers.get(strs[0]);
        String text = getText(answer, strs, 1);
        return input.replace(replace, text);
    }

    private String getText(Object answer, String[] strs, int index) {
        if(answer instanceof String) {
            return answer.toString();
        } else if(index == strs.length) {
            return JacksonUtils.serialize(answer);
        } else if (answer instanceof List) {
            List list = (List) answer;
            answer = list.get(Integer.parseInt(strs[index]));
            return getText(answer, strs, ++index);
        } else {
            Map<String, Object> map = (Map<String, Object>) answer;
            answer = map.get(strs[index]);
            return getText(answer, strs, ++index);
        }
    }

    private String extract(String input) {
        String regex = "%\\{([^}]*)}%";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);
        if(matcher.find()) {
            // 提取并打印%{}之间的内容
            return matcher.group(1);
        }
        return null;
    }

    private Object requestAndCheck(RequestDef def, int lines) throws Exception {
        MvcResult mvcResult = mockMvc.perform(requestBuilder(def.method, def.url, def.request, def.headers))
                .andReturn();
        MockHttpServletResponse servletResponse = mvcResult.getResponse();
        BellaApiResponseAdvice.BellaResponse bellaResponse = JacksonUtils.deserialize(servletResponse.getContentAsString(),
                BellaApiResponseAdvice.BellaResponse.class);
        if(bellaResponse == null) {
            throw new RuntimeException(currentFile + " 第"+ lines + "行执行结果不符合预期，不是BellaResponse:" + servletResponse.getContentAsString());
        }
        if(StringUtils.isNumeric(def.res)) {
            Assert.assertEquals(currentFile + " 第"+ lines + "行执行结果不符合预期, res code:" + bellaResponse.getCode(), Integer.valueOf(def.res).intValue(), bellaResponse.getCode());
        } else {
            if(bellaResponse.getData() instanceof Collection) {
                Collection<LinkedHashMap<String, Object>> expected = JacksonUtils.deserialize(def.res, Collection.class);
                Collection<LinkedHashMap<String, Object>> collection = (Collection) bellaResponse.getData();
                for(LinkedHashMap<String, Object> o : expected) {
                    boolean equal = collection.stream().anyMatch(x -> {
                        if(x.keySet().containsAll(o.keySet())) {
                            return x.entrySet().stream().allMatch(e->!o.containsKey(e.getKey()) || e.getValue().equals(o.get(e.getKey())));
                        }
                        return false;
                    });
                    if(!equal) {
                        throw new RuntimeException(currentFile + " 第" + lines + "行执行结果不符合预期, res:" + JacksonUtils.serialize(bellaResponse.getData()));
                    }
                }
            } else {
                if(!def.res.equals("skip")) {
                    Map<String, Object> expected = JacksonUtils.toMap(def.res);
                    Map<String, Object> real = JacksonUtils.toMap(bellaResponse.getData());
                    for (Map.Entry<String, Object> entry : expected.entrySet()) {
                        if(!real.containsKey(entry.getKey())) {
                            throw new RuntimeException(currentFile + " 第" + lines + "行执行结果不符合预期, res:" + JacksonUtils.serialize(def.res));
                        }
                        Object obj = real.get(entry.getKey());
                        Assert.assertEquals(currentFile + " 第" + lines + "行执行结果不符合预期, res:" + JacksonUtils.serialize(def.res), obj,
                                entry.getValue());

                    }
                }
            }
        }
        return bellaResponse.getData();
    }

    private MockHttpServletRequestBuilder requestBuilder(String method, String url, String body, Map<String, Object> headers) {
        MockHttpServletRequestBuilder builder;
        if(method.equals("GET")) {
            builder = get(url);
        } else if(method.equals("POST")) {
            builder = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        } else if(method.equals("PUT")) {
            builder = put(url).contentType(MediaType.APPLICATION_JSON).content(body);
        } else if(method.equals("DELETE")) {
            builder = delete(url);
        } else {
            throw new RuntimeException(currentFile + ": unsupported http method");
        }
        headers.forEach(builder::header);
        return builder;
    }

    @Data
     static class RequestDef {
         String method;
         String url;
         Map<String, Object> headers;
         String request;
         String res;
    }

    private List<String> readResourceLines(String path) {
        try {
            // 第1行 POST/PUT url    GET url    (n+1)%5 = 1
            // 第2行 header          {}         (n+1)%5 = 2
            // 第2行 request         {}         (n+1)%5 = 3
            // 第3行 res code        res        (n+1)%5 = 4
            // 第4行 空格             空格        (n+1)%5 = 0
            File file = new File(Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(path).getFile());
            return FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
