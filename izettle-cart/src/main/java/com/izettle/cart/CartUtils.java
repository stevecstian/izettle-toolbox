package com.izettle.cart;

import static com.izettle.java.ValueChecks.allNull;
import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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

	static <T extends Item> long getGrossValue(List<T> items) {
		long grossPrice = 0;
		if (!empty(items)) {
			for (T item : items) {
				grossPrice += ItemUtils.getGrossValue(item);
			}
		}
		return grossPrice;
	}

	private static BigDecimal getNonRoundedDiscountValue(Discount discount, Long totalGrossAmount) {
		return getNonRoundedDiscountValue(discount, new BigDecimal(totalGrossAmount));
	}

	static BigDecimal getNonRoundedDiscountValue(Discount discount, BigDecimal totalGrossAmount) {
		BigDecimal retVal = null;
		if (discount.getAmount() != null) {
			retVal = new BigDecimal(discount.getAmount());
		}
		if (discount.getPercentage() != null) {
			retVal = coalesce(retVal, BigDecimal.ZERO).add(
				totalGrossAmount.
				abs().
				multiply(new BigDecimal(discount.getPercentage()).
					divide(new BigDecimal(100L))));
		}
		if (retVal != null) {
			return discount.getQuantity().multiply(retVal);
		}
		return null;
	}

	static <K extends Discount<K>> Long getTotalDiscountValue(List<K> discounts, long totalGrossAmount) {
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
		return coalesce(summarizedAmounts, 0L) + round(Math.abs(totalGrossAmount) * coalesce(summarizedPercentages, 0d) / 100);
	}

	static Double getDiscountPercentage(long grossValue, Long discountValue) {
		if (discountValue == null) {
			return null;
		} else {
			return 100d * discountValue / grossValue;
		}
	}

	/*
	 * Distributes the discounted amount amongst the different line items so that the total rounding error is minimized
	 */
	static <T extends Item> Map<Integer, Long> distributeDiscountedAmountOverItems(
		final List<T> items,
		final Long discountAmount,
		final long grossAmount
	) {
		if (discountAmount == null || grossAmount == 0L) {
			return null;
		}
		final double discountFraction = ((double) discountAmount) / grossAmount;
		long remainingDiscountAmountToDistribute = discountAmount;
		Map<Integer, Long> discountAmountByItemIdx = new HashMap<Integer, Long>();
		NavigableMap<Double, Queue<Integer>> itemIdxByRoundingLoss = new TreeMap<Double, Queue<Integer>>();
		for (int itemIdx = 0; itemIdx < items.size(); itemIdx++) {
			Item item = items.get(itemIdx);
			final double nonRoundedDiscount = ItemUtils.getGrossValue(item) * discountFraction;
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
			Double nonRoundedDiscount = ItemUtils.getGrossValue(item) * discountFraction;
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

	static <T extends Discount> Map<Integer, Long> distributeDiscountedAmountOverDiscounts(
		final List<T> discounts,
		final Long discountAmount,
		final long totalGrossAmount
	) {
		if (discountAmount == null) {
			return null;
		}
		long remainingDiscountAmountToDistribute = discountAmount;
		Map<Integer, Long> discountAmountByDiscountIdx = new HashMap<Integer, Long>();
		NavigableMap<BigDecimal, Queue<Integer>> discountIdxByRoundingLoss = new TreeMap<BigDecimal, Queue<Integer>>();
		for (int discountIdx = 0; discountIdx < discounts.size(); discountIdx++) {
			Discount discount = discounts.get(discountIdx);
			final BigDecimal nonRoundedDiscount = getNonRoundedDiscountValue(discount, totalGrossAmount);
			final long roundedDiscount = round(nonRoundedDiscount);
			final BigDecimal roundingLoss = nonRoundedDiscount.subtract(new BigDecimal(roundedDiscount));
			Queue<Integer> discountIdxs = discountIdxByRoundingLoss.get(roundingLoss);
			if (discountIdxs == null) {
				discountIdxs = new LinkedList<Integer>();
				discountIdxByRoundingLoss.put(roundingLoss, discountIdxs);
			}
			discountIdxs.add(discountIdx);
			discountAmountByDiscountIdx.put(discountIdx, roundedDiscount);
			remainingDiscountAmountToDistribute -= roundedDiscount;
		}
		while (remainingDiscountAmountToDistribute != 0) {
			boolean reclaiming = remainingDiscountAmountToDistribute < 0;
			//We've distributed too much. reclaiming one at a time from the discounts with lowest roundingLoss
			BigDecimal oldRoundingLoss = reclaiming ? discountIdxByRoundingLoss.firstKey() : discountIdxByRoundingLoss.lastKey();
			Queue<Integer> discountIdxs = discountIdxByRoundingLoss.remove(oldRoundingLoss);
			Integer discountIdxToChange = discountIdxs.poll();
			//Reinsert remains to queue until next time:
			if (!discountIdxs.isEmpty()) {
				discountIdxByRoundingLoss.put(oldRoundingLoss, discountIdxs);
			}
			Long roundedDiscount = discountAmountByDiscountIdx.get(discountIdxToChange);
			Discount discount = discounts.get(discountIdxToChange);
			final BigDecimal nonRoundedDiscount = getNonRoundedDiscountValue(discount, totalGrossAmount);
			//reclaim one unit of money:
			roundedDiscount += reclaiming ? -1L : 1L;
			remainingDiscountAmountToDistribute += reclaiming ? 1L : -1L;
			//reinsert into collections for next round
			final BigDecimal newRoundingLoss = nonRoundedDiscount.subtract(new BigDecimal(roundedDiscount));
			Queue<Integer> newDiscountIdxs = discountIdxByRoundingLoss.get(newRoundingLoss);
			if (newDiscountIdxs == null) {
				newDiscountIdxs = new LinkedList<Integer>();
				discountIdxByRoundingLoss.put(newRoundingLoss, newDiscountIdxs);
			}
			newDiscountIdxs.add(discountIdxToChange);
			discountAmountByDiscountIdx.put(discountIdxToChange, roundedDiscount);

		}
		return discountAmountByDiscountIdx;
	}

	static <K extends Discount<K>> List<DiscountLine<K>> buildDiscountLines(
		final List<K> discounts,
		final long grossValue,
		final Long totalDiscountValue
	) {
		final Map<Integer, Long> discountAmountByDiscountIdx = distributeDiscountedAmountOverDiscounts(
			discounts,
			totalDiscountValue,
			grossValue
		);
		final List<DiscountLine<K>> retList = new ArrayList<DiscountLine<K>>();
		if (!empty(discounts)) {
			for (int i = 0; i < discounts.size(); i++) {
				K discount = discounts.get(i);
				Long discountAmount = discountAmountByDiscountIdx.get(i);
				Double linePercentage = 100d * discountAmount / grossValue;
				DiscountLine<K> discountLine = new DiscountLine<K>(discount, linePercentage, discountAmount);
				retList.add(discountLine);
			}
		}
		return retList;
	}

	static <T extends Item<T, K>, K extends Discount<K>> Long summarizeGrossVat(final List<ItemLine<T, K>> itemLines) {
		Long totalGrossVat = null;
		for (ItemLine<T, K> itemLine : itemLines) {
			Long itemGrossVat = itemLine.getGrossVat();
			if (itemGrossVat != null) {
				totalGrossVat = coalesce(totalGrossVat, 0L) + itemGrossVat;
			}
		}
		return totalGrossVat;
	}

	static <T extends Item<T, K>, K extends Discount<K>> Long summarizeEffectiveVat(final List<ItemLine<T, K>> itemLines) {
		Long effectiveVat = null;
		for (ItemLine<T, K> itemLine : itemLines) {
			Long itemEffectiveVat = itemLine.getActualVat();
			if (itemEffectiveVat != null) {
				effectiveVat = coalesce(effectiveVat, 0L) + itemEffectiveVat;
			}
		}
		return effectiveVat;
	}

	static <T extends Item<T, K>, K extends Discount<K>> List<ItemLine<T, K>> buildItemLines(
		final List<T> items,
		final Long grossValue,
		final Long totalDiscountValue
	) {
		final Map<Integer, Long> discountAmountByItemIdx = distributeDiscountedAmountOverItems(
			items,
			totalDiscountValue,
			grossValue
		);
		final List<ItemLine<T, K>> retList = new ArrayList<ItemLine<T, K>>();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			long linePrice = ItemUtils.getGrossValue(item);
			final long effectivePrice;
			if (discountAmountByItemIdx != null) {
				Long discountAmount = discountAmountByItemIdx.get(i);
				effectivePrice = linePrice - coalesce(discountAmount, 0L);
			} else {
				effectivePrice = linePrice;
			}
			Long grossVat = calculateVatFromGrossAmount(linePrice, item.getVatPercentage());
			Long effectiveVat = calculateVatFromGrossAmount(effectivePrice, item.getVatPercentage());
			ItemLine<T, K> itemLine = new ItemLine<T, K>(item, linePrice, grossVat, effectivePrice, effectiveVat);
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

	static <T extends Item<T, K>, K extends Discount<K>> SortedMap<Float, VatGroupValues> groupValuesByVatPercentage(Collection<ItemLine<T, K>> itemLines) {
		SortedMap<Float, Long> actualVatValuePerGroup = new TreeMap<Float, Long>();
		SortedMap<Float, Long> actualValuePerVatGroup = new TreeMap<Float, Long>();
		SortedMap<Float, VatGroupValues> vatGroupValues = new TreeMap<Float, VatGroupValues>();
		for (ItemLine<T, K> itemLine : itemLines) {
			Long actualVat = itemLine.getActualVat();
			Long actualValue = itemLine.getActualValue();
			Float vatPercentage = itemLine.getItem().getVatPercentage();
			if (actualVat != null) {
				Long accVatAmountForGroup = actualVatValuePerGroup.get(vatPercentage);
				Long accValueForGroup = actualValuePerVatGroup.get(vatPercentage);
				actualVatValuePerGroup.put(vatPercentage, coalesce(accVatAmountForGroup, 0L) + actualVat);
				actualValuePerVatGroup.put(vatPercentage, coalesce(accValueForGroup, 0L) + actualValue);
			}
		}
		for (Float vatPerc : actualVatValuePerGroup.keySet()) {
			vatGroupValues.put(vatPerc, new VatGroupValues(vatPerc, actualVatValuePerGroup.get(vatPerc), actualValuePerVatGroup.get(vatPerc)));
		}
		return vatGroupValues;
	}

}
