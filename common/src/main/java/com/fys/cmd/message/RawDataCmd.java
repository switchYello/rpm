package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

/**
 * @author hcy
 * @since 2022/5/3 15:59
 */
public class RawDataCmd implements Cmd, ReferenceCounted {

    private ByteBuf content;

    public RawDataCmd(ByteBuf content) {
        this.content = content.slice();
    }

    public ByteBuf getContent() {
        return content;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(Cmd.RAW_DATA);
        buf.writeInt(content.readableBytes());
        buf.writeBytes(content);
    }

    public static RawDataCmd decoderFrom(ByteBuf in) {
        int dataLength = in.readInt();
        ByteBuf data = in.readBytes(dataLength);
        return new RawDataCmd(data);
    }

    @Override
    public int refCnt() {
        return content.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        return content.retain();
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return content.retain(increment);
    }

    @Override
    public ReferenceCounted touch() {
        content.touch();
        return this;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        content.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return content.release();
    }

    @Override
    public boolean release(int decrement) {
        return content.release(decrement);
    }
}
