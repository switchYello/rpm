package com.fys.handler;

import com.fys.ServerManager;
import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * hcy 2020/2/10
 * length，code，data
 */
public class FrpsHandler extends ReplayingDecoder<Void> {


    private static Logger log = LoggerFactory.getLogger(FrpsHandler.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        //读取四位长度
        int length = in.readInt();
        //读取标志位
        byte code = in.readByte();

        //新连接的数据，并表明自己是数据连接
        if (code == Cmd.dataCmd) {
            CharSequence serverId = in.readCharSequence(length - 1, StandardCharsets.UTF_8);
            log.info("服务端读取到的数据长度是:{},数据标志位是:{},标志位是dataCmd，serverId:{}", length, code, serverId);
            ServerManager.addConnection(serverId.toString(), ctx.channel());
            ctx.pipeline().remove(this);
            return;
        }

        /*
         *此连接是`管理连接`，则标记为管理连接，继续使用
         *长度：标志位：port
         * */
        if (code == Cmd.managerCmd) {
            int port = in.readShort();
            log.info("服务端读取到的数据长度是:{},数据标志位是:{},标志位是managerCmd,将要监听的端口是:{}", length, code, port);
            ServerManager.startNewServer(port, ctx.channel());
            //ctx.pipeline().remove(this);
            return;
        }

        throw new RuntimeException("服务端读取到的数据长度是:" + length + ",数据标志位是:" + code + ",标志位是无法识别");
    }


}
