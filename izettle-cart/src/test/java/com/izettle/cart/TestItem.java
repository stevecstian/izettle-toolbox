package com.izettle.cart;

import java.math.BigDecimal;

public class TestItem implements Item<TestItem, TestDiscount> {

	private final long unitPrice;
	private final Float vatPercentage;
	private final BigDecimal quantity;
	private final String name;
	private final TestDiscount discount;

	TestItem(String name, long unitPrice, Float vatPercentage, BigDecimal quantity, TestDiscount discount) {
		this.name = name;
		this.unitPrice = unitPrice;
		this.vatPercentage = vatPercentage;
		this.quantity = quantity;
		this.discount = discount;
	}

	TestItem(long unitPrice, Float vatPercentage, BigDecimal quantity) {
		this(null, unitPrice, vatPercentage, quantity, null);
	}

	@Override
	public long getUnitPrice() {
		return unitPrice;
	}

	@Override
	public Float getVatPercentage() {
		return vatPercentage;
	}

	@Override
	public String toString() {
		return "TestItem{"
			+ " unitPrice = " + unitPrice
			+ ", vatPercentage = " + vatPercentage
			+ ", quantity = " + quantity
			+ ", name = " + name
			+ ", discount = " + discount
			+ '}';
	}

	@Override
	public BigDecimal getQuantity() {
		return this.quantity;
	}

	@Override
	public TestItem inverse() {
		return new TestItem(name, unitPrice, vatPercentage, quantity.negate(), discount != null ? discount.inverse() : null);
	}

	@Override
	public TestDiscount getDiscount() {
		return discount;
	}
}

