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

    private int connectionCount = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NeedCreateNewConnectionCmd msg) {
        log.debug("收到服务端NeedNewConnection,ServerPort:{},LocalHost:{},LocalPort:{},Token:{},Count:{}", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort(), msg.getConnectionToken(), ++connectionCount);
        new DataConnectionClient(msg).start()
                .addListener((GenericFutureListener<Future<DataConnectionClient>>) future -> {
                    if (future.isSuccess()) {
                        log.info("开启dataConnection成功");
                        future.getNow().write(new WantDataCmd(msg.getConnectionToken()));
                    } else {
                        log.info("开启dataConnection失败", future.cause());
                    }
                });
    }

}
