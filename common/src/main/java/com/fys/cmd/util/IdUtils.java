package com.fys.cmd.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hcy
 * @since 2022/5/4 13:22
 * 简单的id生成器，保证程序内一定数量下不重复
 */
public class IdUtils {

    private static final AtomicInteger ids = new AtomicInteger(1);

    public static int nextId() {
        return ids.getAndIncrement();
    }

}
