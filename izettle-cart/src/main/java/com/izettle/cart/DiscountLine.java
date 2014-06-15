package com.izettle.cart;

public class DiscountLine<K extends Discount<K>> {

	private final K discount;
	private final Double effectivePercentage;

	DiscountLine(K discount, Double effectivePercentage) {
		this.discount = discount;
		this.effectivePercentage = effectivePercentage;
	}

	public K getDiscount() {
		return discount;
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
			+ ", effectivePercentage=" + effectivePercentage
			+ '}';
	}

}
