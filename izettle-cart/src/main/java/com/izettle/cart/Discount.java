package com.izettle.cart;

import java.math.BigDecimal;

public interface Discount<T> {

	BigDecimal getQuantity();

	Long getAmount();

	Double getPercentage();

	T inverse();
}
