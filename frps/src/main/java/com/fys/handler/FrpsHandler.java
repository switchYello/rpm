package com.fys.handler;

import com.fys.ConnectionPool;
import com.fys.Server;
import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (code == Cmd.dataCmd) {
            log.info("服务端读取到的数据长度是:{},数据标志位是:{},标志位是dataCmd，添加到连接池", length, code);
            ctx.pipeline().remove(this);
            ConnectionPool.addConnection(ctx);
            return;
        }

        /*
         *此连接是`管理连接`，则标记为管理连接，继续使用
         *长度：标志位：port
         * */
        if (code == Cmd.managerCmd) {
            ConnectionPool.managerHandlerContext = ctx;
            int port = in.readShort();
            log.info("服务端读取到的数据长度是:{},数据标志位是:{},标志位是managerCmd,将要监听的端口是:{}", length, code, port);
            new Server("127.0.0.1", port).start();
            return;
        }

        throw new RuntimeException("服务端读取到的数据长度是:" + length + ",数据标志位是:" + code + ",标志位是无法识别");
    }


}
