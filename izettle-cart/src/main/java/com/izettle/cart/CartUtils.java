package com.izettle.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

class CartUtils {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private CartUtils() {
    }

    /**
     * Rounds the value to a long using {@link java.math.RoundingMode#HALF_UP}
     *
     * @param decimal the value to round
     * @return rounded value
     */
    static long round(final BigDecimal decimal) {
        return decimal.setScale(0, ROUNDING_MODE).longValue();
    }

    static long round(final Double decimal) {
        return round(BigDecimal.valueOf(decimal));
    }

    static <T extends Item> long getGrossValue(final List<T> items) {
        long grossPrice = 0;
        for (T item : items) {
            grossPrice += ItemUtils.getValue(item);
        }
        return grossPrice;
    }

    static BigDecimal getNonRoundedDiscountValue(final Discount discount, final BigDecimal totalGrossAmount) {
        BigDecimal retVal = null;
        if (discount.getAmount() != null) {
            retVal = BigDecimal.valueOf(discount.getAmount());
        }
        if (discount.getPercentage() != null) {
            retVal = coalesce(retVal, BigDecimal.ZERO)
                .add(
                    totalGrossAmount
                    .abs()
                    .multiply(BigDecimal.valueOf(discount.getPercentage()))
                    .divide(BigDecimal.valueOf(100L))
                );
        }
        if (retVal != null) {
            return discount.getQuantity().multiply(retVal);
        }
        return null;
    }

    /**
     * Calculates the total discount amount. Both the cart-wide discount on the cart gross value and the item line
     * discounts.
     * @param discounts the cart-wide discounts to apply, in order
     * @param totalGrossAmount cart gross amount
     * @param items cart items
     * @return the total rounded discounted value
     */
    static Long getTotalDiscountValue(
        final List<? extends Discount> discounts,
        final long totalGrossAmount,
        final List<? extends Item> items
    ) {

        final Long cartWideDiscount = getTotalCartWideDiscountValue(discounts, totalGrossAmount);

        /*
         Sum up line item discounts.
         */
        Long lineItemDiscount = null;

        for (Item item : items) {
            Long discountValue = ItemUtils.getDiscountValue(item);
            if (discountValue != null) {
                lineItemDiscount = coalesce(lineItemDiscount, 0L) + discountValue;
            }
        }

        if (lineItemDiscount != null || cartWideDiscount != null) {
            return coalesce(cartWideDiscount, 0L) + coalesce(lineItemDiscount, 0L);
        }

        return null;
    }

