package com.izettle.cart;

import static com.izettle.cart.CartUtils.getNonRoundedDiscountValue;
import static com.izettle.cart.CartUtils.round;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;

/**
 * An object that can be added to a cart's list of items
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
	 * Returns the {@link com.izettle.cart.Discount} applied to this item
	 * @return
	 */
	abstract K getDiscount();

	/**
	 * Returns the gross value of the item. Gross is the {@link #getQuantity()} multiplied with {@link #getUnitPrice()}
	 * and rounded to a long using {@link com.izettle.cart.CartUtils#round(java.math.BigDecimal)}, VAT is included.
	 *
	 * @return the gross value
	 */
	public long getGrossValue() {
		BigDecimal valueBeforeDiscounts = getExactGrossValue();
		return round(valueBeforeDiscounts);
	}

	/**
	 * Calculates the value by subtracting {@link #getGrossValue()} with {@link #getDiscountValue()}.
	 * This value will be the local value of an isolated item with it's optional local discounts taken into
	 * consideration, but with no awareness of possible cart-wide side effects.
	 *
	 * @return the value
	 */
	public long getValue() {
		return getGrossValue() - coalesce(getDiscountValue(), 0L);
	}

	/**
	 * Calculates the value of the item line discount.
	 *
	 * @return the line item discount value.
	 */
	public Long getDiscountValue() {
		Discount discount = getDiscount();
		if (!empty(discount)) {
			BigDecimal valueBeforeDiscounts = getExactGrossValue();
			return round(getNonRoundedDiscountValue(discount, valueBeforeDiscounts));
		}
		return null;
	}

	private BigDecimal getExactGrossValue() {
		return getQuantity().multiply(BigDecimal.valueOf(getUnitPrice()));
	}
}
