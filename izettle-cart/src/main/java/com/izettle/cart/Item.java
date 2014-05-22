package com.izettle.cart;

import java.math.BigDecimal;

public interface Item<T> {

	BigDecimal getQuantity();

	long getUnitPrice();

	Double getVatPercentage();

	T inverse();
}
