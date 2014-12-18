package com.izettle.tlv;

import com.izettle.java.ArrayUtils;
import com.izettle.java.Hex;

/**
 * Created by fidde on 16/12/14.
 */
public class TLV {

	private byte[] tag;
	private byte[] length;
	private byte[] value;

	public TLV(byte[] tag, byte[] length, byte[] value) {
		this.tag = tag;
		this.length = length;
		this.value = value;
	}

	public byte[] getTag() {
		return tag;
	}

	public byte[] getLength() {
		return length;
	}

	public byte[] getValue() {
		return value;
	}

	public byte[] toBytes() {
		return ArrayUtils.concat(tag, length, value);
	}

	@Override
	public String toString() {
		return String.format(
			"[T:%s L:%s V:%s]",
			Hex.toHexString(tag),
			Hex.toHexString(length),
			Hex.toHexString(value));
	};
}
