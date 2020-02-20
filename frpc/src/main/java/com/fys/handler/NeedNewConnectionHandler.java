package com.fys.handler;

import com.fys.DataConnectionClient;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/19
 */
public class NeedNewConnectionHandler extends SimpleChannelInboundHandler<NeedCreateNewConnectionCmd> {

    private Logger log = LoggerFactory.getLogger(NeedNewConnectionHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NeedCreateNewConnectionCmd msg) {
        log.info("服务器需要新连接");
        log.info("收到服务端NeedNewConnection serverId:{}", msg.getServerId());
        new DataConnectionClient(msg.getServerId()).start();
    }

}