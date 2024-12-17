-- 更新通道的metric数据
-- 输入参数
local channel_id = KEYS[1]
-- 最小完成阈值
local min_completed_threshold = tonumber(ARGV[1])
local error_rate_threshold = tonumber(ARGV[2])  -- 错误率阈值
local http_code = tonumber(ARGV[3])  -- http code
local current_timestamp = tonumber(ARGV[5])  -- 当前时间戳(s)
-- 从第4个参数开始，每两个参数组成一对 metric_name 和 value
local num_metrics = (table.getn(ARGV) - 5) / 2

-- key的前缀
local prefix_key = "bella-openapi-channel-metrics:" .. channel_id

-- 固定的过期时间（60秒）
local EXPIRY_TIME = 60

local last_timestamp

-- 错误处理函数
local function handle_error(err)
    return { "An error occurred: " .. tostring(err) }
end

local function refresh_current_timestamp()
    local timestamps_key = prefix_key .. ":timestamps"
    last_timestamp = redis.call("LINDEX", timestamps_key, -1)
    if not last_timestamp then
        return
    end
    if tonumber(last_timestamp) > current_timestamp then
        current_timestamp = last_timestamp
    end
end

-- 添加新数据
local function add_new_data(metric_name, value)
    local total_key = prefix_key .. ":total"
    local current_metrics_key = prefix_key .. ":" .. metric_name
    if value ~= 0 then
        -- 更新当前时间戳的值
        redis.call("HINCRBY", current_metrics_key, current_timestamp, value)
    end
    -- 更新总量
    redis.call("HINCRBY", total_key, metric_name, value)
end

local function update_last_timeStamp()
    -- 获取最后一个时间戳
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
    local unavailable_time_key = prefix_key .. ":unavailable_time"
    local status = "available"
    if http_code == 429 then
        status = "temporarily_unavailable"
    else
        local total_key = prefix_key .. ":total"
        local total_errors = tonumber(redis.call("HGET", total_key, "errors") or 0)
        local total_completed = tonumber(redis.call("HGET", total_key, "completed") or 0)
        if total_completed >= min_completed_threshold and total_errors * 100 > total_completed * error_rate_threshold then
            status = "temporarily_unavailable"
        end
    end
    redis.call("HSET", current_status_key, current_timestamp, status)
end

-- 主逻辑
local success, result = pcall(function()
    refresh_current_timestamp()
    for i = 1, num_metrics do
        local metric_name = ARGV[i * 2 + 4]
        local value = tonumber(ARGV[i * 2 + 5])
        add_new_data(metric_name, value)
    end
    update_last_timeStamp()
    update_channel_status()
    return { "OK" }
end)

-- 检查是否有错误发生
if not success then
    return handle_error(result)
end

return result
