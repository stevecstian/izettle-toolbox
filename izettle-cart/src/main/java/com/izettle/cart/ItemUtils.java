package com.izettle.cart;

import static com.izettle.cart.CartUtils.round;

import java.math.BigDecimal;

public class ItemUtils {

	private ItemUtils() {
	}

	public static long grossValue(Item item) {
		return round(item.getQuantity().multiply(new BigDecimal(item.getUnitPrice())));
	}

	/**
	 * VAT amount on specific item before discounts.
	 *
	 * @param item Item.
	 * @return VAT amount for item before discounts, or null if VAT not specified on item.
	 */
	public static Long grossVatValue(Item item) {

		if (item.getVatPercentage() == null) {
			return null;
		}

		long grossValue = grossValue(item);

		return grossValue - round((grossValue * 100) / (100 + (double) item.getVatPercentage()));
	}
}
