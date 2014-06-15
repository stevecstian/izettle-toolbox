package com.izettle.cart;

import java.math.BigDecimal;

public interface Item<T> {

	BigDecimal getQuantity();

	long getUnitPrice();

	Float getVatPercentage();

	T inverse();
}
