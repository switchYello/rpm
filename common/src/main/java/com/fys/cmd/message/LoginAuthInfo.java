package com.fys.cmd.message;

/**
 * @author hcy
 * @since 2022/4/29 18:19
 */
public class LoginAuthInfo {

    private String clientName;
    private long timeStamp;
    private String readMd5;

    public LoginAuthInfo(String clientName, long timeStamp, String readMd5) {
        this.clientName = clientName;
        this.timeStamp = timeStamp;
        this.readMd5 = readMd5;
    }

    public String getClientName() {
        return clientName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getReadMd5() {
        return readMd5;
    }


    @Override
    public String toString() {
        return "LoginAuthInfo{" +
                "clientName='" + clientName + '\'' +
                ", timeStamp=" + timeStamp +
                ", readMd5=" + readMd5 +
                '}';
    }
}
