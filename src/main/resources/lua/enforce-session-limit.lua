-- Atomic session limit enforcement
local userTokensKey = KEYS[1]
local tokenPrefix = ARGV[1]
local reversePrefix = ARGV[2]
local maxSessions = tonumber(ARGV[3]) or 0

-- Get current session count
local currentCount = redis.call('SCARD', userTokensKey) or 0

-- Normalize to number (safe conversion)
currentCount = tonumber(currentCount) or 0

-- If limit exceeded, revoke oldest token
if currentCount >= maxSessions then
    local oldestTokens = redis.call('SPOP', userTokensKey, 1)

    if oldestTokens and type(oldestTokens) == "table" and #oldestTokens > 0 then
        local oldestToken = oldestTokens[1]

        redis.call('DEL', tokenPrefix .. oldestToken)
        redis.call('DEL', reversePrefix .. oldestToken)

        return {1, oldestToken}
    end
end

return {0, ''}