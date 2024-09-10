package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import com.ke.bella.openapi.utils.JacksonUtils;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CompletionLogHandler implements EndpointLogHandler {
    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    @Override
    public void process(EndpointProcessData processData) {
        long startTime = processData.getRequestTime();
        CompletionResponse response = (CompletionResponse) processData.getResponse();
        long firstPackageTime = processData.getFirstPackageTime() == 0L ? response.getCreated()
                : processData.getFirstPackageTime();
        processData.setMetrics(countMetrics(startTime, response.getCreated(), firstPackageTime));
        CompletionRequest request = (CompletionRequest) processData.getRequest();
        String encodingType = JacksonUtils.deserialize(processData.getChannelInfo(), CompletionAdaptor.CompletionProperty.class).getEncodingType();
        processData.setUsage(countTokenUsage(request, response, encodingType));
    }

    @Override
    public String endpoint() {
        return "/v1/chat/completions";
    }

    private Map<String, Object> countMetrics(long startTime, long endTime, long firstPackageTime) {
        return ImmutableMap.of("ttft", firstPackageTime - startTime, "ttlt", endTime - startTime);
    }

    private CompletionResponse.TokenUsage countTokenUsage(CompletionRequest request, CompletionResponse response, String encodingType) {
        if(response.getUsage() != null) {
            return response.getUsage();
        }
        EncodingType encoding = EncodingType.fromName(encodingType).orElse(EncodingType.CL100K_BASE);
        //计费模型请求消耗量
        //计算非userMessage的token用量
        int requestToken = 0;
        List<String> textMessage = new LinkedList<>();
        List<Pair<String, Boolean>> imgMessage = new LinkedList<>();
        if(request.getMessages() != null) {
            for (Message message : request.getMessages()) {
                if(CollectionUtils.isNotEmpty(message.getTool_calls())) {
                    textMessage.addAll(getToolCallStr(message.getTool_calls()));
                } else {
                    //如果message.getContent()是String类型
                    if(message.getContent() instanceof String) {
                        textMessage.add((String) message.getContent());
                    } else if(message.getContent() instanceof java.util.List) {
                        for (Map content : (java.util.List<Map>) message.getContent()) {
                            if(content.containsKey("text")) {
                                textMessage.add((String) content.get("text"));
                            } else if(content.containsKey("image_url")) {
                                //如果包含类型为string的image_url
                                if(content.get("image_url") instanceof String) {
                                    imgMessage.add(Pair.of((String) content.get("image_url"), false));
                                } else if(content.get("image_url") instanceof Map) {
                                    String url = (String) ((Map) content.get("image_url")).get("url");
                                    boolean lowResolution = "low".equals(((Map) content.get("image_url")).get("detail"));
                                    imgMessage.add(Pair.of(url, lowResolution));
                                }
                            }
                        }
                    }
                }
            }
        }
        Optional<Integer> userTextMessageToken = textMessage.stream().map(x -> tokenCount(x, encoding)).reduce(Integer::sum);
        Optional<Integer> userImgMessageToken = imgMessage.stream().map(x-> imageToken(x.getLeft(), x.getRight())).reduce(Integer::sum);
        requestToken += userTextMessageToken.orElse(0) + userImgMessageToken.orElse(0);

        int responseToken = (response.getChoices() == null) ? 0 : response.getChoices().stream()
                .map(x -> {
                    if(CollectionUtils.isNotEmpty(x.getMessage().getTool_calls())) {
                        return getToolCallStr(x.getMessage().getTool_calls());
                    } else {
                        return Lists.newArrayList(x.getMessage().getContent());
                    }
                }).flatMap(List::stream)
                .map(String.class::cast)
                .map(x -> tokenCount(x, encoding)).reduce(Integer::sum).orElse(0);
        CompletionResponse.TokenUsage tokenUsage = new CompletionResponse.TokenUsage();
        tokenUsage.setPrompt_tokens(requestToken);
        tokenUsage.setCompletion_tokens(responseToken);
        tokenUsage.setTotal_tokens(requestToken + responseToken);
        return tokenUsage;
    }

    private List<String> getToolCallStr(List<Message.ToolCall> toolCalls) {
        return toolCalls.stream()
                .map(t->getFunctionStr(t.getFunction()))
                .collect(Collectors.toList());
    }
    private String getFunctionStr(Message.FunctionCall functionCall) {
        return functionCall.getName() == null ? functionCall.getArguments() :
                functionCall.getName() + functionCall.getArguments();
    }

    /**
     * 不同模型的encodingType:
     * @see com.knuddels.jtokkit.api.ModelType
     * @param text
     * @param encodingType
     * @return
     */
    private int tokenCount(String text, EncodingType encodingType) {
        Encoding encoding = registry.getEncoding(encodingType);
        return encoding.countTokens(text);
    }

    private int imageToken (String imageStr, boolean lowResolution) {
        //gpt-4-vision模型，imageToken计算
        //!!! 注意，其他模型的imageToken计算方式可能不同，需要单独实现
        // 2024.05.23补充：4o token计算相同
        Pair<Integer, Integer> imgSize = null;
        if(imageStr.startsWith("http")) {
            imgSize = imageSizeFromUrl(imageStr);
        } else {
            imgSize = imageSizeFromB64(imageStr);
        }
        return imageToken(imgSize.getLeft(), imgSize.getRight(), lowResolution);
    }

    private static Pair<Integer, Integer> imageSizeFromB64(String base64DataUrl) {
        //从base64DataUrl中提取base64Str：data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAyA...
        String base64Str = base64DataUrl.split(",")[1];
        //将base64转化为InputStream
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
        try (InputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
            //读取图片url，获取像素宽度，长度
            Image image = ImageIO.read(inputStream);
            // 获取图片的宽度
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            return Pair.of(width, height);
        } catch (Exception e) {
            return Pair.of(0, 0);
        }
    }

    private Pair<Integer, Integer> imageSizeFromUrl(String imageUrl) {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            //读取图片url，获取像素宽度，长度
            Image image = ImageIO.read(inputStream);
            // 获取图片的宽度
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            return Pair.of(width, height);
        } catch (Exception e) {
            return Pair.of(0, 0);
        }
    }

    private int imageToken(double widthpx, double heightpx, boolean lowResolution) {
        if (lowResolution) {
            //如果图片使用低分辨率处理，则图像统一被压缩到512*512范围内，占用token：85
            return 85;
        } else {
            //如果使用高分辨率处理，则首先使用压缩到512*512范围内的低分辨率进行图像处理，之后切割处理
            return imageTiles(widthpx, heightpx) * 170 + 85;
        }
    }

    /**
     * 计算图像被拆分的单元格
     *
     * @param widthpx
     * @param heightpx
     * @return
     */
    private int imageTiles(double widthpx, double heightpx) {
        double max = Math.max(widthpx, heightpx);
        //将图像缩放到2048*2048范围内
        if (max > 2048) {
            double scale = 2048 / max;
            widthpx = widthpx * scale;
            heightpx = heightpx * scale;
        }
        //将图像短边缩放到不超过768
        double min = Math.min(widthpx, heightpx);
        if (min > 768) {
            double scale = 768 / min;
            widthpx = widthpx * scale;
            heightpx = heightpx * scale;
        }
        //将图像切割为512*512单元格
        return (int) (Math.ceil(widthpx / 512) * Math.ceil(heightpx / 512));
    }
}
