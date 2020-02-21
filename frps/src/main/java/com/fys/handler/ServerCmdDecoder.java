package com.fys.handler;

import com.fys.ServerManager;
import com.fys.cmd.Cmd;
import com.fys.cmd.clientToServer.Pong;
import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.handler.FlowManagerHandler;
import com.fys.cmd.handler.TimeOutHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/10
 * length，code，data
 * 此类处理服务端和客户端managerChannel
 */
public class ServerCmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ServerCmdDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        int length = in.readInt();
        byte flag = in.readByte();

        //收到新增需要开启Server
        if (flag == Cmd.ClientToServer.wantManagerCmd) {
            int port = in.readShort();
            String clientName = readStr(in, length - 1 - 2);
            log.debug("读取到WantManagerCmd,Port:{},ClientName:{}", port, clientName);
            ctx.pipeline().addLast(WantManagerCmdHandler.INSTANCE);
            out.add(new WantManagerCmd(port, clientName));
            return;
        }

        //收到客户端pong,只有管理连接才能收到pong，因为数据连接在初次连接后会移除当前Handler
        if (flag == Cmd.ClientToServer.pong) {
            out.add(new Pong());
            return;
        }

        //新建数据连接
        if (flag == Cmd.ClientToServer.wantDataCmd) {
            long connectionToken = in.readLong();
            String serverId = readStr(in, length - 1 - 8);
            log.debug("读取到WantDataCmd,ServerId:{},Token:{}", serverId, connectionToken);
            //添加TimeOutHandler和FlowManagerHandler
            ctx.pipeline().remove(this);
            ctx.pipeline().addLast(new TimeOutHandler(0, 0, 120));
            ctx.pipeline().addLast(FlowManagerHandler.INSTANCE);
            ServerManager.addConnection(serverId, connectionToken, ctx.channel());
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端发送的指令,数据长度:{},指令:{}", length, flag);
        ctx.close();
    }

    private String readStr(ByteBuf in, int length) {
        return in.readCharSequence(length, UTF_8).toString();
    }
}
