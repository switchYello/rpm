package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/3 15:59
 */
public class RawDataCmd implements Cmd {

    ByteBuf content;

    public RawDataCmd(ByteBuf content) {
        content.retain();
        this.content = content.slice();
    }

    public ByteBuf getContent() {
        return content;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(Cmd.rawData);
        buf.writeInt(content.readableBytes());
        buf.writeBytes(content);
    }

    public static RawDataCmd decoderFrom(ByteBuf in) {
        int dataLength = in.readInt();
        ByteBuf data = in.readBytes(dataLength);
        return new RawDataCmd(data);
    }

}
