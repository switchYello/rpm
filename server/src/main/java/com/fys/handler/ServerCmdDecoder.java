package com.fys.handler;

import com.fys.ServerManager;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.clientToServer.LoginCmd;
import com.fys.cmd.message.clientToServer.Pong;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * hcy 2020/2/10
 * length，code，data
 * 此类处理服务端和客户端managerChannel
 */
public class ServerCmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ServerCmdDecoder.class);

    //防止多次登录
    private boolean addHandler = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte flag = in.readByte();
        //新建数据连接
        if (flag == Cmd.dataConnectionCmd) {
            DataConnectionCmd cmd = DataConnectionCmd.decoderFrom(in);
            log.debug("获取客户端连接:{}", cmd);
            ctx.pipeline().remove(this);
            ServerManager.addConnection(cmd, ctx.channel());
            return;
        }

        //收到客户端pong,只有管理连接才能收到pong，数据连接不会发送Ping也就不会收到Pong
        if (flag == Cmd.ClientToServer.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }

        //登录
        if (flag == Cmd.ClientToServer.login) {
            LoginCmd login = LoginCmd.decoderFrom(in);
            if (!addHandler) {
                ctx.pipeline().addLast(new LoginCmdHandler());
                ctx.pipeline().addLast(new PingPongHandler());
                ctx.pipeline().addLast(ExceptionHandler.INSTANCE);
                addHandler = !addHandler;
            }
            out.add(login);
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端:{} 发送的指令,指令:{}", ctx.channel().remoteAddress(), flag);
        in.skipBytes(in.readableBytes());
        ctx.close();

    }

}
