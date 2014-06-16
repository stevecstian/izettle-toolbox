package com.izettle.cart;

import static com.izettle.java.ValueChecks.allNull;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

class CartUtils {

	private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

	private CartUtils() {
	}

	static long round(BigDecimal decimal) {
		return decimal.setScale(0, ROUNDING_MODE).longValue();
	}

	static long round(Double decimal) {
		return round(new BigDecimal(decimal));
	}

	static long getPrice(Item item) {
		return round(item.getQuantity().multiply(new BigDecimal(item.getUnitPrice())));
	}

	static <T extends Item> long getTotalGrossAmount(List<T> items) {
		long grossPrice = 0;
		if (!empty(items)) {
			for (T item : items) {
				grossPrice += getPrice(item);
			}
		}
		return grossPrice;
	}

	static <K extends Discount<K>> Long getTotalDiscountAmount(List<K> discounts, long totalGrossAmount) {
		Double summarizedPercentages = null;
		Long summarizedAmounts = null;
		if (!empty(discounts)) {
			for (K discount : discounts) {
				BigDecimal quantity = discount.getQuantity();
				//The amount on each discount needs to be representable as a valid amount of money, hence rounding here
				if (discount.getAmount() != null) {
					summarizedAmounts
						= coalesce(summarizedAmounts, 0L)
						+ round(quantity.multiply(new BigDecimal(discount.getAmount())));
				}
				if (discount.getPercentage() != null) {
					summarizedPercentages
						= coalesce(summarizedPercentages, 0d)
						+ quantity.multiply(new BigDecimal(discount.getPercentage())).doubleValue();
				}
			}
		}
		if (allNull(summarizedAmounts, summarizedPercentages)) {
			return null;
		}
		return coalesce(summarizedAmounts, 0L) + round(totalGrossAmount * coalesce(summarizedPercentages, 0d) / 100);
	}

	static <T extends Item> Map<Integer, Long> distributeDiscount(
		final List<T> items,
		final long totalDiscountAmount,
		final long grossAmount
	) {
		final double totalDiscountFraction = ((double) totalDiscountAmount) / grossAmount;
		long remainingDiscountAmountToDistribute = totalDiscountAmount;
		Map<Integer, Long> discountAmountByItemIdx = new HashMap<Integer, Long>();
		NavigableMap<Double, Queue<Integer>> itemIdxByRoundingLoss = new TreeMap<Double, Queue<Integer>>();
		for (int itemIdx = 0; itemIdx < items.size(); itemIdx++) {
			Item item = items.get(itemIdx);
			final double nonRoundedDiscount = CartUtils.getPrice(item) * totalDiscountFraction;
			final long roundedDiscount = CartUtils.round(nonRoundedDiscount);
			final double roundingLoss = nonRoundedDiscount - roundedDiscount;
			Queue<Integer> itemIdxs = itemIdxByRoundingLoss.get(roundingLoss);
			if (itemIdxs == null) {
				itemIdxs = new LinkedList<Integer>();
				itemIdxByRoundingLoss.put(roundingLoss, itemIdxs);
			}
			itemIdxs.add(itemIdx);
			discountAmountByItemIdx.put(itemIdx, roundedDiscount);
			remainingDiscountAmountToDistribute -= roundedDiscount;
		}
		while (remainingDiscountAmountToDistribute != 0) {
			boolean reclaiming = remainingDiscountAmountToDistribute < 0;
			//We've distributed too much. reclaiming one at a time from the items with lowest roundingLoss
			Double oldRoundingLoss = reclaiming ? itemIdxByRoundingLoss.firstKey() : itemIdxByRoundingLoss.lastKey();
			Queue<Integer> itemIdxs = itemIdxByRoundingLoss.remove(oldRoundingLoss);
			Integer itemIdxToChange = itemIdxs.poll();
			//Reinsert remains to queue until next time:
			if (!itemIdxs.isEmpty()) {
				itemIdxByRoundingLoss.put(oldRoundingLoss, itemIdxs);
			}
			Long roundedDiscount = discountAmountByItemIdx.get(itemIdxToChange);
			Item item = items.get(itemIdxToChange);
			Double nonRoundedDiscount = CartUtils.getPrice(item) * totalDiscountFraction;
			//reclaim one unit of money:
			roundedDiscount += reclaiming ? -1L : 1L;
			remainingDiscountAmountToDistribute += reclaiming ? 1L : -1L;
			//reinsert into collections for next round
			double newRoundingLoss = nonRoundedDiscount - roundedDiscount;
			Queue<Integer> newItemIdxs = itemIdxByRoundingLoss.get(newRoundingLoss);
			if (newItemIdxs == null) {
				newItemIdxs = new LinkedList<Integer>();
				itemIdxByRoundingLoss.put(newRoundingLoss, newItemIdxs);
			}
			newItemIdxs.add(itemIdxToChange);
			discountAmountByItemIdx.put(itemIdxToChange, roundedDiscount);

		}
		return discountAmountByItemIdx;
	}

