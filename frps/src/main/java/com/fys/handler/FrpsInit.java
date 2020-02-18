package com.fys.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * hcy 2020/2/17
 * 用来初始化FrpsHandler而不能直接在ServerBootStrap中new FrpsHandler(),因为传入ServerBootStrap里的Handler是单例的
 */
public class FrpsInit extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new FrpsHandler());
    }
}
