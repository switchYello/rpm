package com.fys.handler;

import com.fys.Config;
import com.fys.cmd.Cmd;
import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import com.fys.cmd.serverToClient.Ping;
import com.fys.cmd.serverToClient.ServerStartFailCmd;
import com.fys.cmd.serverToClient.ServerStartSuccessCmd;
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
        ctx.writeAndFlush(new WantManagerCmd(Config.serverWorkPort, Config.localClientName))
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

        if (flag == Cmd.ServerToClient.needCreateNewConnectionCmd) {
            out.add(new NeedCreateNewConnectionCmd(in.readCharSequence(length - 1, UTF_8).toString()));
            return;
        }
        if (flag == Cmd.ServerToClient.ping) {
            out.add(new Ping());
            return;
        }
        if (flag == Cmd.ServerToClient.serverStartFailCmd) {
            out.add(new ServerStartFailCmd(in.readCharSequence(length - 1, UTF_8).toString()));
            return;
        }
        if (flag == Cmd.ServerToClient.serverStartSuccessCmd) {
            out.add(new ServerStartSuccessCmd());
            return;
        }

        log.error("无法识别服务端发送的指令,数据长度:{},指令:{}", length, flag);
        ctx.close();
    }

}
