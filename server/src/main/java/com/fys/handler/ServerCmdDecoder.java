package com.fys.handler;

import com.fys.ServerManager;
import com.fys.cmd.Cmd;
import com.fys.cmd.clientToServer.Pong;
import com.fys.cmd.clientToServer.WantDataCmd;
import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.handler.FlowManagerHandler;
import com.fys.cmd.handler.TimeOutHandler;
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

    private boolean addHandler = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        byte flag = in.readByte();

        //收到新增需要开启Server
        if (flag == Cmd.ClientToServer.wantManagerCmd) {
            WantManagerCmd wantManagerCmd = WantManagerCmd.decoderFrom(in);
            if (!addHandler) {
                ctx.pipeline().addLast(WantManagerCmdHandler.INSTANCE);
                ctx.pipeline().addLast(new PingPongHandler());
                ctx.pipeline().addLast(ExceptionHandler.INSTANCE);
                addHandler = !addHandler;
            }
            out.add(wantManagerCmd);
            return;
        }

        //新建数据连接
        if (flag == Cmd.ClientToServer.wantDataCmd) {
            WantDataCmd wantDataCmd = WantDataCmd.decoderFrom(in);
            log.debug("获取客户端连接:{}", wantDataCmd);
            ctx.pipeline().remove(this);
            ctx.pipeline().addLast(new TimeOutHandler(0, 0, 120));
            ctx.pipeline().addLast(FlowManagerHandler.INSTANCE);
            ctx.pipeline().addLast(ExceptionHandler.INSTANCE);
            ServerManager.addConnection(wantDataCmd.getServerPort(), ctx.channel());
            return;
        }

        //收到客户端pong,只有管理连接才能收到pong，因为数据连接在初次连接后会移除当前Handler
        if (flag == Cmd.ClientToServer.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端发送的指令,指令:{}", flag);
        ctx.close();
    }

}
