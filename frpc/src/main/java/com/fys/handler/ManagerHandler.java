package com.fys.handler;

import com.fys.Config;
import com.fys.cmd.Cmd;
import com.fys.cmd.clientToServer.ManagerConnection;
import com.fys.cmd.serverToClient.NeedNewConnectionCmd;
import com.fys.cmd.serverToClient.Ping;
import com.fys.cmd.serverToClient.ServerStartFail;
import com.fys.cmd.serverToClient.ServerStartSuccess;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 */
public class ManagerHandler extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ManagerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接到服务器成功host：{}，port:{}", Config.serverHost, Config.serverPort);
        ctx.writeAndFlush(new ManagerConnection(Config.serverWorkPort, Config.localClientName))
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        log.info("客户端发送信息表示自己是控制链接");
                    }
                });
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        byte flag = in.readByte();

        if (flag == Cmd.ServerToClient.needCreateNewConnection) {
            out.add(new NeedNewConnectionCmd(in.readCharSequence(length - 1, UTF_8).toString()));
            return;
        }
        if (flag == Cmd.ServerToClient.ping) {
            out.add(new Ping());
        }
        if (flag == Cmd.ServerToClient.serverStartFail) {
            out.add(new ServerStartFail(in.readCharSequence(length - 1, UTF_8).toString()));
        }
        if (flag == Cmd.ServerToClient.serverStartSuccess) {
            out.add(new ServerStartSuccess());
        }

        throw new RuntimeException("无法识别服务端发送的指令,数据长度:" + length + ",指令:" + flag);
    }

}
