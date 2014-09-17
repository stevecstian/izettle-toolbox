package com.izettle.cart;

import static com.izettle.cart.CartUtils.getNonRoundedDiscountValue;
import static com.izettle.cart.CartUtils.round;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;

/**
 * An object that can be added to a carts list of items
 * @param <T> The type of the item itself
 * @param <K> The type of the optional item local discount
 */
public abstract class Item<T, K extends Discount<?>> {

	/**
	 * The quantity, or the number of units that this item represents
	 * @return the quantity, cannot be null
	 */
	abstract BigDecimal getQuantity();

	/**
	 * The cost per unit for this item
	 * @return unit price
	 */
	abstract long getUnitPrice();

	/**
	 * The percent VAT that is applied to this item, can be null for situations where VAT is not applicable
	 * @return the vat percentage
	 */
	abstract Float getVatPercentage();

	/**
	 * Utility method that subclasses need to implement. Inverse here, means the concept of negating the line, which
	 * would normally be done by cloning it's own fields, but negating the sign on the quantity. Used for situations
	 * such as refunds
	 * @return the inversed Item
	 */
	abstract T inverse();

	/**
	 * Returns the discount applied to this item
	 * @return
	 */
	abstract K getDiscount();

	/**
	 * Calculates the gross value of an item. This value will be the local value of an isolated item with it's optional
	 * local discounts taken into consideration, but with no awareness of possible cart-wide side effects
	 *
	 * @return the isolated value of this item
	 */
	public long getGrossValue() {
		BigDecimal valueBeforeDiscounts = getValueBeforeDiscounts();
		long originalGrossValue = round(valueBeforeDiscounts);
		return originalGrossValue - coalesce(getDiscountValue(), 0L);
	}

	/**
	 * Calculates the value of the item line discount.
	 *
	 * @return the line item discount value.
	 */
	public Long getDiscountValue() {
		Discount discount = getDiscount();
		if (!empty(discount)) {
			BigDecimal valueBeforeDiscounts = getValueBeforeDiscounts();
			return round(getNonRoundedDiscountValue(discount, valueBeforeDiscounts));
		}
		return null;
	}

	private BigDecimal getValueBeforeDiscounts() {
		return getQuantity().multiply(BigDecimal.valueOf(getUnitPrice()));
	}
}