	static Double getDiscountLinePercentage(
		final Discount discount,
		final BigDecimal quantity,
		final long totalGrossPrice
	) {
		Double discountPercentage = null;
		Long discountAmount = null;

		if (discount.getAmount() != null) {
			discountAmount
				= coalesce(discountAmount, 0L)
				+ round(quantity.multiply(new BigDecimal(discount.getAmount())));
		}
		if (discount.getPercentage() != null) {
			discountPercentage
				= coalesce(discountPercentage, 0D)
				+ round(quantity.multiply(new BigDecimal(discount.getPercentage())));
		}

		if (allNull(discountPercentage, discountAmount)) {
			return null;
		}
		return coalesce(discountPercentage, 0d) + 100d * (((double) coalesce(discountAmount, 0L)) / totalGrossPrice);
	}

	static <K extends Discount<K>> List<DiscountLine<K>> buildDiscountLines(
		final List<K> discounts,
		final long totalGrossPrice
	) {
		List<DiscountLine<K>> retList = new ArrayList<DiscountLine<K>>();
		if (!empty(discounts)) {
			for (K discount : discounts) {
				BigDecimal quantity = discount.getQuantity();
				Double linePercentage = getDiscountLinePercentage(discount, quantity, totalGrossPrice);
				DiscountLine<K> discountLine = new DiscountLine<K>(discount, linePercentage);
				retList.add(discountLine);
			}
		}
		return retList;
	}

	static <T extends Item<T>> Long summarizeEffectiveVat(final List<ItemLine<T>> itemLines) {
		Long effectiveVat = null;
		for (ItemLine<?> itemLine : itemLines) {
			Long itemEffectiveVat = itemLine.getEffectiveVat();
			if (itemEffectiveVat != null) {
				effectiveVat = coalesce(effectiveVat, 0L) + itemEffectiveVat;
			}
		}
		return effectiveVat;
	}

	static <T extends Item<T>> List<ItemLine<T>> buildItemLines(
		final List<T> items,
		final Map<Integer, Long> discountAmountByItemIdx
	) {
		List<ItemLine<T>> retList = new ArrayList<ItemLine<T>>();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			long linePrice = getPrice(item);
			final long effectivePrice;
			if (discountAmountByItemIdx != null) {
				Long discountAmount = discountAmountByItemIdx.get(i);
				effectivePrice = linePrice - coalesce(discountAmount, 0L);
			} else {
				effectivePrice = linePrice;
			}
			Long effectiveVat = calculateVatFromGrossAmount(effectivePrice, item.getVatPercentage());
			ItemLine<T> itemLine = new ItemLine<T>(item, linePrice, effectivePrice, effectiveVat);
			retList.add(itemLine);
		}
		return retList;
	}

	static Long calculateVatFromGrossAmount(final Long amountIncVat, final Float vatPercent) {
		if (amountIncVat == null || vatPercent == null) {
			return null;
		}
		return amountIncVat - round((amountIncVat * 100) / (100 + (double) vatPercent));
	}

	static <T extends Item<T>, K extends Discount<K>> SortedMap<Float, Long> groupEffectiveVatAmounts(Cart<T, K> cart) {
		SortedMap<Float, Long> vatAmountPerGroup = new TreeMap<Float, Long>();
		for (ItemLine<T> itemLine : cart.getItemLines()) {
			Long effectiveVat = itemLine.getEffectiveVat();
			Float vatPercentage = itemLine.getItem().getVatPercentage();
			if (effectiveVat != null) {
				Long accVatAmountForGroup = vatAmountPerGroup.get(vatPercentage);
				vatAmountPerGroup.put(vatPercentage, coalesce(accVatAmountForGroup, 0L) + effectiveVat);
			}
		}
		return vatAmountPerGroup;
	}

}
