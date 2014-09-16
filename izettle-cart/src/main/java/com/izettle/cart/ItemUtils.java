package com.izettle.cart;

import static com.izettle.cart.CartUtils.getNonRoundedDiscountValue;
import static com.izettle.cart.CartUtils.round;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;

public class ItemUtils {

	private ItemUtils() {
	}

	/**
	 * Calculates the gross value of an item. This value will be the local value of an isolated item with it's optional
	 * local discounts taken into consideration, but with no awareness of possible cart-wide side effects
	 *
	 * @param item the Item to calculate the value for
	 * @return the isolated value of this item
	 */
	public static long getGrossValue(Item item) {
		BigDecimal valueBeforeDiscounts = getValueBeforeDiscounts(item);
		long originalGrossValue = round(valueBeforeDiscounts);
		return originalGrossValue - coalesce(getDiscountValue(item), 0L);
	}

	/**
	 * Calculates the value of the item line discount.
	 *
	 * @param item the Item to calculate the value for
	 * @return the line item discount value.
	 */
	public static Long getDiscountValue(Item item) {
		Discount discount = item.getDiscount();
		if (!empty(discount)) {
			BigDecimal valueBeforeDiscounts = getValueBeforeDiscounts(item);
			return round(getNonRoundedDiscountValue(discount, valueBeforeDiscounts));
		}
		return null;
	}

	private static BigDecimal getValueBeforeDiscounts(Item item) {
		return item.getQuantity().multiply(BigDecimal.valueOf(item.getUnitPrice()));
	}
}
