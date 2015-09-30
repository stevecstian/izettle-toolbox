package com.izettle.cart;

import static com.izettle.cart.CartUtils.getNonRoundedDiscountValue;
import static com.izettle.cart.CartUtils.round;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;

class ItemUtils {
    /**
     * Returns the gross value of the item. Gross is the quantity multiplied with unit price
     * and rounded to a long using {@link com.izettle.cart.CartUtils#round(java.math.BigDecimal)}, VAT is included.
     *
     * @return the gross value
     */
    static long getGrossValue(final Item item) {
        final BigDecimal valueBeforeDiscounts = getExactGrossValue(item);
        return round(valueBeforeDiscounts);
    }

    /**
     * Calculates the value by subtracting gross value with discount value.
     * This value will be the local value of an isolated item with it's optional local discounts taken into
     * consideration, but with no awareness of possible cart-wide side effects.
     *
     * @return the value
     */
    static long getValue(final Item item) {
        return getGrossValue(item) - coalesce(getDiscountValue(item), 0L);
    }

    /**
     * Calculates the value of the item line discount.
     *
     * @return the line item discount value.
     */
    static Long getDiscountValue(final Item item) {
        Discount discount = item.getDiscount();
        if (!empty(discount)) {
            BigDecimal valueBeforeDiscounts = getExactGrossValue(item);
            return round(getNonRoundedDiscountValue(discount, valueBeforeDiscounts));
        }
        return null;
    }


    private static BigDecimal getExactGrossValue(final Item item) {
        return item.getQuantity().multiply(BigDecimal.valueOf(item.getUnitPrice()));
    }
}
