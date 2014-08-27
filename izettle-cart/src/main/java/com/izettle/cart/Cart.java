package com.izettle.cart;

import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

public class Cart<T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>> {

	private final List<ItemLine<T, D>> itemLines;
	private final List<DiscountLine<K>> discountLines;
	private final long grossValue;
	private final Long discountValue;
	private final Long actualVat;
	private final Double actualDiscountPercentage;
	private final Long grossVat;

	/**
	 * Produces a new immutable cart object from two lists if Items and Discounts
	 * @param items the list of items, must not be empty (as a cart without items makes no sense)
	 * @param discounts the list of cart wide discounts, possibly empty
	 */
	public Cart(List<T> items, List<K> discounts) {
		if (items == null) {
			items = Collections.emptyList();
		}
		this.grossValue = CartUtils.getGrossValue(items);
		this.discountValue = CartUtils.getTotalDiscountValue(discounts, grossValue);
		this.actualDiscountPercentage = CartUtils.getDiscountPercentage(grossValue, discountValue);
		this.discountLines = CartUtils.buildDiscountLines(discounts, grossValue, discountValue);
		this.itemLines = CartUtils.buildItemLines(items, grossValue, discountValue);
		this.grossVat = CartUtils.summarizeGrossVat(itemLines);
		this.actualVat = CartUtils.summarizeEffectiveVat(itemLines);
	}

	/**
	 * Produces a new cart that is inversed, eg an identical cart where all quantities are negated. Useful for example
	 * for refunds
	 * @return the inversed cart
	 */
	public Cart<T, D, K> inverse() {
		//Copy all items and discounts, but negate the quantities:
		List<T> inverseItems;
		List<K> inverseDiscounts;
		if (empty(itemLines)) {
			inverseItems = Collections.emptyList();
		} else {
			inverseItems = new ArrayList<T>(itemLines.size());
			for (ItemLine<T, D> itemLine : itemLines) {
				inverseItems.add(itemLine.getItem().inverse());
			}
		}
		if (empty(discountLines)) {
			inverseDiscounts = Collections.emptyList();
		} else {
			inverseDiscounts = new ArrayList<K>(discountLines.size());
			for (DiscountLine<K> discountLine : discountLines) {
				inverseDiscounts.add(discountLine.getDiscount().inverse());
			}
		}
		return new Cart<T, K>(inverseItems, inverseDiscounts);
	}

	/**
	 * The actual value of the cart, when all kinds of discounts has been taken into consideration
	 * @return the actual value of the cart
	 */
	public long getValue() {
		return grossValue - coalesce(discountValue, 0L);
	}

	/**
	 * The carts list of items lines. Each provided item at construction time will have it's own line, Each line will
	 * contain additional information, specific to the context of this cart, such as actual amounts and VATs
	 * @return an immutable list of item lines
	 */
	public List<ItemLine<T, D>> getItemLines() {
		return Collections.unmodifiableList(itemLines);
	}

	/**
	 * The carts list of discount lines. Each provided discount at construction time will have it's own line, Each line
	 * contain additional information, specific to the context of this cart, such as actual amounts
	 * @return an immutable list of discount lines
	 */
	public List<DiscountLine<K>> getDiscountLines() {
		return Collections.unmodifiableList(discountLines);
	}

	/**
	 * The total value of this cart before cart wide discounts has been applied
	 * @return the gross value of the cart
	 */
	public long getGrossValue() {
		return grossValue;
	}

	/**
	 * The actual value of all cart wide discounts, where percentage discounts has been translated to amounts
	 * @return the amount of cart-wide discounts
	 */
	public Long getDiscountValue() {
		return discountValue;
	}

	/**
	 * The actual VAT amount for this cart. That is the sum of all item lines's vat percentage applied to each lines
	 * actual value
	 * @return The actual VAT amount for this cart, or null of VAT is not applicable
	 */
	public Long getActualVat() {
		return actualVat;
	}

	/**
	 * The VAT amount for this cart should there be no cart wide discounts.
	 * @return The VAT amount for this cart before cart wide discounts, or null if VAT is not applicable
	 */
	public Long getGrossVat() {
		return grossVat;
	}

	/**
	 * The amount VAT that's "lost" after applying cart-wide discounts, eg the difference between the gross VAT and
	 * actual VAT
	 * @return the discount VAT, or null if VAT is not applicable
	 */
	public Long getDiscountVat() {
		if (anyEmpty(grossVat, actualVat, discountValue)) {
			return null;
		}
		return grossVat - actualVat;
	}

	/**
	 * Given multiple cart-wide discounts, using both percentages and amounts, this is the actual percentage applied to
	 * the cart's gross amount. This value won't always be identical to the provided percentage, even if there is only
	 * one cart-wide discount with only percentage. This is because rounding takes place per line. The purpose of this
	 * method is to be able to get the resulting data. As an counter example, if only discounts with amounts are
	 * provided, the returned value in getDiscountValue() will always be the expected.
	 * @return the actual percentage the cart-wide discounts represents of the cart's gross value, or null if VAT is not
	 * applicable
	 */
	public Double getActualDiscountPercentage() {
		return actualDiscountPercentage;
	}

	/**
	 * Will produce a summary of data per used VAT percentage, returned mapped by it's respective VAT percentage
	 * @return the map of values by percentage, or empty maps if VAT is not applicable
	 */
	public SortedMap<Float, VatGroupValues> groupValuesByVatPercentage() {
		return CartUtils.groupValuesByVatPercentage(getItemLines());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cart {\n");
		sb.append("\tLineItems:\n");
		for (ItemLine<T, D> itemLine : itemLines) {
			sb.append("\t\t").append(itemLine).append("\n");
		}
		sb.append("\tDiscounts:\n");
		for (DiscountLine<K> discountLine : discountLines) {
			sb.append("\t\t").append(discountLine).append("\n");
		}
		sb.append("\tGross Amounts:\n");
		sb.append("\t\tGross Value: ").append(this.getGrossValue()).append("\n");
		sb.append("\t\tGross VAT: ").append(this.getGrossVat()).append("\n");
		sb.append("\tActual Amounts:\n");
		sb.append("\t\tValue: ").append(this.getValue()).append("\n");
		sb.append("\t\tDiscount Value: ").append(this.getDiscountValue()).append("\n");
		sb.append("\t\tDiscount Percentage: ").append(this.getActualDiscountPercentage()).append("\n");
		sb.append("\t\tVAT: ").append(this.getActualVat()).append("\n");
		sb.append('}');
		return sb.toString();
	}
}
