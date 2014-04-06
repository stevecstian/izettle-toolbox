package com.izettle.cart;

import java.math.BigDecimal;

public class ItemLine<T extends Item> {

	private final T item;
	private final BigDecimal quantity;
	private final long grossPrice;
	private final long effectivePrice;
	private final Long effectiveVat;

	ItemLine(T item, BigDecimal quantity, long grossPrice, long effectivePrice, Long effectiveVat) {
		this.item = item;
		this.quantity = quantity;
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

	public BigDecimal getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return "LineItem{" + "item=" + item + ", quantity=" + quantity + ", grossPrice=" + grossPrice + ", effectivePrice=" + effectivePrice + ", effectiveVat=" + effectiveVat + '}';
	}

}
