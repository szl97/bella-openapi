package com.ke.bella.openapi.utils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;

public class FileUtils {

    private static final Map<String, String> MIME_TO_EXTENSION = new HashMap<>();
    static {
        // 初始化MIME类型到文件扩展名的映射
        MIME_TO_EXTENSION.put("text/html", "html");
        MIME_TO_EXTENSION.put("text/css", "css");
        MIME_TO_EXTENSION.put("text/javascript", "js");
        MIME_TO_EXTENSION.put("image/jpg", "jpg");
        MIME_TO_EXTENSION.put("image/jpeg", "jpg");
        MIME_TO_EXTENSION.put("image/png", "png");
        MIME_TO_EXTENSION.put("image/gif", "gif");
        MIME_TO_EXTENSION.put("image/webp", "webp");
        MIME_TO_EXTENSION.put("image/svg+xml", "svg");
        MIME_TO_EXTENSION.put("application/pdf", "pdf");
        MIME_TO_EXTENSION.put("application/zip", "zip");
        MIME_TO_EXTENSION.put("audio/mpeg", "mp3");
        MIME_TO_EXTENSION.put("audio/mp3", "mp3");
        MIME_TO_EXTENSION.put("audio/mp4", "m4a");
        MIME_TO_EXTENSION.put("audio/wav", "wav");
        MIME_TO_EXTENSION.put("audio/webm", "webm");
        MIME_TO_EXTENSION.put("audio/amr", "amr");
        MIME_TO_EXTENSION.put("video/mp4", "mp4");
        MIME_TO_EXTENSION.put("video/quicktime", "mov");
        MIME_TO_EXTENSION.put("video/mpeg", "mpeg");

        // Microsoft Word文档
        MIME_TO_EXTENSION.put("application/msword", "doc");
        MIME_TO_EXTENSION.put("application/json", "json");
        MIME_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

        // Microsoft Excel电子表格
        MIME_TO_EXTENSION.put("application/vnd.ms-excel", "xls");
        MIME_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

        MIME_TO_EXTENSION.put("application/vnd.ms-powerpoint", "ppt");
        MIME_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");

        MIME_TO_EXTENSION.put("text/plain", "txt");
        MIME_TO_EXTENSION.put("text/markdown", "md");
        MIME_TO_EXTENSION.put("text/csv", "csv");

        MIME_TO_EXTENSION.put("application/xml", "xml");
        MIME_TO_EXTENSION.put("application/epub+zip", "epub");
        MIME_TO_EXTENSION.put("message/rfc822", "eml");
        MIME_TO_EXTENSION.put("application/vnd.ms-outlook", "msg");
        // 可以根据需要添加更多的MIME类型
    }

    public static MediaType getMediaType(String contentType) {
        return MediaType.parse(contentType);
    }

    public static String getExtensionFromMimeType(MediaType mediaType) {
        String pure = extraPureMediaType(mediaType);
        return MIME_TO_EXTENSION.getOrDefault(pure.toLowerCase(), "bin");
    }

    /**
     * such as "text", "image", "audio", "video", or "application".
     *
     * @return
     */
    public static String getType(String mediaType) {
        MediaType MediaTypeFiltered = MediaType.parse(mediaType);
        return getType(MediaTypeFiltered);
    }

    public static String getType(MediaType mediaType) {
        String t = extraPureMediaType(mediaType).toLowerCase();
        if(t.startsWith("image")) {
            return "image";
        } else if(t.startsWith("audio")) {
            return "audio";
        } else if(t.startsWith("video")) {
            return "video";
        } else if(t.startsWith("text")) {
            return "text";
        } else {
            return "binary";
        }
    }

    public static String getSubType(MediaType mediaType) {
        if(mediaType == null) {
            return null;
        }
        return mediaType.subtype();
    }

    private static String extraPureMediaType(MediaType mediaType) {
        return mediaType.type() + "/" + mediaType.subtype();
    }

}
