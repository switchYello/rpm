package test;

import com.fys.InnerConnectionFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Test;

/**
 * @author hcy
 * @since 2022/4/27 23:54
 */
public class TestChannel {


    @Test
    public void testChannel() throws InterruptedException {

        byte[] bytes = new byte[1024 * 150];

        ChannelFuture future = InnerConnectionFactory.createChannel("127.0.0.1", 80, true);
        Channel channel = future.channel();
        ChannelFuture channelFuture = channel.write(Unpooled.wrappedBuffer(bytes));

        System.out.println("write ok");
        channelFuture.sync();
        System.out.println("success");
    }

}
