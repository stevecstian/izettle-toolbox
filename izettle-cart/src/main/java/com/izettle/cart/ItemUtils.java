package com.izettle.cart;

import static com.izettle.cart.CartUtils.getNonRoundedDiscountValue;
import static com.izettle.cart.CartUtils.round;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;

public class ItemUtils {

	private ItemUtils() {
	}

	/**
	 * Calculates the gross value of an item. This value will be the local value of an isolated item with it's optional
	 * local discounts taken into consideration, but with no awareness of possible cart-wide side effects
	 * @param item the Item to calculate the value for
	 * @return the isolated value of this item
	 */
	public static long getGrossValue(Item item) {
		BigDecimal exactValue = item.getQuantity().multiply(BigDecimal.valueOf(item.getUnitPrice()));
		Discount discount = item.getDiscount();
		if (!empty(discount)) {
			/*
			 * We have a discount on this single line. Just apply the discount locally before rounding and present the
			 * gross amount as if all else normal
			 */
			return round(exactValue.subtract(getNonRoundedDiscountValue(discount, exactValue)));
		}
		return round(exactValue);
	}
}
