package com.izettle.cart;

import static com.izettle.cart.CartUtils.round;

import java.math.BigDecimal;

public class ItemUtils {

	private ItemUtils() {
	}

	public static long getGrossValue(Item item) {
		return round(item.getQuantity().multiply(new BigDecimal(item.getUnitPrice())));
	}
}
