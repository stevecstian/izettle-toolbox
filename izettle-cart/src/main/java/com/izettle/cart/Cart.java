package com.izettle.cart;

import static com.izettle.cart.CartUtils.distributeDiscount;
import static com.izettle.java.ValueChecks.empty;

import com.izettle.java.ValueChecks;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Cart<T extends Item, K extends Discount> {

	private final List<ItemLine<T>> itemLines;
	private final List<DiscountLine<K>> discountLines;
	private final long totalGrossAmount;
	private final Long totalDiscountAmount;
	private final Long totalEffectiveVat;
	private final Double totalEffectiveDiscountPercentage;

	public Cart(List<T> items, Map<K, BigDecimal> discounts) {
		if (empty(items)) {
			throw new IllegalArgumentException("Cannot create a cart with no items");
		}
		this.totalGrossAmount = CartUtils.getTotalGrossAmount(items);
		this.discountLines = CartUtils.buildDiscountLines(discounts, totalGrossAmount);

		this.totalDiscountAmount = CartUtils.getTotalDiscountAmount(discounts, totalGrossAmount);
		Map<Integer, Long> discountAmountByItemIdx = null;
		if (totalDiscountAmount != null) {
			discountAmountByItemIdx = distributeDiscount(items, totalDiscountAmount, totalGrossAmount);
			this.totalEffectiveDiscountPercentage = 100d * totalDiscountAmount / totalGrossAmount;
		} else {
			this.totalEffectiveDiscountPercentage = null;
		}
		this.itemLines = CartUtils.buildItemLines(items, discountAmountByItemIdx);
		this.totalEffectiveVat = CartUtils.summarizeEffectiveVat(itemLines);

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

}
