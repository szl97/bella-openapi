package com.ke.bella.openapi;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.client.OpenapiClient;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import com.ke.bella.openapi.protocol.files.File;
import com.ke.bella.openapi.protocol.files.FileUrl;
import com.ke.bella.openapi.utils.FileUtils;

@SpringJUnitConfig(TestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "spring.profiles.active=ut" })
@Transactional
public class OpenapiClientTest {

    private static final String API_KEY = "c647706c-0d96-4649-b889-f37ac9324d25";
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
        FileUrl fileUrl = openapiClient.getFileUrl(API_KEY, "file-2412291635520022000203-2052459596");
        Assertions.assertNotNull(fileUrl);
        Assertions.assertNotNull(fileUrl.getUrl());
    }

    @Test
    public void testRetrieveFileContent() {
        byte[] bytes = openapiClient.retrieveFileContent(API_KEY, "file-2412291635520022000203-2052459596");
        Assertions.assertNotNull(bytes);
    }

    @Test
    public void testGetFileWithMetadata() {
        File file = openapiClient.getFile(API_KEY, "file-2501081157090026000017-2075695711");
        Assertions.assertNotNull(file);
        Assertions.assertNotNull(file.getMimeType());
        Assertions.assertNotNull(file.getType());
    }

    @Test
    public void testListFiles() {
        List<File> files = openapiClient.listFiles(API_KEY);
        Assertions.assertNotNull(files);
        Assertions.assertTrue(files.stream().anyMatch(e -> e.getPurpose().equals("assistants")));
        Assertions.assertTrue(files.stream().anyMatch(e -> e.getPurpose().equals("vision")));
        Assertions.assertTrue(files.size() > 1);

        List<File> filesLimit = openapiClient.listFiles(API_KEY, null, 1, null, null);
        Assertions.assertNotNull(filesLimit);
        Assertions.assertEquals(1, filesLimit.size());

        List<File> filesVision = openapiClient.listFiles(API_KEY, "vision", 1, null, null);
        Assertions.assertNotNull(filesLimit);
        Assertions.assertTrue(filesVision.stream().allMatch(e -> e.getPurpose().equals("vision")));
    }

    @Test
    public void testUploadFileAndDelete() throws FileNotFoundException {
        String filename = "upload_test.jpg";
        java.io.File file = new java.io.File("src/test/resources/" + filename);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            File vision = openapiClient.uploadFile(API_KEY, "vision", inputStream, filename);
            Assertions.assertNotNull(vision);
            File deleting = openapiClient.deleteFile(API_KEY, vision.getId());
            Assertions.assertNotNull(deleting);
            Assertions.assertThrows(IllegalStateException.class, () -> openapiClient.getFile(API_KEY, vision.getId()));
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Test
    public void testUploadFileWithUnknownMimeType() throws FileNotFoundException {
        String filename = "test_upload_unknown_mime_type.jsonl";
        java.io.File file = new java.io.File("src/test/resources/" + filename);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] bytes = FileUtils.readAllBytes(inputStream);

            Assertions.assertDoesNotThrow(() -> openapiClient.uploadFile(API_KEY, "vision", bytes, filename));
            File vision = openapiClient.uploadFile(API_KEY, "vision", bytes, filename);
            Assertions.assertNotNull(vision);

        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Test
    public void testListFilesByFileIds() {
        String firstId = "file-2501081157090026000017-2075695711";
        String secondId = "file-2501101804270027000104-2075695711";
        List<File> files = openapiClient.listFiles(API_KEY, Lists.newArrayList(firstId, secondId));
        Assertions.assertNotNull(files);
        Assertions.assertEquals(2, files.size());
        Assertions.assertEquals(files.get(0).getId(), firstId);
        Assertions.assertEquals(files.get(1).getId(), secondId);
    }

    @Test
    public void testGetPreviewUrl() {
        String fileId = "file-2412291635520022000203-2052459596";
        String url = openapiClient.getPreviewUrl(API_KEY, fileId);
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.startsWith("http"));
    }

    @Test
    public void testGetPreviewUrlWithInvalidFileId() {
        String fileId = "file-2501101754410023000092-2075695711";
        String url = openapiClient.getPreviewUrl(API_KEY, fileId);
        Assertions.assertNotNull(url);
    }
}
