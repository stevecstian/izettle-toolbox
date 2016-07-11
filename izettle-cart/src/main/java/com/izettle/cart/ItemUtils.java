package com.izettle.cart;

import static com.izettle.cart.CartUtils.coalesce;
import static com.izettle.cart.CartUtils.getRoundedDiscountValue;
import static com.izettle.cart.CartUtils.round;

import java.math.BigDecimal;
import java.util.Collection;

class ItemUtils {

    private ItemUtils() {
    }

    /**
     * Returns the gross value of the item. Gross is the quantity multiplied with unit price
     * and rounded to a long using {@link CartUtils#round(BigDecimal)}, VAT is included.
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
        if (discount != null) {
            return getRoundedDiscountValue(discount, getGrossValue(item));
        }
        return null;
    }

    private static BigDecimal getExactGrossValue(final Item item) {
        return item.getQuantity().multiply(BigDecimal.valueOf(item.getUnitPrice()));
    }

    static <T extends Item> void validateItems(final Collection<T> items) {
        if (items == null) {
            return;
        }
        for (T item : items) {
            validateItem(item);
        }
    }

    static <T extends Item> void validateItem(final T item) {
        if (item.getQuantity() == null) {
            throw new IllegalArgumentException("Item cannot have null quantity: " + item);
        }
        if (item.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Item cannot have ZERO quantity: " + item);
        }
    }
}
