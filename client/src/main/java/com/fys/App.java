package com.fys;

import com.fys.connection.ManagerConnection;

/**
 * @author hcy
 * @since 2022/4/23 23:06
 */
public class App {

    public static void main(String[] args) {
        new App().start();
    }

    private void start() {
        ManagerConnection connection = new ManagerConnection("hcy_home_pc", "0.0.0.0", 9050);
        connection.start();
    }

}
