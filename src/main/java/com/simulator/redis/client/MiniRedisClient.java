package com.simulator.redis.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MiniRedisClient {
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public MiniRedisClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
    }

    public String sendCommand(String... args) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.length).append("\r\n");
        for (String arg : args) {
            sb.append("$").append(arg.length()).append("\r\n").append(arg).append("\r\n");
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        return new String(buffer, 0, len);
    }

    public void close() throws IOException {
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        MiniRedisClient client = new MiniRedisClient("localhost", 6379);
        System.out.println(client.sendCommand("SET", "foo", "bar"));
        System.out.println(client.sendCommand("GET", "foo"));
        client.close();
    }
}

