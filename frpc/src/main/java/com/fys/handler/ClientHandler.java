package com.fys.handler;

import com.fys.Client;
import com.fys.Config;
import com.fys.cmd.Cmd;
import com.fys.cmd.clientToServer.ManagerConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * hcy 2020/2/18
 */
public class ClientHandler extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("客户端连接到服务器成功host：{}，port:{}", Config.serverHost, Config.serverPort);

        ctx.writeAndFlush(new ManagerConnection(Config.serverWorkPort))
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
        byte code = in.readByte();

        if (code == Cmd.createNewConnection) {
            CharSequence serverId = in.readCharSequence(length - 1, StandardCharsets.UTF_8);
            log.info("收到服务端NeedNewConnection serverId:{}", serverId);
            new Client(serverId.toString()).start();
        }
    }
}