    /**
     * Total value of the cart-wide discounts, if any.
     *
     * @param discounts Cart-wide discounts.
     * @param totalGrossAmount Cart gross value (after line item discounts).
     * @return Total value of cart-wide discounts if any, null otherwise.
     */
    static Long getTotalCartWideDiscountValue(final List<? extends Discount> discounts, final long totalGrossAmount) {
        boolean appliedDiscount = false;
        BigDecimal tmpTotalAmount = BigDecimal.valueOf(totalGrossAmount);

        for (Discount discount : discounts) {
            final BigDecimal discountValue = getNonRoundedDiscountValue(discount, tmpTotalAmount);
            if (discountValue != null) {
                tmpTotalAmount = tmpTotalAmount.subtract(discountValue);
                appliedDiscount = true;
            }
        }

        if (appliedDiscount) {
            return round(BigDecimal.valueOf(totalGrossAmount).subtract(tmpTotalAmount));
        }
        return null;
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
        final Long cartWideDiscountAmount,
        final long grossAmount
    ) {
        if (cartWideDiscountAmount == null || grossAmount == 0L) {
            return null;
        }
        final double discountFraction = ((double) cartWideDiscountAmount) / grossAmount;
        long remainingDiscountAmountToDistribute = cartWideDiscountAmount;
        final Map<Integer, Long> discountAmountByItemIdx = new HashMap<Integer, Long>();
        final NavigableMap<Double, Queue<Integer>> itemIdxByRoundingLoss = new TreeMap<Double, Queue<Integer>>();
        for (int itemIdx = 0; itemIdx < items.size(); itemIdx++) {
            final Item item = items.get(itemIdx);
            final double nonRoundedDiscount = ItemUtils.getValue(item) * discountFraction;
            final long roundedDiscount = round(nonRoundedDiscount);
            final double roundingLoss = nonRoundedDiscount - roundedDiscount;
            if (!itemIdxByRoundingLoss.containsKey(roundingLoss)) {
                itemIdxByRoundingLoss.put(roundingLoss, new LinkedList<Integer>());
            }
            itemIdxByRoundingLoss.get(roundingLoss).add(itemIdx);
            discountAmountByItemIdx.put(itemIdx, roundedDiscount);
            remainingDiscountAmountToDistribute -= roundedDiscount;
        }
        while (remainingDiscountAmountToDistribute != 0) {
            final boolean reclaiming = remainingDiscountAmountToDistribute < 0;
            //We've distributed too much. reclaiming one at a time from the items with lowest roundingLoss
            final Double oldRoundingLoss = reclaiming ? itemIdxByRoundingLoss.firstKey() : itemIdxByRoundingLoss.lastKey();
            final Queue<Integer> itemIdxs = itemIdxByRoundingLoss.remove(oldRoundingLoss);
            final Integer itemIdxToChange = itemIdxs.poll();
            //Reinsert remains to queue until next time:
            if (!itemIdxs.isEmpty()) {
                itemIdxByRoundingLoss.put(oldRoundingLoss, itemIdxs);
            }
            final Item item = items.get(itemIdxToChange);
            final Double nonRoundedDiscount = ItemUtils.getValue(item) * discountFraction;
            //reclaim one unit of money:
            final Long roundedDiscount = discountAmountByItemIdx.get(itemIdxToChange) + (reclaiming ? -1L : 1L);
            remainingDiscountAmountToDistribute += reclaiming ? 1L : -1L;
            //reinsert into collections for next round
            final double newRoundingLoss = nonRoundedDiscount - roundedDiscount;
            if (!itemIdxByRoundingLoss.containsKey(newRoundingLoss)) {
                itemIdxByRoundingLoss.put(newRoundingLoss, new LinkedList<Integer>());
            }
            itemIdxByRoundingLoss.get(newRoundingLoss).add(itemIdxToChange);
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
        final Map<Integer, Long> roundedDiscountAmountByDiscountIdx = new HashMap<Integer, Long>();
        final Map<Integer, BigDecimal> nonRoundedDiscountAmountByDiscountIdx = new HashMap<Integer, BigDecimal>();
        final NavigableMap<BigDecimal, Queue<Integer>> discountIdxByRoundingLoss = new TreeMap<BigDecimal, Queue<Integer>>();
        BigDecimal tmpTotAmount = BigDecimal.valueOf(totalGrossAmount);
        for (int discountIdx = 0; discountIdx < discounts.size(); discountIdx++) {
            final Discount discount = discounts.get(discountIdx);
            final BigDecimal nonRoundedDiscount = getNonRoundedDiscountValue(discount, tmpTotAmount);
            final long roundedDiscount = round(nonRoundedDiscount);
            final BigDecimal roundingLoss = nonRoundedDiscount.subtract(BigDecimal.valueOf(roundedDiscount));
            if (!discountIdxByRoundingLoss.containsKey(roundingLoss)) {
                discountIdxByRoundingLoss.put(roundingLoss, new LinkedList<Integer>());
            }
            discountIdxByRoundingLoss.get(roundingLoss).add(discountIdx);
            roundedDiscountAmountByDiscountIdx.put(discountIdx, roundedDiscount);
            nonRoundedDiscountAmountByDiscountIdx.put(discountIdx, nonRoundedDiscount);
            remainingDiscountAmountToDistribute -= roundedDiscount;
            tmpTotAmount = tmpTotAmount.subtract(nonRoundedDiscount);
        }
        while (remainingDiscountAmountToDistribute != 0) {
            final boolean reclaiming = remainingDiscountAmountToDistribute < 0;
            //We've distributed too much. reclaiming one at a time from the discounts with lowest roundingLoss
            final BigDecimal oldRoundingLoss
                = reclaiming ? discountIdxByRoundingLoss.firstKey() : discountIdxByRoundingLoss.lastKey();
            final Queue<Integer> discountIdxs = discountIdxByRoundingLoss.remove(oldRoundingLoss);
            final Integer discountIdxToChange = discountIdxs.poll();
            //Reinsert remains to queue until next time:
            if (!discountIdxs.isEmpty()) {
                discountIdxByRoundingLoss.put(oldRoundingLoss, discountIdxs);
            }
            //reclaim one unit of money:
            final Long roundedDiscount = roundedDiscountAmountByDiscountIdx.get(discountIdxToChange) + (reclaiming ? -1L : 1L);
            remainingDiscountAmountToDistribute += reclaiming ? 1L : -1L;
            final BigDecimal nonRoundedDiscount = nonRoundedDiscountAmountByDiscountIdx.get(discountIdxToChange);
            //reinsert into collections for next round
            final BigDecimal newRoundingLoss = nonRoundedDiscount.subtract(BigDecimal.valueOf(roundedDiscount));
            if (!discountIdxByRoundingLoss.containsKey(newRoundingLoss)) {
                discountIdxByRoundingLoss.put(newRoundingLoss, new LinkedList<Integer>());
            }
            discountIdxByRoundingLoss.get(newRoundingLoss).add(discountIdxToChange);
            roundedDiscountAmountByDiscountIdx.put(discountIdxToChange, roundedDiscount);

        }
        return roundedDiscountAmountByDiscountIdx;
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
            final Long itemGrossVat = itemLine.getGrossVat();
            if (itemGrossVat != null) {
                totalGrossVat = coalesce(totalGrossVat, 0L) + itemGrossVat;
            }
        }
        return totalGrossVat;
    }

