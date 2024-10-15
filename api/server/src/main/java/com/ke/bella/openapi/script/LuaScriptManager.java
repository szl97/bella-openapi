package com.ke.bella.openapi.script;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import org.apache.commons.io.FileUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
public class LuaScriptManager {
    private static final String luaDir = "classpath:/lua/";
    private static final String luaScriptsKey = "luaScripts:";
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() throws IOException {
        QuickConfig config = QuickConfig.newBuilder(luaScriptsKey)
                .cacheNullValue(true)
                .cacheType(CacheType.BOTH)
                .expire(Duration.of(0, ChronoUnit.MILLIS))
                .localExpire(Duration.of(0, ChronoUnit.MILLIS))
                .localLimit(100)
                .penetrationProtect(false)
                .build();
        cacheManager.getOrCreateCache(config);
    }

    private String doLoad(String fileName) throws IOException {
        Resource resource = resourceLoader.getResource(luaDir + fileName);
        String scriptContent = FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
        return redissonClient.getScript().scriptLoad(scriptContent);
    }

    @Cached(name = luaScriptsKey, key = "#scriptName")
    public String getScriptSha(String scriptName) throws IOException {
        return doLoad(scriptName + ".lua");
    }

    public String reloadScript(String scriptName) throws IOException {
        String sha = doLoad(scriptName + ".lua");
        cacheManager.getCache(luaScriptsKey).put(scriptName, sha);
        return sha;
    }
}
