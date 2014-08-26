package com.izettle.cart;

import java.math.BigDecimal;

public interface Item<T, K extends Discount<?>> {

	BigDecimal getQuantity();

	long getUnitPrice();

	Float getVatPercentage();

	T inverse();

	/**
	 * Returns the discount applied to this item
	 * @return
	 */
	K getDiscount();
}
