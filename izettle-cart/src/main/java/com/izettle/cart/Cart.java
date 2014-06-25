package com.izettle.cart;

import static com.izettle.cart.CartUtils.distributeDiscountedAmountOverDiscounts;
import static com.izettle.cart.CartUtils.distributeDiscountedAmountOverItems;
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
	private final long totalGrossAmount;
	private final Long totalDiscountAmount;
	private final Long totalEffectiveVat;
	private final Double totalEffectiveDiscountPercentage;

	public Cart(List<T> items, List<K> discounts) {
		if (empty(items)) {
			throw new IllegalArgumentException("Cannot create a cart with no items");
		}
		this.totalGrossAmount = CartUtils.getTotalGrossAmount(items);
		this.totalDiscountAmount = CartUtils.getTotalDiscountAmount(discounts, totalGrossAmount);
		final Map<Integer, Long> discountAmountByDiscountIdx;
		final Map<Integer, Long> discountAmountByItemIdx;
		if (totalDiscountAmount != null) {
			this.totalEffectiveDiscountPercentage = 100d * totalDiscountAmount / totalGrossAmount;
			discountAmountByItemIdx = distributeDiscountedAmountOverItems(items, totalDiscountAmount, totalGrossAmount);
			discountAmountByDiscountIdx = distributeDiscountedAmountOverDiscounts(
				discounts,
				totalDiscountAmount,
				totalGrossAmount
			);
		} else {
			this.totalEffectiveDiscountPercentage = null;
			discountAmountByItemIdx = null;
			discountAmountByDiscountIdx = null;
		}
		this.discountLines = CartUtils.buildDiscountLines(discounts, totalGrossAmount, discountAmountByDiscountIdx);
		this.itemLines = CartUtils.buildItemLines(items, discountAmountByItemIdx);
		this.totalEffectiveVat = CartUtils.summarizeEffectiveVat(itemLines);
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

	public long getTotalEffectivePrice() {
		return totalGrossAmount - ValueChecks.coalesce(totalDiscountAmount, 0L);
	}

	public List<ItemLine<T>> getItemLines() {
		return Collections.unmodifiableList(itemLines);
	}

	public List<DiscountLine<K>> getDiscountLines() {
		return Collections.unmodifiableList(discountLines);
	}

	public long getTotalGrossAmount() {
		return totalGrossAmount;
	}

	public Long getTotalDiscountAmount() {
		return totalDiscountAmount;
	}

	public Long getTotalEffectiveVat() {
		return totalEffectiveVat;
	}

	public Double getTotalEffectiveDiscountPercentage() {
		return totalEffectiveDiscountPercentage;
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
		sb.append("\tTotal Amount: ").append(this.getTotalGrossAmount()).append("\n");
		sb.append("\tTotal Discount Amount:").append(this.getTotalDiscountAmount()).append("\n");
		sb.append("Effective amounts:\n");
		sb.append("\tPrice:").append(this.getTotalEffectivePrice()).append("\n");
		sb.append("\tDiscount Percentage:").append(this.getTotalEffectiveDiscountPercentage()).append("\n");
		sb.append("\tVat: ").append(this.getTotalEffectiveVat()).append("\n");
		return sb.toString();
	}
}
