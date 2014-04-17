package com.izettle.cart;

public class ItemLine<T extends Item> {

	private final T item;
	private final long grossPrice;
	private final long effectivePrice;
	private final Long effectiveVat;

	ItemLine(T item, long grossPrice, long effectivePrice, Long effectiveVat) {
		this.item = item;
		this.grossPrice = grossPrice;
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

	@Override
	public String toString() {
		return "LineItem{" + "item=" + item + ", grossPrice=" + grossPrice + ", effectivePrice=" + effectivePrice + ", effectiveVat=" + effectiveVat + '}';
	}

}
