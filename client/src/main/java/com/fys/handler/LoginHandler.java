package com.fys.handler;

import com.fys.AppClient;
import com.fys.Config;
import com.fys.cmd.message.clientToServer.LoginCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * hcy 2020/5/8
 * 连接成功后则发送登陆信息
 */
public class LoginHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Config config = ctx.channel().attr(AppClient.key).get();
        ctx.writeAndFlush(new LoginCmd(config.getClientName()));
        super.channelActive(ctx);
    }
}
