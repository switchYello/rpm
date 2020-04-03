package com.fys.cmd.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hcy 2020/4/3
 */
public class CodeUtil {

    public static byte[] md5(byte[] src) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            return md.digest(src);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