    static <T extends Item<T, K>, K extends Discount<K>> Long summarizeEffectiveVat(
        final List<ItemLine<T, K>> itemLines,
        final ServiceChargeLine serviceChargeLine
    ) {
        Long effectiveVat = null;
        for (ItemLine<T, K> itemLine : itemLines) {
            final Long itemEffectiveVat = itemLine.getActualVat();
            if (itemEffectiveVat != null) {
                effectiveVat = coalesce(effectiveVat, 0L) + itemEffectiveVat;
            }
        }

        if (serviceChargeLine != null && serviceChargeLine.getVat() != null) {
            effectiveVat = coalesce(effectiveVat, 0L) + serviceChargeLine.getVat();
        }

        return effectiveVat;
    }

    static <T extends Item<T, K>, K extends Discount<K>> List<ItemLine<T, K>> buildItemLines(
        final List<T> items,
        final Long grossValue,
        final Long cartWideDiscountAmount
    ) {
        final Map<Integer, Long> discountAmountByItemIdx = distributeDiscountedAmountOverItems(
            items,
            cartWideDiscountAmount,
            grossValue
        );
        final List<ItemLine<T, K>> retList = new ArrayList<ItemLine<T, K>>();
        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);
            final long effectivePrice;
            if (discountAmountByItemIdx != null) {
                final Long discountAmount = discountAmountByItemIdx.get(i);
                effectivePrice = ItemUtils.getValue(item) - coalesce(discountAmount, 0L);
            } else {
                effectivePrice = ItemUtils.getValue(item);
            }
            final Long grossVat = calculateVatFromGrossAmount(ItemUtils.getGrossValue(item), item.getVatPercentage());
            final Long effectiveVat = calculateVatFromGrossAmount(effectivePrice, item.getVatPercentage());

            final ItemLine<T, K> itemLine = new ItemLine<T, K>(
                item,
                ItemUtils.getGrossValue(item),
                grossVat,
                effectivePrice,
                effectiveVat,
                ItemUtils.getDiscountValue(item)
            );

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

