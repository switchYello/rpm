package com.fys.handler;

import com.fys.Config;
import com.fys.DataConnectionClient;
import com.fys.cmd.message.DataConnectionCmd;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只有服务器创建成功后，才会被添加到channel中
 */
public class DataConnectionHandler extends SimpleChannelInboundHandler<DataConnectionCmd> {

    private Logger log = LoggerFactory.getLogger(DataConnectionHandler.class);

    /*
     * 需要注意这个ctx是managerChannel的，即使开启失败也不能关闭此ctx
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataConnectionCmd msg) {
        Attribute<Config> conf = ctx.channel().attr(AttributeKey.valueOf("conf"));
        Config config = conf.get();
        log.debug("收到服务端DataConnection,{} -> {}:{}", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
        Promise<DataConnectionClient> promise = ctx.executor().newPromise();
        new DataConnectionClient(config.getServerInfo(), msg).start(promise);
        promise.addListener((GenericFutureListener<Future<DataConnectionClient>>) future -> {
            if (future.isSuccess()) {
                log.debug("开启dataConnection{} -> {}:{}成功", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
                future.getNow().write(msg);
            } else {
                log.info("开启dataConnection失败", future.cause());
            }
        });
    }

}
