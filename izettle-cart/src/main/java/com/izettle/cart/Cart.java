package com.izettle.cart;

import static com.izettle.java.ValueChecks.allNull;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;
import static java.util.Collections.unmodifiableList;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Cart<T extends Item, K extends Discount> {

	private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
	private final List<ItemLine<T>> itemLines;
	private final List<DiscountLine<K>> discountLines;
	private final long totalEffectivePrice;
	private final long totalGrossPrice;
	private final Long totalEffectiveVat;
	private final Double totalEffectiveDiscountPercentage;

	public Cart(Map<T, BigDecimal> items, Map<K, BigDecimal> discounts) {
		if (empty(items)) {
			throw new IllegalArgumentException("Cannot create a cart with no items");
		}
		this.totalGrossPrice = getTotalGrossPrice(items);
		this.discountLines = buildDiscountLines(discounts, totalGrossPrice);
		this.totalEffectiveDiscountPercentage = summarizeEffectiveDiscountPercentages(discountLines);
		this.itemLines = buildItemLines(items, totalEffectiveDiscountPercentage);
		this.totalEffectivePrice = summarizeEffectivePrice(itemLines);
		this.totalEffectiveVat = summarizeEffectiveVat(itemLines);
	}

	public List<ItemLine<T>> getItemLines() {
		return unmodifiableList(itemLines);
	}

	/**
	 * The total amount to be paid. Bottom line after possible discounts
	 * @return The fractionized amount
	 */
	public long getTotalEffectivePrice() {
		return totalEffectivePrice;
	}

	public long getTotalGrossPrice() {
		return totalGrossPrice;
	}

	public Long getTotalEffectiveVat() {
		return totalEffectiveVat;
	}

	public List<DiscountLine<K>> getDiscountLines() {
		return unmodifiableList(discountLines);
	}

	public Double getTotalEffectiveDiscountPercentage() {
		return totalEffectiveDiscountPercentage;
	}

	@Override
	public String toString() {
		return ""
				+ "Cart{"
				+ "  itemLines=" + itemLines
				+ ", discountLines=" + discountLines
				+ ", totalEffectivePrice=" + totalEffectivePrice
				+ ", totalGrossPrice=" + totalGrossPrice
				+ ", totalEffectiveVat=" + totalEffectiveVat
				+ ", effectiveDiscountPerc=" + totalEffectiveDiscountPercentage
				+ '}';
	}

	static <T extends Item> long getTotalGrossPrice(Map<T, BigDecimal> items) {
		long grossPrice = 0;
		if (!empty(items)) {
			for (Entry<T, BigDecimal> entry : items.entrySet()) {
				T item = entry.getKey();
				BigDecimal quantity = entry.getValue();
				grossPrice += getLinePrice(item, quantity);
			}
		}
		return grossPrice;
	}

	static long getLinePrice(Item item, BigDecimal quantity) {
		return round(quantity.multiply(new BigDecimal(item.getPricePerQuantity())));
	}

	static Double getLinePercentage(Discount discount, BigDecimal quantity, long totalGrossPrice) {
		Double discountPercentage = null;
		Long discountAmount = null;

		if (discount.getAmount() != null) {
			discountAmount = coalesce(discountAmount, 0L) + round(quantity.multiply(new BigDecimal(discount.getAmount())));
		}
		if (discount.getPercentage() != null) {
			discountPercentage = coalesce(discountPercentage, 0D) + round(quantity.multiply(new BigDecimal(discount.getPercentage())));
		}

		if (allNull(discountPercentage, discountAmount)) {
			return null;
		}
		return coalesce(discountPercentage, 0d) + 100d * (((double) coalesce(discountAmount, 0L)) / totalGrossPrice);
	}

	static long round(BigDecimal decimal) {
		return decimal.round(new MathContext(0, ROUNDING_MODE)).longValue();
	}

	static long round(Double decimal) {
		return round(new BigDecimal(decimal));
	}

	static <T extends Item> List<ItemLine<T>> buildItemLines(Map<T, BigDecimal> items, Double effectiveDiscountPerc) {
		List<ItemLine<T>> retList = new ArrayList<ItemLine<T>>();
		for (Entry<T, BigDecimal> entry : items.entrySet()) {
			T item = entry.getKey();
			BigDecimal quantity = entry.getValue();
			long linePrice = getLinePrice(item, quantity);
			final long effectivePrice;
			if (effectiveDiscountPerc != null) {
				effectivePrice = round(linePrice * ((100d - effectiveDiscountPerc) / 100d));
			} else {
				effectivePrice = linePrice;
			}
			Long effectiveVat = calculateVatFromGrossAmount(effectivePrice, item.getVatPercentage());
			ItemLine<T> itemLine = new ItemLine<T>(item, quantity, linePrice, effectivePrice, effectiveVat);
			retList.add(itemLine);
		}
		return retList;
	}

	static <K extends Discount> List<DiscountLine<K>> buildDiscountLines(Map<K, BigDecimal> discounts, long totalGrossPrice) {
		List<DiscountLine<K>> retList = new ArrayList<DiscountLine<K>>();
		if (!empty(discounts)) {
			for (Entry<K, BigDecimal> entry : discounts.entrySet()) {
				K discount = entry.getKey();
				BigDecimal quantity = entry.getValue();
				Double linePercentage = getLinePercentage(discount, quantity, totalGrossPrice);
				DiscountLine<K> discountLine = new DiscountLine<K>(discount, quantity, linePercentage);
				retList.add(discountLine);
			}
		}
		return retList;
	}

	static Long calculateVatFromGrossAmount(Long amountIncVat, Double vatPercent) {
		if (amountIncVat == null || vatPercent == null) {
			return null;
		}
		return amountIncVat - round((amountIncVat * 100) / (100 + vatPercent));
	}

	static <T extends Item> Long summarizeEffectiveVat(List<ItemLine<T>> itemLines) {
		Long effectiveVat = null;
		for (ItemLine<?> itemLine : itemLines) {
			Long itemEffectiveVat = itemLine.getEffectiveVat();
			if (itemEffectiveVat != null) {
				effectiveVat = coalesce(effectiveVat, 0L) + itemEffectiveVat;
			}
		}
		return effectiveVat;
	}

	static <T extends Item> long summarizeEffectivePrice(List<ItemLine<T>> itemLines) {
		long retVal = 0L;
		if (!empty(itemLines)) {
			for (ItemLine<T> itemLine : itemLines) {
				retVal += itemLine.getEffectivePrice();
			}
		}
		return retVal;
	}

	static <K extends Discount> Double summarizeEffectiveDiscountPercentages(List<DiscountLine<K>> discountLines) {
		Double retVal = null;
		if (!empty(discountLines)) {
			for (DiscountLine<K> discountLine : discountLines) {
				if (discountLine.getEffectivePercentage() != null) {
					retVal = coalesce(retVal, 0D) + discountLine.getEffectivePercentage();
				}

			}
		}
		return retVal;
	}
}
