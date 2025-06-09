package com.simulator.redis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RedisServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void start(int port) throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .childHandler(new ChannelInitializer<>() {
                     @Override
                     protected void initChannel(Channel ch) {
                         ChannelPipeline pipeline = ch.pipeline();
                         pipeline.addLast(new RedisServerHandler());
                     }
                 });

        ChannelFuture future = bootstrap.bind(port).sync();
        System.out.println("Redis Simulator started on port " + port);
        future.channel().closeFuture().sync(); // 阻塞等待关闭
    }

    public void stop() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 6379;
        new RedisServer().start(port);
    }
}
