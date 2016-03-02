package com.izettle.cassandra;

/**
 * Created by Johan on 02/03/16.
 */
public class ConnectionPoint {
    private final String ipAddress;
    private final int port;

    public ConnectionPoint(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
