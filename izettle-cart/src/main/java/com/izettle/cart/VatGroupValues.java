package com.izettle.cart;

public class VatGroupValues {
	private final float vatPercentage;
	private final long actualVatValue;
	private final long actualValue;

	protected VatGroupValues(float vatPercentage, long actualVatValue, long actualValue) {
		this.vatPercentage = vatPercentage;
		this.actualVatValue = actualVatValue;
		this.actualValue = actualValue;
	}

	public float getVatPercentage() {
		return vatPercentage;
	}

	public long getActualVatValue() {
		return actualVatValue;
	}

	public long getActualValue() {
		return actualValue;
	}
}
