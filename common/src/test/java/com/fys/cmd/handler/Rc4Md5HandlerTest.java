package com.fys.cmd.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * hcy 2020/4/3
 */
public class Rc4Md5HandlerTest {

    @Test
    public void decode() {

        EmbeddedChannel eb = new EmbeddedChannel(new Rc4Md5Handler("123456"));
        //出站将数据加密
        ByteBuf origin = Unpooled.wrappedBuffer("abcdefghijklmn".getBytes());
        //将副本写出channel
        ByteBuf src = origin.copy();
        Assert.assertTrue(eb.writeOutbound(src));
        //出站rc4加密后的数据
        Object o = eb.readOutbound();

        //进站，解密数据
        Assert.assertTrue(eb.writeInbound(o));
        //读取解密后的数据
        ByteBuf buff = eb.readInbound();
        Assert.assertTrue(ByteBufUtil.equals(origin, buff));
        Assert.assertFalse(eb.finish());
        eb.close();
    }
}