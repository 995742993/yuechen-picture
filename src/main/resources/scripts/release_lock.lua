-- resources/scripts/release_lock.lua
local lockKey = KEYS[1]
local lockValue = ARGV[1]

-- 只有锁的值匹配时才删除
if redis.call("GET", lockKey) == lockValue then
    return redis.call("DEL", lockKey)
else
    return 0
end