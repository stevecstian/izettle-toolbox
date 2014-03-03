package com.izettle.astyanax.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.astyanax.connectionpool.Host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cassandra Astyanax Host Supplier for Multiregion AWS implementations,
 * that discovers Cassandra nodes based on a given security group
 * and returns nodes only in the current/given region.
 *
 * @author progre55
 */
public class AwsSecurityGroupHostSupplier implements Supplier<List<Host>> {

	private final static Logger LOG = LoggerFactory.getLogger(AwsSecurityGroupHostSupplier.class);

	private final AmazonEC2 client;
	private Region region;
	private final int port;
	private final Filter filter;
	private volatile List<Host> previousHosts;

	private static final int DEFAULT_TIMEOUT = 5000;
	private static final Region DEFAULT_REGION = Region.getRegion(Regions.EU_WEST_1);
	private static final String metadataEndpoint = "http://169.254.169.254/latest/meta-data/placement/availability-zone";

	/**
	 * Constructor with a specific region. Can run on non-AWS installations.
	 *
	 * @param client      - {@link AmazonEC2Client} implementation
	 * @param groupId     - AWS security group id
	 * @param region      - AWS {@link Region} to look for nodes in
	 * @param defaultPort - default Cassandra api port
	 */
	public AwsSecurityGroupHostSupplier(AmazonEC2Client client, String groupId, Region region, int defaultPort) {
		this.client = client;
		this.filter = new Filter("group-id", Lists.newArrayList(groupId));
		this.region = region;
		this.port = defaultPort;
		if (region == null) {
			setMyRegion();
		} else {
			client.setRegion(region);
		}
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

	/*
	 * Find out the region we are placed in
	 */
	private Region findMyRegion() {
		try {
			URL url = new URL(metadataEndpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(DEFAULT_TIMEOUT); // 5 seconds
			conn.setReadTimeout(DEFAULT_TIMEOUT); // 5 seconds

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String zone = br.readLine();

			br.close();

			if (zone != null && zone.length() > 0) {
				zone = zone.substring(0, zone.length() - 1);
				return Region.getRegion(Regions.fromName(zone));
			}

		} catch (IOException e) {
			LOG.warn("Could not get the Region information, is this instance even running on AWS? "
					+ "Will try again next time", e);
		}

		return null;
	}

	/*
	 * Find out and set the region if it hasn't already been set
	 */
	private void setMyRegion() {
		if (region == null) {
			region = findMyRegion();
			if (region != null) {
				client.setRegion(region);
			} else {
				// set to default region for now, will try again next time
				client.setRegion(DEFAULT_REGION);
			}
		}
	}

	@Override
	public synchronized List<Host> get() {
		try {
			setMyRegion();
			Map<String, Host> ipToHost = Maps.newHashMap();
			DescribeInstancesRequest req = new DescribeInstancesRequest();
			req.withFilters(filter);
			DescribeInstancesResult res = client.describeInstances(req);
			for (Reservation reservation : res.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					String privateIP = instance.getPrivateIpAddress();
					if (privateIP != null) {
						Host host = new Host(privateIP, port);
						ipToHost.put(privateIP, host);
					}
				}
			}

			ArrayList<Host> currentHosts = Lists.newArrayList(ipToHost.values());
			logHostUpdate(previousHosts, currentHosts);
			previousHosts = currentHosts;
			return previousHosts;
		} catch (AmazonClientException ex) {
			if (previousHosts == null) {
				throw new RuntimeException(ex);
			}
			LOG.warn("Failed to get hosts for sg: {} in region: {}.  Will use previously known hosts instead",
					Arrays.toString(filter.getValues().toArray()),
					region == null ? DEFAULT_REGION.toString() : region.toString());
			return previousHosts;
		}
	}

	private void logHostUpdate(List<Host> previous, List<Host> current) {

		if (current != null) {
			for (Host host : current) {
				if (previous == null || !previous.contains(host)) {
					LOG.info("Registering a possible node on IP {} with the C* connection pool", host.getIpAddress());
				}
			}
		}

		if (previous != null) {
			for (Host host : previous) {
				if (current == null || !current.contains(host)) {
					LOG.info("Removing a possible node on IP {} with the C* connection pool", host.getIpAddress());
				}
			}
		}

	}
}