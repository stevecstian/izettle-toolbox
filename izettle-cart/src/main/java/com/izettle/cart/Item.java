package com.izettle.cart;

import java.math.BigDecimal;

/**
 * An object that can be added to a cart's list of items
 * @param <T> The type of the item itself
 * @param <K> The type of the optional item local discount
 */
public interface Item<T, K extends Discount<?>> {

    /**
     * The quantity, or the number of units that this item represents
     * @return the quantity, cannot be null
     */
    BigDecimal getQuantity();

    /**
     * The cost per unit for this item
     * @return unit price
     */
    long getUnitPrice();

    /**
     * The percent VAT that is applied to this item, can be null for situations where VAT is not applicable
     * @return the vat percentage
     */
    Float getVatPercentage();

    /**
     * Utility method that subclasses need to implement. Inverse here, means the concept of negating the line, which
     * would normally be done by cloning it's own fields, but negating the sign on the quantity. Used for situations
     * such as refunds
     * @return the inversed Item
     */
    T inverse();

    /**
     * Returns the {@link Discount} applied to this item
     * @return
     */
    K getDiscount();

    /**
     * The identifier of the item in relation to other line items in the cart
     * @return the identifier of the item
     */
    Object getId();
}
