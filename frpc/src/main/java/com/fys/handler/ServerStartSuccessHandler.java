package com.fys.handler;

import com.fys.cmd.serverToClient.ServerStartSuccess;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/19
 */
public class ServerStartSuccessHandler extends SimpleChannelInboundHandler<ServerStartSuccess> {

    private Logger log = LoggerFactory.getLogger(ServerStartSuccessHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartSuccess msg) {

        log.info("服务器创建server成功");
    }

}