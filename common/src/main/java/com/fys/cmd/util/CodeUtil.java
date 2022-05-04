package com.fys.cmd.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hcy 2020/4/3
 */
public class CodeUtil {

    private static char[] TABLE = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    public static byte[] md5(byte[] src) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            return md.digest(src);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5Str(byte[] src) {
        byte[] bytes = md5(src);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(TABLE[b >>> 4 & 0x0f]);
            sb.append(TABLE[b & 0x0f]);
        }
        return sb.toString();
    }

    public static byte[] md5(byte[] src, byte[] src2) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(src);
            md.update(src2);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
