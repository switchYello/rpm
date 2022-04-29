package com.fys.cmd.message;

import java.util.Arrays;

/**
 * @author hcy
 * @since 2022/4/29 18:19
 */
public class LoginAuthInfo {

    String clientName;
    long timeStamp;
    byte[] readMd5;

    public LoginAuthInfo(String clientName, long timeStamp, byte[] readMd5) {
        this.clientName = clientName;
        this.timeStamp = timeStamp;
        this.readMd5 = readMd5;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public byte[] getReadMd5() {
        return readMd5;
    }

    public void setReadMd5(byte[] readMd5) {
        this.readMd5 = readMd5;
    }

    @Override
    public String toString() {
        return "LoginAuthInfo{" +
                "clientName='" + clientName + '\'' +
                ", timeStamp=" + timeStamp +
                ", readMd5=" + Arrays.toString(readMd5) +
                '}';
    }
}
