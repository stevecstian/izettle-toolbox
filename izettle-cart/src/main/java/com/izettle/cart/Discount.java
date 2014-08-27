package com.izettle.cart;

import java.math.BigDecimal;

/**
 * An object that can be added to a carts list of discounts
 * @param <T> The type of the discount itself
 */
public interface Discount<T> {

	/**
	 * The quantity, or the number of units that this discount represents
	 * @return the quantity, cannot be null
	 */
	BigDecimal getQuantity();

	/**
	 * The amount that this discount affects is target with
	 * @return The amount, can be null if the percentage is not
	 */
	Long getAmount();

	/**
	 * The percentage that this discount affects is target with. The actual effect will depend on properties on the
	 * target, such as it's gross amount
	 * @return The percentage, can be null if the amount is not
	 */
	Double getPercentage();

	/**
	 * Utility method that subclasses need to implement. Inverse here, means the concept of negating the discount, which
	 * would normally be done by cloning it's own fields, but negating the sign on the quantity. Used for situations
	 * such as refunds
	 * @return the inversed Discount
	 */
	T inverse();
}
