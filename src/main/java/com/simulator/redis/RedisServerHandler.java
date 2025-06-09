package com.simulator.redis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class RedisServerHandler extends ChannelInboundHandlerAdapter {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String input = buf.toString(CharsetUtil.UTF_8);
        buf.release();

        List<String> parts = RespParser.parse(input);  // 解析 RESP 格式
        if (parts.isEmpty()) return;

        String cmd = parts.get(0).toUpperCase();
        String response;

        switch (cmd) {
            case "PING":
                response = "+PONG\r\n";
                break;
            case "SET":
                store.put(parts.get(1), parts.get(2));
                response = "+OK\r\n";
                break;
            case "GET":
                String value = store.getOrDefault(parts.get(1), null);
                if (value != null)
                    response = "$" + value.length() + "\r\n" + value + "\r\n";
                else
                    response = "$-1\r\n";
                break;
            default:
                response = "-ERR unknown command\r\n";
        }

        ctx.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }
}