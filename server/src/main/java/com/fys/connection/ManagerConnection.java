package com.fys.connection;

import com.fys.ClientManager;
import com.fys.cmd.message.LoginAuthInfo;
import com.fys.cmd.message.LoginFailCmd;
import com.fys.cmd.message.Pong;
import com.fys.cmd.util.CodeUtil;
import com.fys.conf.ServerInfo;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author hcy
 * @since 2022/4/28 14:44
 */
public class ManagerConnection {

    private static final Logger log = LoggerFactory.getLogger(ManagerConnection.class);
    private ChannelHandlerContext ctx;
    private ClientManager clientManager;
    ServerInfo serverInfo;
    String clientName;

    public ManagerConnection(ChannelHandlerContext ctx, ClientManager clientManager, ServerInfo serverInfo) {
        this.ctx = ctx;
        this.clientManager = clientManager;
        this.serverInfo = serverInfo;
    }

    public ChannelFuture writeMessage(Object obj) {
        return ctx.writeAndFlush(obj);
    }

    public void close() {
        ctx.close();
    }

    public ChannelHandlerContext nativeChannel() {
        return ctx;
    }

    public String getClientName() {
        return clientName;
    }

    /**
     * 验证登录信息，如已登录则忽略，如验证失败则发送响应后关闭连接
     */
    public void handlerLogin(LoginAuthInfo msg) {
        byte[] md5 = CodeUtil.md5((msg.getClientName() + msg.getTimeStamp() + serverInfo.getToken()).getBytes(UTF_8));
        if (!Arrays.equals(msg.getReadMd5(), md5)) {
            log.info("客户端验证失败:{}", msg);
            ctx.writeAndFlush(new LoginFailCmd(msg.getClientName(), "验证失败")).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (clientName != null) {
            log.info("已登录，无需重复登录:{}", clientName);
            return;
        }
        this.clientName = msg.getClientName();
        clientManager.registerManagerConnection(this);
        log.info("客户端登录成功 - {}", ctx.channel());
    }

    public void handlerPing() {
        ctx.writeAndFlush(new Pong()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

}
