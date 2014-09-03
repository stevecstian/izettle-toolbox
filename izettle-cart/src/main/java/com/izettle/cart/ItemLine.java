package com.izettle.cart;

/**
 * Representing a line of a item in a cart. The line consists of it's Item member (of type &lt;K, T&gt; originally
 * provided by the caller), and some calculated values that's relevant to it's context and location in the cart
 * @param <T> The type of the Item itself
 * @param <K> The type of the optional Discount that might be associated with the provided Item
 */
public class ItemLine<T extends Item<T, K>, K extends Discount<K>> {

	private final T item;
	private final long grossValue;
	private final Long grossVat;
	private final long actualValue;
	private final Long actualVat;

	ItemLine(T item, long grossValue, Long grossVat, long actualValue, Long actualVat) {
		this.item = item;
		this.grossValue = grossValue;
		this.grossVat = grossVat;
		this.actualValue = actualValue;
		this.actualVat = actualVat;
	}

	/**
	 * Returns the Item object originally provided at creation time. This is a utility function, enabling the calling
	 * code to keep references to it's own data structures, while allowing this library to still perform all financial
	 * calculations
	 * @return The originally provided item object
	 */
	public T getItem() {
		return item;
	}

	/**
	 * The actual value that this line represents in the cart. This is the gross value (quantity X unit price),
	 * plus/minus possible line local discounts, and plus/minus cart wide discounts
	 * @return The actual value that this line represents
	 */
	public long getActualValue() {
		return actualValue;
	}

	/**
	 * The actual VAT amount that this line represents in the cart. That is the item's vat percentage applied to the
	 * line's actualValue
	 * @return The actual VAT amount that this line represents in the cart, or null if VAT is not applicable for this
	 * line
	 */
	public Long getActualVat() {
		return actualVat;
	}

	/**
	 * The amount that this line represents on it's own, e.g. the raw gross value plus/minus it's line local discount
	 * @return The value this line represents before cart wide discounts are applied
	 */
	public long getGrossValue() {
		return grossValue;
	}

	/**
	 * The VAT amount that this line would have represented should there be no cart wide discounts.
	 * @return The VAT amount for this line before cart wide discounts, or null if VAT is not applicable for this line
	 */
	public Long getGrossVat() {
		return grossVat;
	}

	@Override
	public String toString() {
		return ""
			+ "LineItem {"
			+ " item = " + item
			+ ", grossValue = " + grossValue
			+ ", grossVat = " + grossVat
			+ ", actualValue = " + actualValue
			+ ", actualVat = " + actualVat
			+ '}';
	}

}
