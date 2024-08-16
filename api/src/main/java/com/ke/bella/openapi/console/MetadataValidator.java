package com.ke.bella.openapi.console;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ke.bella.openapi.utils.JacksonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.db.TableConstants.CHANNEL_PRIORITY;
import static com.ke.bella.openapi.db.TableConstants.DATA_DESTINATIONS;
import static com.ke.bella.openapi.db.TableConstants.ENDPOINT;
import static com.ke.bella.openapi.db.TableConstants.MODEL;
import static com.ke.bella.openapi.db.TableConstants.ModelJsonKey;
import static com.ke.bella.openapi.db.TableConstants.SystemBasicCategory;
import static com.ke.bella.openapi.db.TableConstants.SystemBasicEndpoint;
import static com.ke.bella.openapi.utils.MatchUtils.isAllText;
import static com.ke.bella.openapi.utils.MatchUtils.isBracesWithSpaces;
import static com.ke.bella.openapi.utils.MatchUtils.isTextStart;
import static com.ke.bella.openapi.utils.MatchUtils.isValidURL;

public class MetadataValidator {
    private static final LoadingCache<String, Pattern> baseEndpointPatternCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Pattern>() {
                @Override
                public Pattern load(String key) {
                    String regex = StringUtils.replace(key, "*", "\\d+");
                    return Pattern.compile(regex);
                }
            });

    public static void checkEndpointOp(MetaDataOps.EndpointOp op, boolean create) {
        Assert.hasText(op.getEndpoint(), "能力点path不可为空");
        boolean systemEndpoint = Arrays.stream(SystemBasicEndpoint.values())
                .map(SystemBasicEndpoint::getEndpoint)
                .anyMatch(path -> matchPath(path, op.getEndpoint()));
        if(create) {
            Assert.isTrue(!systemEndpoint, "不可创建系统endpoint");
            Assert.isTrue(op.getEndpoint().matches(".*/v\\d+/.*"), "path格式必须为*/v{d}/*");
            Assert.isTrue(!op.getEndpoint().contains("_"), "能力点path不能包含下划线");
            Assert.isTrue(op.getEndpoint().length() <= 60, "能力点path长度不能超过60");
            Assert.hasText(op.getEndpointName(), "能力点名字不可为空");
            Assert.hasText(op.getMaintainerCode(), "维护人系统号不可为空");
            Assert.hasText(op.getMaintainerName(), "维护人名字不可为空");
        } else {
            if(systemEndpoint) {
                op.setEndpointName(null);
            }
            Assert.isTrue(StringUtils.isNotBlank(op.getEndpointName())
                    || StringUtils.isNotBlank(op.getMaintainerCode())
                    || StringUtils.isNotBlank(op.getMaintainerName()), "可修改字段全部为空，无法修改");
        }
        if(StringUtils.isNotEmpty(op.getEndpointName())) {
            Assert.isTrue(isTextStart(op.getEndpointName()), "能力点名称不可以数字/特殊字符/空格开头");
            Assert.isTrue(op.getEndpointName().length() <= 10, "能力点名称长度不能超过10");
        }
        if(StringUtils.isNotEmpty(op.getMaintainerCode())) {
            Assert.isTrue(StringUtils.isNumeric(op.getMaintainerCode()), "系统号必须为数字");
        }
        if(StringUtils.isNotEmpty(op.getMaintainerName())) {
            Assert.isTrue(isAllText(op.getMaintainerName()), "姓名不可以包含数字/特殊字符/空格开头");
        }
    }

    public static void checkEndpointStatusOp(MetaDataOps.EndpointStatusOp op) {
        Assert.isTrue(Arrays.stream(SystemBasicEndpoint.values()).map(SystemBasicEndpoint::getEndpoint)
                .noneMatch(path -> matchPath(path, op.getEndpoint())), "不可修改系统endpoint");
        Assert.hasText(op.getEndpoint(), "能力点path不可为空");
    }

    public static void checkModelOp(MetaDataOps.ModelOp op, boolean create) {
        Assert.hasText(op.getModelName(), "模型名称不可为空");
        if(create) {
            Assert.isTrue(isTextStart(op.getModelName()), "模型名不可以数字/特殊字符/空格开头");
            Assert.notEmpty(op.getEndpoints(), "能力点不可为空");
            Assert.isTrue(op.getModelName().length() <= 60, "模型名长度不能超过60");
            Assert.hasText(op.getProperties(), "模型属性不能为空");
            Assert.hasText(op.getFeatures(), "模型特性不能为空");
        } else {
            Assert.isTrue(CollectionUtils.isNotEmpty(op.getEndpoints())
                    || StringUtils.isNotBlank(op.getDocumentUrl())
                    || StringUtils.isNotBlank(op.getProperties())
                    || StringUtils.isNotBlank(op.getFeatures()), "可修改字段全部为空，无法修改");
        }
        if(CollectionUtils.isNotEmpty(op.getEndpoints())) {
            boolean hasBasicEndpoint = op.getEndpoints().stream().anyMatch(path ->
                    Arrays.stream(SystemBasicEndpoint.values())
                            .map(SystemBasicEndpoint::getEndpoint)
                            .anyMatch(match -> matchPath(match, path)));
            Assert.isTrue(hasBasicEndpoint, "模型必须有一个基本能力点，基本能力点如下：\r\n" +
                    String.join(",", Arrays.stream(SystemBasicEndpoint.values())
                            .map(SystemBasicEndpoint::getEndpoint).collect(Collectors.toList())));
        }
    }

    public static void checkModelNameOp(MetaDataOps.ModelNameOp op) {
        Assert.hasText(op.getModelName(), "模型名不可为空");
    }

    public static void checkChannelCreateOp(MetaDataOps.ChannelCreateOp op) {
        Assert.hasText(op.getEntityType(), "实体类型不可为空");
        Assert.hasText(op.getEntityCode(), "实体编码不可为空");
        if(op.getEntityType().equals(MODEL)) {
            Assert.hasText(op.getChannelInfo(), "模型通道的通道信息不可为空");
            Assert.hasText(op.getPriceInfo(), "模型通道的单价信息不可为空");
        }
        Assert.isTrue(op.getEntityCode().equals(ENDPOINT) || op.getEntityType().equals(MODEL),
                "实体类型只能是endpoint或model");
        Assert.hasText(op.getProtocol(), "请求协议不可为空");
        Assert.hasText(op.getSupplier(), "供应商不可为空");
        Assert.hasText(op.getUrl(), "url不可为空");
        Assert.isTrue(DATA_DESTINATIONS.contains(op.getDataDestination()),
                "通道的数据流向只能是：" + String.join("或", DATA_DESTINATIONS));
        Assert.isTrue(CHANNEL_PRIORITY.contains(op.getPriority()),
                "通道的优先级只能是：" + String.join("或", CHANNEL_PRIORITY));
        Assert.hasText(op.getProtocol(), "请求协议不可为空字符串");
        Assert.hasText(op.getSupplier(), "供应商不可为空字符串");
        Assert.isTrue(isValidURL(op.getUrl()), "url必须以http://或https://开头");
        checkJsonInfo(op.getChannelInfo());
        checkJsonInfo(op.getPriceInfo());
    }

    public static void checkChannelUpdateOp(MetaDataOps.ChannelUpdateOp op) {
        Assert.hasText(op.getChannelCode(), "通道编码不可为空");
        Assert.isTrue(StringUtils.isNotBlank(op.getPriority())
                || StringUtils.isNotBlank(op.getChannelInfo())
                || StringUtils.isNotBlank(op.getPriceInfo()), "可修改字段全部为空，无法修改");
        if(StringUtils.isNotEmpty(op.getPriority())) {
            Assert.isTrue(CHANNEL_PRIORITY.contains(op.getPriority()),
                    "通道的优先级只能是：" + String.join("或", CHANNEL_PRIORITY));
        }
        checkJsonInfo(op.getChannelInfo());
        checkJsonInfo(op.getPriceInfo());
    }

    private static void checkJsonInfo(String info) {
        //只检查是否是json，其他信息在service中根据类型判断
        if(StringUtils.isNotEmpty(info)) {
            Map<String, Object> map = json2Map(info);
            Assert.isTrue(map == null || !map.isEmpty(), "信息非json格式");
        }
    }

    public static void checkChannelStatusOp(MetaDataOps.ChannelStatusOp op) {
        Assert.hasText(op.getChannelCode(), "通道编码不可为空");
    }

    public static void checkCategoryCreateOp(MetaDataOps.CategoryCreateOp op) {
        if(op.getParentCode() != null) {
            Assert.hasText(op.getParentCode(), "父类目编码不可为空字符串");
        }
        Assert.isTrue(Arrays.stream(SystemBasicCategory.values())
                        .noneMatch(x -> {
                            String systemParentCode = x.getParent() == null ? "" : x.getParent().getCode();
                            String parentCode = op.getParentCode() == null ? "" : op.getParentCode();
                            return parentCode.equals(systemParentCode) && op.getCategoryName().equals(x.getName());
                        }),
                "不可创建系统类目");
        Assert.hasText(op.getCategoryName(), "类目名称不可为空");
        Assert.isTrue(op.getCategoryName().length() <= 10, "类目名称长度不可超过10");
        Assert.isTrue(isTextStart(op.getCategoryName()), "类目名称必须以文字开头");
    }

    public static void checkCategoryStatus(MetaDataOps.CategoryStatusOp op) {
        Assert.hasText(op.getCategoryCode(), "类目编码不可为空");
    }

    public static void checkEndpointCategoryOp(MetaDataOps.EndpointCategoriesOp op) {
        Assert.hasText(op.getEndpoint(), "能力点path不可为空");
        Assert.notEmpty(op.getCategoryCodes(), "类目编码不可为空");
        Assert.isTrue(Arrays.stream(SystemBasicEndpoint.values())
                        .noneMatch(endpoint -> matchPath(endpoint.getEndpoint(), op.getEndpoint())
                                && op.getCategoryCodes().contains(endpoint.getCategory().getCode())),
                "不可修改系统默认的能力点类目");
    }

    public static void checkReplaceEndpointCategoryOp(MetaDataOps.EndpointCategoriesOp op) {
        Assert.hasText(op.getEndpoint(), "能力点path不可为空");
        if(CollectionUtils.isNotEmpty(op.getCategoryCodes())) {
            op.getCategoryCodes().forEach(code -> Assert.hasText(code, "类目编码不可为空"));
        }
    }

    /**
     * 检查路径是否匹配
     *
     * @param match 要匹配的路径
     * @param path  要检查的理解
     *
     * @return 匹配返回true，不匹配返回false
     */
    public static boolean matchPath(String match, String path) {
        try {
            Matcher matcher = baseEndpointPatternCache.get(match).matcher(path);
            return matcher.matches();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> json2Map(String jsonStr) {
        if(isBracesWithSpaces(jsonStr)) {
            return null;
        }
        return JacksonUtils.toMap(jsonStr);
    }

    /**
     * 检查json是否符合要求
     *
     * @param map      jsonMap
     * @param endpoint 能力点，对应 {@link ModelJsonKey} 中的endpoint
     * @param field    model实体的字段名，对应 {@link ModelJsonKey} 中的field
     *
     * @return 如果存在不符合要求的key，返回不合法的提示，否则返回null
     */
    public static String generateInvalidModelJsonKeyMessage(Map<String, Object> map, String endpoint, String field) {
        List<ModelJsonKey> keys = Arrays.stream(ModelJsonKey.values())
                .filter(key -> key.getFied().equals(field) && matchPath(key.getEndpoint(), endpoint))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(keys)) {
            return null;
        }
        List<String> notExists = keys.stream().map(ModelJsonKey::getCode).filter(key -> !map.containsKey(key)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(notExists)) {
            String codes = String.join(",", notExists);
            return field + "中缺少：" + codes;
        }
        List<ModelJsonKey> invalidKeys = keys.stream().filter(key -> !map.get(key.getCode()).getClass().equals(key.getType()))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(invalidKeys)) {
            StringBuilder sb = new StringBuilder(field).append("中以下内容不符合规范：\r\n");
            invalidKeys.forEach(key -> sb.append(key.getCode())
                    .append("的值类型为：")
                    .append(key.getType().getSimpleName())
                    .append(", "));
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return null;
    }
}
