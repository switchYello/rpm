package com.fys.handler;

import com.fys.ServerManager;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.handler.FlowManagerHandler;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.clientToServer.Pong;
import com.fys.cmd.message.clientToServer.WantManagerCmd;
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

    //因为每个ManagerChannel能开启多个Server，但只需要添加一份Handler即可，所以设置这个标志位
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
        if (flag == Cmd.dataConnectionCmd) {
            DataConnectionCmd cmd = DataConnectionCmd.decoderFrom(in);
            log.debug("获取客户端连接:{}", cmd);
            ctx.pipeline().remove(this);
            //这个客户端和服务端之间的超时检测，时间放长一些起保护作用即可，实际不需要这里做判断
            ctx.pipeline().addLast(new TimeOutHandler(0, 0, 300));
            ctx.pipeline().addLast(ExceptionHandler.INSTANCE);
            ServerManager.addConnection(cmd.getToken(), ctx.channel());
            return;
        }

        //收到客户端pong,只有管理连接才能收到pong，数据连接不会发送Ping也就不会收到Pong
        if (flag == Cmd.ClientToServer.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端发送的指令,指令:{}", flag);
        ctx.close();
    }

}