    static <T extends Item<T, K>, K extends Discount<K>> SortedMap<Float, VatGroupValues> groupValuesByVatPercentage(
        final Collection<ItemLine<T, K>> itemLines,
        final ServiceChargeLine serviceChargeLine
    ) {
        final SortedMap<Float, Long> actualVatValuePerGroup = new TreeMap<Float, Long>();
        final SortedMap<Float, Long> actualValuePerVatGroup = new TreeMap<Float, Long>();
        final SortedMap<Float, VatGroupValues> vatGroupValues = new TreeMap<Float, VatGroupValues>();
        for (ItemLine<T, K> itemLine : itemLines) {
            final Long actualVat = itemLine.getActualVat();
            final Long actualValue = itemLine.getActualValue();
            final Float vatPercentage = itemLine.getItem().getVatPercentage();
            if (actualVat != null) {
                final Long accVatAmountForGroup = actualVatValuePerGroup.get(vatPercentage);
                final Long accValueForGroup = actualValuePerVatGroup.get(vatPercentage);
                actualVatValuePerGroup.put(vatPercentage, coalesce(accVatAmountForGroup, 0L) + actualVat);
                actualValuePerVatGroup.put(vatPercentage, coalesce(accValueForGroup, 0L) + actualValue);
            }
        }

        if (serviceChargeLine != null && serviceChargeLine.getVat() != null) {
            final Float serviceChargeVatPercentage = serviceChargeLine.getServiceCharge().getVatPercentage();
            final Long accVatAmountForGroup = actualVatValuePerGroup.get(serviceChargeVatPercentage);
            final Long accValueForGroup = actualValuePerVatGroup.get(serviceChargeVatPercentage);
            actualVatValuePerGroup.put(
                serviceChargeVatPercentage,
                coalesce(accVatAmountForGroup, 0L) + serviceChargeLine.getVat()
            );
            actualValuePerVatGroup.put(
                serviceChargeVatPercentage,
                coalesce(accValueForGroup, 0L) + serviceChargeLine.getValue()
            );
        }

        for (Float vatPerc : actualVatValuePerGroup.keySet()) {
            vatGroupValues.put(
                vatPerc,
                new VatGroupValues(vatPerc, actualVatValuePerGroup.get(vatPerc), actualValuePerVatGroup.get(vatPerc))
            );
        }

        return vatGroupValues;
    }

    static <S extends ServiceCharge<S>> ServiceChargeLine<S> buildServiceChargeLine(
        final long grossValue,
        final Long cartWideDiscountValue,
        final S serviceCharge
    ) {
        if (null == serviceCharge) {
            return null;
        }

        final Long serviceChargeValue = getServiceChargeValue(grossValue, cartWideDiscountValue, serviceCharge);
        final Long serviceChargeVat;
        if (serviceCharge.getVatPercentage() == null) {
            serviceChargeVat = null;
        } else {
            serviceChargeVat
                = coalesce(serviceChargeValue, 0L)
                - round((serviceChargeValue * 100) / (100 + (double) serviceCharge.getVatPercentage()));
        }

        return new ServiceChargeLine<S>(serviceCharge, serviceChargeValue, serviceChargeVat);
    }

    static Long getServiceChargeValue(
        final long grossValue,
        final Long cartWideDiscountValue,
        final ServiceCharge serviceCharge
    ) {
        if (null == serviceCharge) {
            return null;
        }

        final long actualValue = grossValue - coalesce(cartWideDiscountValue, 0L);

        final Long serviceChargeFromPercent;
        if (serviceCharge.getPercentage() == null) {
            serviceChargeFromPercent = null;
        } else {
            serviceChargeFromPercent = round(actualValue * serviceCharge.getPercentage() / 100);
        }

        return (serviceChargeFromPercent == null && serviceCharge.getAmount() == null)
            ? null
            : (coalesce(serviceCharge.getAmount(), 0L) + coalesce(serviceChargeFromPercent, 0L));
    }

    public static <T, S extends T> T coalesce(T subject, S fallback) {
        return subject != null ? subject : fallback;
    }

    public static boolean empty(Collection o) {
        return o == null || o.isEmpty();
    }

}
