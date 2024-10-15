-- 更新通道的metric数据
-- 输入参数
local channel_id = KEYS[1]
local min_completed_threshold = tonumber(ARGV[1])  -- 最小完成阈值
local error_rate_threshold = tonumber(ARGV[2])  -- 错误率阈值
local http_code = tonumber(ARGV[3])  -- http code
local channel_unavailable_expire_time = tonumber(ARGV[4])  -- 不可用状态过期时间
-- 从第4个参数开始，每两个参数组成一对 metric_name 和 value
local num_metrics = (table.getn(ARGV) - 4) / 2

-- key的前缀
local prefix_key = "bella-openapi-chanel-metrics:" .. channel_id

-- 获取Redis服务器的当前时间戳
local current_timestamp = redis.call("TIME")[1]

-- 固定的过期时间（60秒）
local EXPIRY_TIME = 60

-- 错误处理函数
local function handle_error(err)
    return { err = "An error occurred: " .. tostring(err) }
end

-- 获取过期时间戳
local function get_expired_timestamps()
    local timestamps_key = prefix_key .. ":timestamps"
    local oldest_allowed = current_timestamp - EXPIRY_TIME
    local expired_timestamps = {}
    while true do
        local oldest_timestamp = redis.call("LINDEX", timestamps_key, 0)
        if not oldest_timestamp or tonumber(oldest_timestamp) > oldest_allowed then
            break
        end
        table.insert(expired_timestamps, oldest_timestamp)
        redis.call("LPOP", timestamps_key)
    end
    return expired_timestamps
end

-- 清理过期数据
local function get_expired_value(expired_timestamps, metric_name)
    local value = 0
    local expired_metrics_key = prefix_key .. ":" .. metric_name
    for _, timestamp in ipairs(expired_timestamps) do
        -- 清理过期的metrics数据
        local expired_metrics = redis.call("HGET", expired_metrics_key, timestamp)
        if expired_metrics then
            value = value + tonumber(expired_metrics)
            redis.call("HDEL", hash_key, timestamp)
        end
    end
    return value
end

-- 添加新数据
local function add_new_data(metric_name, value, expired_value)
    local total_key = prefix_key .. ":total"
    local current_metrics_key = prefix_key .. ":" .. metric_name
    -- 更新当前时间戳的值
    redis.call("HINCRBY", current_metrics_key, current_timestamp, value)
    -- 更新总量
    redis.call("HINCRBY", total_key, metric_name, value - expired_value)
end

local function update_last_timeStamp()
    -- 获取最后一个时间戳
    local last_timestamp = redis.call("LINDEX", list_key, -1)
    if not last_timestamp or tonumber(last_timestamp) ~= current_timestamp then
        -- 添加新的时间戳
        local timestamps_key = prefix_key .. ":timestamps"
        -- 添加新的时间戳
        redis.call("RPUSH", timestamps_key, current_timestamp)
    end
end

-- 更新通道状态
local function update_channel_status()
    local mark_key = prefix_key .. ":unavailable"
    local current_status_key = prefix_key .. ":status"
    if http_code == 429 then
        redis.call("HSET", current_status_key, current_timestamp, "temporarily_unavailable")
        redis.call("SETEX", mark_key, channel_unavailable_expire_time, "true")
        return
    end
    local total_errors = tonumber(redis.call("HGET", total_key, "errors") or 0)
    local total_completed = tonumber(redis.call("HGET", total_key, "completed") or 0)

    local status = "available"
    if total_completed >= min_completed_threshold and total_errors > total_completed * error_rate_threshold then
        status = "temporarily_unavailable"
        redis.call("SETEX", mark_key, channel_unavailable_expire_time, "true")
    end
    redis.call("HSET", current_status_key, current_timestamp, status)
end

-- 主逻辑
local success, result = pcall(function()
    local expired_timestamps = get_expired_timestamps()
    for i = 1, num_metrics do
        local metric_name = ARGV[i * 2 + 3]
        local value = tonumber(ARGV[i * 2 + 4])
        local expired_value = get_expired_value(expired_timestamps, metric_name)
        add_new_data(metric_name, value, expired_value)
    end
    update_last_timeStamp()
    update_channel_status()
    return { status = "OK" }
end)

-- 检查是否有错误发生
if not success then
    return handle_error(result)
end

return result
