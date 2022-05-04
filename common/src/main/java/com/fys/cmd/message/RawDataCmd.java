package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

/**
 * @author hcy
 * @since 2022/5/3 15:59
 * 原始数据
 */
public class RawDataCmd implements Cmd, ReferenceCounted {

    //最大传输包大小，超过此值进行切分
    private static final int limitPackageSize = 0x3FFF;

    private ByteBuf content;

    public RawDataCmd(ByteBuf content) {
        this.content = content.slice();
    }

    public ByteBuf getContent() {
        return content;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        while (content.isReadable()) {
            int minSize = Math.min(limitPackageSize, content.readableBytes());
            buf.writeInt(Cmd.RAW_DATA);
            buf.writeShort(minSize);
            buf.writeBytes(content, minSize);
        }
    }

    public static RawDataCmd decoderFrom(ByteBuf in) {
        short dataLength = in.readShort();
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
