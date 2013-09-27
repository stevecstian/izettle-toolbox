package com.izettle.messaging;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public class MessagingException extends Exception {

	private static final long serialVersionUID = 1L;
	private String message;

	public MessagingException(String message, Throwable cause) {
		super(cause);
		this.message = message;
		if (cause instanceof AmazonServiceException) {
			this.message += buildAmazonServerExceptionMessage((AmazonServiceException) cause);
		} else if (cause instanceof AmazonClientException) {
			this.message += buildAmazonClientExceptionMessage((AmazonClientException) cause);
		}
	}

	public MessagingException(String message) {
		super(message);
	}

	private static String buildAmazonClientExceptionMessage(AmazonClientException ace) {
		StringBuilder sb = new StringBuilder();
		sb.append("Caught an AmazonClientException, which means the client encountered " +
				"a serious internal problem while trying to communicate with SQS, such as not " +
				"being able to access the network.");
		sb.append("Error Message: ").append(ace.getMessage());
		return sb.toString();
	}

	private static String buildAmazonServerExceptionMessage(AmazonServiceException ase) {
		StringBuilder sb = new StringBuilder();
		sb.append("Caught an AmazonServiceException, which means your request made it "
				+ "to Amazon SQS, but was rejected with an error response for some reason.");
		sb.append("Error Message:    ").append(ase.getMessage());
		sb.append("HTTP Status Code: ").append(ase.getStatusCode());
		sb.append("AWS Error Code:   ").append(ase.getErrorCode());
		sb.append("Error Type:       ").append(ase.getErrorType());
		sb.append("Request ID:       ").append(ase.getRequestId());
		return sb.toString();
	}

	@Override
	public final String getMessage() {
		return super.getMessage() + "\n" + message;
	}

}
