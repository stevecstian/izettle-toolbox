package com.izettle.cart;

import java.math.BigDecimal;
import java.util.Collection;

class DiscountUtils {

    private DiscountUtils() {
    }

    static <D extends Discount> void validateDiscounts(final Collection<D> discounts) {
        if (discounts == null) {
            return;
        }
        for (D item : discounts) {
            validateDiscount(item);
        }
    }

    private static <D extends Discount> void validateDiscount(final D discount) {
        if (discount.getQuantity() == null) {
            throw new IllegalArgumentException("Discount cannot have null quantity: " + discount);
        }
        if (discount.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Discount cannot have ZERO quantity: " + discount);
        }
    }
}
