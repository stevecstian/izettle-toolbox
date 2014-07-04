package com.izettle.cart;

public class DiscountLine<K extends Discount<K>> {

	private final K discount;
	private final Double effectivePercentage;
	private final Long effectiveDiscountAmount;

	DiscountLine(K discount, Double effectivePercentage, Long effectiveDiscountAmount) {
		this.discount = discount;
		this.effectivePercentage = effectivePercentage;
		this.effectiveDiscountAmount = effectiveDiscountAmount;
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

	public Long getEffectiveDiscountAmount() {
		return effectiveDiscountAmount;
	}

	@Override
	public String toString() {
		return ""
			+ "DiscountLine{"
			+ "discount=" + discount
			+ ", effectivePercentage=" + effectivePercentage
			+ ", effectiveDiscountAmount=" + effectiveDiscountAmount
			+ '}';
	}
}
