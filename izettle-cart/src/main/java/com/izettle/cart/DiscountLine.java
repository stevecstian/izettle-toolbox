package com.izettle.cart;

import java.math.BigDecimal;

public class DiscountLine<K extends Discount> {

	private final K discount;
	private final BigDecimal quantity;
	private final Double effectivePercentage;

	public DiscountLine(K discount, BigDecimal quantity, Double effectivePercentage) {
		this.discount = discount;
		this.quantity = quantity;
		this.effectivePercentage = effectivePercentage;
	}

	public K getDiscount() {
		return discount;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	/**
	 * Percentage of the card gross amount that this line represents. This is the combination of the
	 * discounts percentage and amount, multiplied with the quantity.
	 * @return the percentage, or null of there is no discount
	 */
	public Double getEffectivePercentage() {
		return effectivePercentage;
	}

	@Override
	public String toString() {
		return ""
				+ "DiscountLine{"
				+ "discount=" + discount
				+ ", quantity=" + quantity
				+ ", effectivePercentage=" + effectivePercentage
				+ '}';
	}

}
