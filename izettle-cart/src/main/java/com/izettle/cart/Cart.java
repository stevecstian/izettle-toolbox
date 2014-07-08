package com.izettle.cart;

import static com.izettle.cart.CartUtils.distributeDiscountedAmountOverDiscounts;
import static com.izettle.cart.CartUtils.distributeDiscountedAmountOverItems;
import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;

import com.izettle.java.ValueChecks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class Cart<T extends Item<T>, K extends Discount<K>> {

	private final List<ItemLine<T>> itemLines;
	private final List<DiscountLine<K>> discountLines;
	private final long grossAmount;
	private final Long discountAmount;
	private final Long effectiveVat;
	private final Double effectiveDiscountPercentage;
	private final Long grossVatAmount;

	public Cart(List<T> items, List<K> discounts) {
		if (empty(items)) {
			throw new IllegalArgumentException("Cannot create a cart with no items");
		}
		this.grossAmount = CartUtils.getGrossAmount(items);
		this.discountAmount = CartUtils.getDiscountAmount(discounts, grossAmount);
		final Map<Integer, Long> discountAmountByDiscountIdx;
		final Map<Integer, Long> discountAmountByItemIdx;
		if (discountAmount != null) {
			this.effectiveDiscountPercentage = 100d * discountAmount / grossAmount;
			discountAmountByItemIdx = distributeDiscountedAmountOverItems(items, discountAmount, grossAmount);
			discountAmountByDiscountIdx = distributeDiscountedAmountOverDiscounts(
				discounts,
				discountAmount,
				grossAmount
			);
		} else {
			this.effectiveDiscountPercentage = null;
			discountAmountByItemIdx = null;
			discountAmountByDiscountIdx = null;
		}
		this.discountLines = CartUtils.buildDiscountLines(discounts, grossAmount, discountAmountByDiscountIdx);
		this.itemLines = CartUtils.buildItemLines(items, discountAmountByItemIdx);
		this.grossVatAmount = CartUtils.summarizeGrossVat(itemLines);
		this.effectiveVat = CartUtils.summarizeEffectiveVat(itemLines);
	}

	public Cart<T, K> inverse() {
		//Copy all items and discounts, but negate the quantities:
		List<T> inverseItems;
		List<K> inverseDiscounts;
		if (empty(itemLines)) {
			inverseItems = Collections.EMPTY_LIST;
		} else {
			inverseItems = new ArrayList<T>(itemLines.size());
			for (ItemLine<T> itemLine : itemLines) {
				inverseItems.add(itemLine.getItem().inverse());
			}
		}
		if (empty(discountLines)) {
			inverseDiscounts = Collections.EMPTY_LIST;
		} else {
			inverseDiscounts = new ArrayList<K>(discountLines.size());
			for (DiscountLine<K> discountLine : discountLines) {
				inverseDiscounts.add(discountLine.getDiscount().inverse());
			}
		}
		return new Cart(inverseItems, inverseDiscounts);
	}

	public long getEffectivePrice() {
		return grossAmount - ValueChecks.coalesce(discountAmount, 0L);
	}

	public List<ItemLine<T>> getItemLines() {
		return Collections.unmodifiableList(itemLines);
	}

	public List<DiscountLine<K>> getDiscountLines() {
		return Collections.unmodifiableList(discountLines);
	}

	public long getGrossAmount() {
		return grossAmount;
	}

	public Long getDiscountAmount() {
		return discountAmount;
	}

	public Long getEffectiveVat() {
		return effectiveVat;
	}

	public Long getGrossVatAmount() {
		return grossVatAmount;
	}

	public Long getDiscountVatAmount() {
		if (anyEmpty(grossVatAmount, effectiveVat, discountAmount)) {
			return null;
		}
		return grossVatAmount - effectiveVat;
	}

	public Double getEffectiveDiscountPercentage() {
		return effectiveDiscountPercentage;
	}

	public SortedMap<Float, Long> groupEffectiveVatAmounts() {
		return CartUtils.groupEffectiveVatAmounts(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Items:\n");
		for (ItemLine<T> itemLine : itemLines) {
			sb.append("\t").append(itemLine).append("\n");
		}
		sb.append("Discounts:\n");
		for (DiscountLine<K> discountLine : discountLines) {
			sb.append("\t").append(discountLine).append("\n");
		}
		sb.append("Gross Amounts:\n");
		sb.append("\tGross Amount: ").append(this.getGrossAmount()).append("\n");
		sb.append("\tDiscount Amount:").append(this.getDiscountAmount()).append("\n");
		sb.append("Amounts:\n");
		sb.append("\tPrice:").append(this.getEffectivePrice()).append("\n");
		sb.append("\tDiscount Percentage:").append(this.getEffectiveDiscountPercentage()).append("\n");
		sb.append("\tVat: ").append(this.getEffectiveVat()).append("\n");
		return sb.toString();
	}
}
