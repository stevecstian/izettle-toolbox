package com.izettle.datastax.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.izettle.cassandra.AwsSecurityGroupHostSupplierAbstract;
import com.izettle.cassandra.ConnectionPoint;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cassandra Astyanax Host Supplier for Multiregion AWS implementations,
 * that discovers Cassandra nodes based on a given security group
 * and returns nodes only in the current/given region.
 *
 * @author progre55
 */
public class AwsSecurityGroupHostSupplier extends AwsSecurityGroupHostSupplierAbstract<Collection<InetSocketAddress>> {

    /**
     * Constructor with a specific region. Can run on non-AWS installations.
     *
     * @param client      - {@link AmazonEC2Client} implementation
     * @param groupId     - AWS security group id
     * @param region      - AWS {@link Region} to look for nodes in
     * @param defaultPort - default Cassandra api port
     */
    public AwsSecurityGroupHostSupplier(AmazonEC2Client client, String groupId, Region region, int defaultPort) {
        super(client, groupId, region, defaultPort);
    }

    /**
     * Constructor with the default region. Will try to determine the Cassandra region based
     * on the current instance placement.
     * Astyanax should be running on an AWS instance if this constructor is used.
     *
     * @param client      - {@link AmazonEC2Client} implementation
     * @param groupId     - AWS security group id
     * @param defaultPort - default Cassandra api port
     */
    public AwsSecurityGroupHostSupplier(AmazonEC2Client client, String groupId, int defaultPort) {
        this(client, groupId, null, defaultPort);
    }

    @Override
    public Collection<InetSocketAddress> convertTo(List<ConnectionPoint> connectionPoints) {
        return connectionPoints.stream()
            .map(connectionPoint -> new InetSocketAddress(connectionPoint.getIpAddress(), connectionPoint.getPort()))
            .collect(
                Collectors.toList());
    }
}
