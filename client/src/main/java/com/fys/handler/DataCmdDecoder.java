package com.fys.handler;

import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.RawDataCmd;
import com.fys.cmd.message.StartTransactionCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author hcy
 * @since 2022/5/4 14:04
 */
public class DataCmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(DataCmdDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int flag = in.readInt();
        if (flag == Cmd.RAW_DATA) {
            out.add(RawDataCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.START_TRANSACTION_CMD) {
            out.add(new StartTransactionCmd());
            return;
        }
        log.error("无法识别服务端发送的指令,指令:{}", flag);
        in.skipBytes(actualReadableBytes());
        ctx.close();
    }

}