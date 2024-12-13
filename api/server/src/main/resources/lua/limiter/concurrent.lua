-- 记录并发请求数
-- 输入参数
local key = KEYS[1]
local operation = ARGV[1] -- "INCR" or "DECR"

-- key过期时间设置为2分钟，防止出现死锁
local EXPIRY_TIME = 120

-- 错误处理函数
local function handle_error(err)
    return { "An error occurred: " .. tostring(err) }
end

-- 主要逻辑
local success, result = pcall(function()
    local current_count = redis.call(operation, key)
    if current_count < 0 then
        redis.call("DEL", key)
    elseif current_count > 0 then
        -- 设置过期时间
        redis.call("EXPIRE", key, EXPIRY_TIME)
    end
    return current_count or 0
end)

if not success then
    return handle_error(result)
end

return result
