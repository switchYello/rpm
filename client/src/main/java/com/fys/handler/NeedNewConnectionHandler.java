package com.fys.handler;

import com.fys.DataConnectionClient;
import com.fys.cmd.clientToServer.WantDataCmd;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只有服务器创建成功后，才会被添加到channel中
 */
@ChannelHandler.Sharable
public class NeedNewConnectionHandler extends SimpleChannelInboundHandler<NeedCreateNewConnectionCmd> {

    private Logger log = LoggerFactory.getLogger(NeedNewConnectionHandler.class);

    public static NeedNewConnectionHandler INSTANCE = new NeedNewConnectionHandler();

    private NeedNewConnectionHandler() {
    }


    /*
     * 需要注意这个ctx是managerChannel的，即使开启失败也不能关闭此ctx
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NeedCreateNewConnectionCmd msg) {
        log.debug("收到服务端NeedNewConnection,{} -> {}:{}", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
        new DataConnectionClient(msg).start()
                .addListener((GenericFutureListener<Future<DataConnectionClient>>) future -> {
                    if (future.isSuccess()) {
                        log.info("开启dataConnection{} -> {}:{}成功", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
                        future.getNow().write(new WantDataCmd(msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort()));
                    } else {
                        log.info("开启dataConnection失败", future.cause());
                    }
                });
    }

}
