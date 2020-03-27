package com.fys.handler;

import com.fys.Config;
import com.fys.ServerManager;
import com.fys.cmd.exception.AuthenticationException;
import com.fys.cmd.message.clientToServer.LoginCmd;
import com.fys.cmd.message.serverToClient.LoginFailCmd;
import com.fys.cmd.message.serverToClient.ServerStartFailCmd;
import com.fys.cmd.message.serverToClient.ServerStartSuccessCmd;
import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/3/25
 */
public class LoginCmdHandler extends SimpleChannelInboundHandler<LoginCmd> {

    private static Logger log = LoggerFactory.getLogger(LoginCmdHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginCmd msg) {
        ServerInfo serverInfo = Config.getServerInfo(msg.getClientName());
        //确定配置中存在此客户端名
        if (serverInfo == null) {
            LoginFailCmd cmd = new LoginFailCmd(msg.getClientName(), "无法识别客户端名");
            log.info(cmd.toString());
            ctx.writeAndFlush(cmd).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        //确认此消息的token是正确的
        try {
            msg.check(serverInfo.getToken());
        } catch (AuthenticationException e) {
            LoginFailCmd cmd = new LoginFailCmd(msg.getClientName(), "token验证不通过");
            log.info(cmd.toString());
            ctx.writeAndFlush(cmd).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        log.info("{} 登录成功", serverInfo.getClientName());

        //开启配置中的映射服务,并在成功或失败后发送消息
        for (ServerWorker sw : serverInfo.getServerWorkers()) {
            ServerManager.startServers(sw, ctx.channel()).addListener(f -> {
                if (f.isSuccess()) {
                    ServerStartSuccessCmd successCmd = new ServerStartSuccessCmd(sw.getServerPort(), sw.getLocalHost(), sw.getLocalPort());
                    log.info(successCmd.toString());
                    ctx.writeAndFlush(successCmd).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    ServerStartFailCmd failCmd = new ServerStartFailCmd(sw.getServerPort(), sw.getLocalHost(), sw.getLocalPort(), f.cause().toString());
                    log.error(failCmd.toString());
                    ctx.writeAndFlush(failCmd).addListener(ChannelFutureListener.CLOSE);
                }
            });
        }

    }


}
