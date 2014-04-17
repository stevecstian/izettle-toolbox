package com.izettle.cart;

import java.math.BigDecimal;

public interface Item {

	BigDecimal getQuantity();

	long getUnitPrice();

	Double getVatPercentage();

}
