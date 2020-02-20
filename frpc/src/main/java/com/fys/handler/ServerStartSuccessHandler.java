package com.fys.handler;

import com.fys.DataConnectionClient;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import com.fys.cmd.serverToClient.ServerStartSuccessCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/19
 * 此类处理服务器创建成功事件
 */
public class ServerStartSuccessHandler extends SimpleChannelInboundHandler<ServerStartSuccessCmd> {

    private Logger log = LoggerFactory.getLogger(ServerStartSuccessHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartSuccessCmd msg) {
        log.info("服务器创建server成功");
        ctx.pipeline().replace(this, null, new NeedNewConnectionHandler(msg.getServerId()));
    }

    /**
     * 此类处理服务器发送的需要新连接的请求，但只有服务器创建成功后，才会被添加到channel中
     */
    private static class NeedNewConnectionHandler extends SimpleChannelInboundHandler<NeedCreateNewConnectionCmd> {

        private Logger log = LoggerFactory.getLogger(NeedNewConnectionHandler.class);

        private String serverId;

        public NeedNewConnectionHandler(String serverId) {
            this.serverId = serverId;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, NeedCreateNewConnectionCmd msg) {
            log.info("服务器需要新连接");
            log.info("收到服务端NeedNewConnection serverId:{}", serverId);
            new DataConnectionClient(serverId).start();
        }

    }


}