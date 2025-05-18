-- resources/scripts/verify_and_delete.lua
local codeKey = KEYS[1]
local inputCode = ARGV[1]

-- 原子性验证并删除
local storedCode = redis.call("GET", codeKey)
if storedCode == inputCode then
    redis.call("DEL", codeKey)
    return 1
else
    return 0
end