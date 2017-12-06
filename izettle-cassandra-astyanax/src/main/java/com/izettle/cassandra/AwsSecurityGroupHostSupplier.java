package com.izettle.cassandra;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.netflix.astyanax.connectionpool.Host;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cassandra Astyanax Host Supplier for Multiregion AWS implementations,
 * that discovers Cassandra nodes based on a given security group
 * and returns nodes only in the current/given region.
 */
public class AwsSecurityGroupHostSupplier extends AbstractAwsSecurityGroupHostSupplier<List<Host>> {

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
    public List<Host> convertTo(List<ConnectionPoint> connectionPoints) {
        return connectionPoints.stream()
            .map(connectionPoint -> new Host(connectionPoint.getIpAddress(), connectionPoint.getPort()))
            .collect(
                Collectors.toList());
    }
}
