package com.izettle.messaging.queue;

import java.sql.Connection;
import java.sql.DriverManager;

public class H2ConnectionProvider {

    public static Connection inMemory(String name) {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=0");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection file(String path) {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:split:" + path + ";DB_CLOSE_DELAY=0;FILE_LOCK=NO");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


