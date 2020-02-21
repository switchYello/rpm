package com.fys.handler;

import com.fys.DataConnectionClient;
import com.fys.cmd.clientToServer.WantDataCmd;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只有服务器创建成功后，才会被添加到channel中
 */
public class NeedNewConnectionHandler extends SimpleChannelInboundHandler<NeedCreateNewConnectionCmd> {

    private Logger log = LoggerFactory.getLogger(NeedNewConnectionHandler.class);

    private String serverId;

    public NeedNewConnectionHandler(String serverId) {
        this.serverId = serverId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NeedCreateNewConnectionCmd msg) {
        log.info("收到服务端NeedNewConnection");
        new DataConnectionClient().start().addListener((GenericFutureListener<Future<DataConnectionClient>>) future -> {
            if (future.isSuccess()) {
                future.getNow().write(new WantDataCmd(msg.getConnectionToken(), serverId));
            }
        });
    }

}
