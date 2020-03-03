package com.fys.dao;

import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * hcy 2020/3/2
 */
public class ConfigDao {

    private static Logger log = LoggerFactory.getLogger(ConfigDao.class);

    private static JdbcConnectionPool cp;

    static {
        String initSql = "table.sql";
        String url = "jdbc:h2:~/test;DB_CLOSE_DELAY=-1;MODE=MySQL";

        try (BufferedReader in = new BufferedReader(new InputStreamReader(ConfigDao.class.getClassLoader().getResourceAsStream(initSql), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = in.readLine()) != null) {
                if (temp.trim().startsWith("#")) {
                    continue;
                }
                sb.append(temp);
            }

            Class.forName("org.h2.Driver");
            DriverManager.getConnection(url, "sa", "").close();
            cp = JdbcConnectionPool.create(url, "sa", "");

            Connection connection = cp.getConnection();

            for (String s : sb.toString().split(";")) {
                log.debug("加载初始化sql为:" + s);
                boolean execute = connection.createStatement().execute(s);
                log.debug("执行结果:" + execute);
            }
            connection.close();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 添加服务
     * */
    public void addServer(ServerInfo server) {
        try (Connection conn = cp.getConnection()) {
            conn.createStatement().execute("delete from server_info");
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `server_info` (`id`, `server_host`, `server_port`, `auto_token`) VALUES ('1', ?, ?, ?)");
            preparedStatement.setString(1, server.getServerIp());
            preparedStatement.setInt(2, server.getServerPort());
            preparedStatement.setString(3, server.getAutoToken());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }


    public ServerWorker getWorkerById(int id) {
        try (Connection conn = cp.getConnection()) {
            ResultSet resultSet = conn.createStatement().executeQuery("select * from server_worker where id = " + id);
            if (resultSet.next()) {
                ServerWorker s = new ServerWorker();
                s.setServerPort(resultSet.getInt(2));
                s.setLocalHost(resultSet.getString(3));
                s.setLocalPort(resultSet.getInt(4));
                return s;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }

    public void insertWork(ServerWorker work) {
        try (Connection conn = cp.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `SERVER_WORKER` (`server_port`, `local_host`, `local_port`) VALUES (?, ?, ?)");
            preparedStatement.setInt(1, work.getServerPort());
            preparedStatement.setString(2, work.getLocalHost());
            preparedStatement.setInt(3, work.getLocalPort());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }

    public void updateWork(ServerWorker work) {
        try (Connection conn = cp.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("Update `SERVER_WORKER` SET server_port = ?,local_host = ?,local_port = ? where id = ?");
            preparedStatement.setInt(1, work.getServerPort());
            preparedStatement.setString(2, work.getLocalHost());
            preparedStatement.setInt(3, work.getLocalPort());
            preparedStatement.setInt(4, work.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }

    public void deleteWork(int id) {
        try (Connection conn = cp.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("delete from `SERVER_WORKER` where id = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }

    public ServerInfo getServerInfo() {
        try (Connection conn = cp.getConnection()) {

            ResultSet resultSet = conn.createStatement().executeQuery("select * from server_info where id = 1");
            ServerInfo s = new ServerInfo();
            if (resultSet.next()) {
                s.setServerIp(resultSet.getString(2));
                s.setServerPort(resultSet.getInt(3));
                s.setAutoToken(resultSet.getString(4));
            }
            return s;
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }

    public List<ServerWorker> getServerWorks() {
        try (Connection conn = cp.getConnection()) {
            ResultSet resultSet = conn.createStatement().executeQuery("select * from server_worker");
            List<ServerWorker> list = new ArrayList<>();
            while (resultSet.next()) {
                ServerWorker s = new ServerWorker();
                s.setId(resultSet.getInt(1));
                s.setServerPort(resultSet.getInt(2));
                s.setLocalHost(resultSet.getString(3));
                s.setLocalPort(resultSet.getInt(4));
                list.add(s);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("sqlException", e);
        }
    }

}
