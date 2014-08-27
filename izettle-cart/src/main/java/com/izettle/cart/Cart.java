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

	public long getValue() {
		return grossValue - coalesce(discountValue, 0L);
	}

	public List<ItemLine<T, D>> getItemLines() {
		return Collections.unmodifiableList(itemLines);
	}

	public List<DiscountLine<K>> getDiscountLines() {
		return Collections.unmodifiableList(discountLines);
	}

	public long getGrossValue() {
		return grossValue;
	}

	public Long getDiscountValue() {
		return discountValue;
	}

	public Long getActualVat() {
		return actualVat;
	}

	public Long getGrossVat() {
		return grossVat;
	}

	public Long getDiscountVat() {
		if (anyEmpty(grossVat, actualVat, discountValue)) {
			return null;
		}
		return grossVat - actualVat;
	}

	public Double getActualDiscountPercentage() {
		return actualDiscountPercentage;
	}

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
