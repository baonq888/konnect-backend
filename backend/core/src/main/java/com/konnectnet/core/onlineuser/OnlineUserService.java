package com.konnectnet.core.onlineuser;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final StringRedisTemplate redisTemplate;
    private static final String ONLINE_USERS_KEY = "online-users";

    public void addUser(String email) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, email);
    }

    public void removeUser(String email) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, email);
    }

    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    public boolean isOnline(String email) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, email));
    }
}
