package com.izettle.cart;

public class ItemLine<T extends Item<T, K>, K extends Discount<K>> {

	private final T item;
	private final long grossValue;
	private final Long grossVat;
	private final long actualValue;
	private final Long actualVat;

	ItemLine(T item, long grossValue, Long grossVat, long actualValue, Long actualVat) {
		this.item = item;
		this.grossValue = grossValue;
		this.grossVat = grossVat;
		this.actualValue = actualValue;
		this.actualVat = actualVat;
	}

	public T getItem() {
		return item;
	}

	public long getActualValue() {
		return actualValue;
	}

	public Long getActualVat() {
		return actualVat;
	}

	public long getGrossValue() {
		return grossValue;
	}

	public Long getGrossVat() {
		return grossVat;
	}

	@Override
	public String toString() {
		return ""
			+ "LineItem {"
			+ " item = " + item
			+ ", grossValue = " + grossValue
			+ ", grossVat = " + grossVat
			+ ", actualValue = " + actualValue
			+ ", actualVat = " + actualVat
			+ '}';
	}

}
