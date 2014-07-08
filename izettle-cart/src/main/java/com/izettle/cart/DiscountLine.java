package com.izettle.cart;

public class DiscountLine<K extends Discount<K>> {

	private final K discount;
	private final Double actualPercentage;
	private final Long value;

	DiscountLine(K discount, Double actualPercentage, Long value) {
		this.discount = discount;
		this.actualPercentage = actualPercentage;
		this.value = value;
	}

	public K getDiscount() {
		return discount;
	}

	/**
	 * Percentage of the cart gross amount that this line represents. This is the combination of the discounts
	 * percentage and amount, multiplied with the quantity.
	 * @return the percentage, or null of there is no discount
	 */
	public Double getActualPercentage() {
		return actualPercentage;
	}

	public Long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return ""
			+ "DiscountLine {"
			+ " discount = " + discount
			+ ", actualPercentage = " + actualPercentage
			+ ", value = " + value
			+ '}';
	}
}
