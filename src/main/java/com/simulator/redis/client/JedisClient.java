package com.simulator.redis.client;

import redis.clients.jedis.Jedis;

public class JedisClient {
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.set("foo", "bar");
            String value = jedis.get("foo");
            System.out.println("GET foo => " + value);
        }
    }
}

