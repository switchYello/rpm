package com.fys.cmd.exception;

/**
 * hcy 2020/2/24
 */
public class AuthenticationException extends RuntimeException {

    public static AuthenticationException INSTANCE = new AuthenticationException();

    public AuthenticationException() {
        super("密码不对");
    }
}
