package com.izettle.cart;

public class ItemLine<T extends Item<T>> {

	private final T item;
	private final long grossPrice;
	private final Long grossVat;
	private final long effectivePrice;
	private final Long effectiveVat;

	ItemLine(T item, long grossPrice, Long grossVat, long effectivePrice, Long effectiveVat) {
		this.item = item;
		this.grossPrice = grossPrice;
		this.grossVat = grossVat;
		this.effectivePrice = effectivePrice;
		this.effectiveVat = effectiveVat;
	}

	public T getItem() {
		return item;
	}

	public long getEffectivePrice() {
		return effectivePrice;
	}

	public Long getEffectiveVat() {
		return effectiveVat;
	}

	public long getGrossPrice() {
		return grossPrice;
	}

	public Long getGrossVat() {
		return grossVat;
	}

	@Override
	public String toString() {
		return ""
			+ "LineItem{"
			+ "item=" + item
			+ ", grossPrice=" + grossPrice
			+ ", grossVat=" + grossVat
			+ ", effectivePrice=" + effectivePrice
			+ ", effectiveVat=" + effectiveVat
			+ '}';
	}

}
