package com.fys;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * hcy 2020/2/26
 */
public class H2Test {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {


        JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:~/test", "", "");
        Connection conn = cp.getConnection();
        conn.close();
        cp.dispose();

//        Class.forName("org.h2.Driver");
//        Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
//
//        Server.createWebServer().start();

        //默认8082 端口

        Class.forName("org.h2.Driver");
        Connection conn1 = DriverManager.getConnection("jdbc:h2:tcp:localhost:9092/~/test", "sa", "");

    }


}