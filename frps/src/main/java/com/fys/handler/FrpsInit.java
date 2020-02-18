package com.fys.handler;

import com.fys.cmd.handler.CmdEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * hcy 2020/2/17
 * 用来初始化FrpsHandler而不能直接在ServerBootStrap中new FrpsHandler(),因为传入ServerBootStrap里的Handler是单例的
 */
public class FrpsInit extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) {
        ch.pipeline().addLast(new CmdEncoder());
        ch.pipeline().addLast(new FrpsHandler());
    }
}
