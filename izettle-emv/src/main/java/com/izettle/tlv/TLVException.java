package com.izettle.tlv;

/**
 * Created by fidde on 16/12/14.
 */
public class TLVException extends Exception {

	public TLVException(String str) {
		super(str);
	}

	public TLVException(String str, Throwable t) {
		super(str, t);
	}
}
