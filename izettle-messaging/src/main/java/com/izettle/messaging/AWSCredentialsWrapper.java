package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import java.io.IOException;
import java.io.InputStream;

public class AWSCredentialsWrapper {
	
	public static AWSCredentials getCredentials(InputStream credentialsInputStream) throws MessagingException {
		try {
			return new PropertiesCredentials(credentialsInputStream);
		} catch (IOException e) {
			throw new MessagingException("Could not read properties", e);
		}
	}
	
	public static AWSCredentials getCredentials(String accessKey, String secretKey) throws MessagingException {
		return new BasicAWSCredentials(accessKey, secretKey);
	}
}
