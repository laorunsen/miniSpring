package com.simulator.redis;

import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class RedisServerTest {

    private static final int PORT = 6379;
    private static ExecutorService executor;

    @BeforeAll
    public static void startRedisServer() throws Exception {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                new RedisServer().start(PORT);
            } catch (Exception e) {
                throw new RuntimeException("Failed to start RedisServer", e);
            }
        });

        Thread.sleep(1000); // 给服务一点启动时间
    }

    @AfterAll
    public static void shutdown() {
        executor.shutdownNow(); // RedisServer.stop()
    }

        @Test
    public void testPing() {
        try (Jedis jedis = new Jedis("localhost", PORT)) {
            String result = jedis.ping();
            assertEquals("PONG", result);
        }
    }

    @Test
    public void testSetGet() {
        try (Jedis jedis = new Jedis("localhost", PORT)) {
            jedis.set("foo", "bar");
            String result = jedis.get("foo");
            assertEquals("bar", result);
        }
    }

    @Test
    public void testGetMissingKey() {
        try (Jedis jedis = new Jedis("localhost", PORT)) {
            String result = jedis.get("missing");
            assertNull(result);
        }
    }  
}
