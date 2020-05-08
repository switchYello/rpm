package com.fys.handler;

import com.fys.AppClient;
import com.fys.Config;
import com.fys.DataConnectionClient;
import com.fys.cmd.message.DataConnectionCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataConnectionHandler extends SimpleChannelInboundHandler<DataConnectionCmd> {

    private Logger log = LoggerFactory.getLogger(DataConnectionHandler.class);

    /*
     * 需要注意这个ctx是managerChannel的，即使开启失败也不能关闭此ctx
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataConnectionCmd msg) {
        Attribute<Config> conf = ctx.channel().attr(AppClient.key);
        Config config = conf.get();
        log.debug("收到服务端DataConnection,{} -> {}:{}", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
        new DataConnectionClient(config, msg).start();
    }

}
