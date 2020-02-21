package com.fys.handler;

import com.fys.cmd.serverToClient.ServerStartSuccessCmd;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/19
 * 此类处理服务器创建成功事件
 */
@ChannelHandler.Sharable
public class ServerStartSuccessHandler extends SimpleChannelInboundHandler<ServerStartSuccessCmd> {

    private Logger log = LoggerFactory.getLogger(ServerStartSuccessHandler.class);
    public static ServerStartSuccessHandler INSTANCE = new ServerStartSuccessHandler();

    private ServerStartSuccessHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartSuccessCmd msg) {
        log.info("服务器创建server成功");
        ctx.pipeline().replace(this, null, new NeedNewConnectionHandler(msg.getServerId()));
    }

}