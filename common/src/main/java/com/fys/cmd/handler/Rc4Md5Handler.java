package com.fys.cmd.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;

import static com.fys.cmd.util.CodeUtil.md5;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/4/3
 */
public class Rc4Md5Handler extends ByteToMessageCodec<ByteBuf> {

    private final static SecureRandom random = new SecureRandom();
    private boolean firstDecode = true;
    private boolean firstEncode = true;
    private Cipher decoderCipher;
    private Cipher encoderCipher;

    private String password;

    public Rc4Md5Handler(String password) {
        this.password = password;
    }

    /*
     * 第一次写数据时，生成随机IV 构建cipher
     * 使用cipher加密数据
     * */
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        if (firstEncode) {
            byte[] iv = randomIv();
            encoderCipher = Cipher.getInstance("RC4");
            encoderCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(md5(md5(password.getBytes(UTF_8)), iv), "RC4"));
            firstEncode = false;
            out.writeBytes(iv);
        }
        if (msg.readableBytes() <= 0) {
            return;
        }
        //将要加密的数据明文
        ByteBuffer data = msg.nioBuffer();
        int dataLength = data.remaining();
        //存储密文的缓存
        ByteBuffer outData = ctx.alloc().ioBuffer(dataLength).nioBuffer(0, dataLength);
        //加密操作,返回处理数据的长度
        int updateLength = encoderCipher.update(data, outData);
        //忽略处理过的数据
        msg.skipBytes(updateLength);
        outData.flip();
        out.writeBytes(outData);
    }


    /*
     * 初次读时，前16位作为iv使用，构建cipher，以后再使用则不需要构建
     * */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (firstDecode) {
            if (in.readableBytes() < 16) {
                return;
            }
            byte[] iv = readByte(in, 16);
            decoderCipher = Cipher.getInstance("RC4");
            byte[] realPassWord = md5(md5(password.getBytes(UTF_8)), iv);
            decoderCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(realPassWord, "RC4"));
            firstDecode = false;
        }
        if (in.readableBytes() <= 0) {
            return;
        }
        //获取到的密文
        ByteBuffer data = in.nioBuffer();
        int dataLength = data.remaining();
        //存储明文
        ByteBuffer outData = ctx.alloc().ioBuffer(dataLength).nioBuffer(0, dataLength);
        //解密操作
        int updateLength = decoderCipher.update(data, outData);
        //忽略处理过的数据
        in.skipBytes(updateLength);
        //反转明文buffer
        outData.flip();
        //输出明文
        out.add(Unpooled.wrappedBuffer(outData));
    }


    private byte[] readByte(ByteBuf in, int length) {
        byte[] bytes = ByteBufUtil.getBytes(in, in.readerIndex(), length);
        in.skipBytes(bytes.length);
        return bytes;
    }

    private byte[] randomIv() {
        return random.generateSeed(16);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (encoderCipher != null) {
            encoderCipher.doFinal();
        }
        if (decoderCipher != null) {
            decoderCipher.doFinal();
        }
        super.channelInactive(ctx);
    }
}
