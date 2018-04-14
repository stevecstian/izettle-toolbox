package com.izettle.cart;

import static com.izettle.cart.CartUtils.coalesce;

import com.izettle.cart.exception.CartException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A cart-like object representing the altered items. An alteration in itself doesn't behave exactly like a normal cart,
 * which is why this class is needed: to avoid rounding errors on repeated returns issued on the same original cart,
 * this class needs to compare the previous version of the cart with the current resulting one and answer all queries as
 * the difference between the two. This class doesn't have a public constructor: it should only be created by applying
 * an alteration operation on an existing cart object.
 * @param <T> the type of the items
 * @param <D> the type of the lite item discounts
 * @param <K> the type of the cart-wide discounts
 * @param <S> the type of the service charge
*/
public class AlterationCart<T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>> {

    /**
     * The last version of the cart, after previous alterations has been applied
     */
    private final Cart<T, D, K, S> originalCart;
    /**
     * The applied altered quantities
     */
    private final Map<Object, BigDecimal> alteration;
    /**
     * The residual cart after the current alteration has been applied
    */
    private final Cart<T, D, K, S> resultingCart;

    AlterationCart(
        final Cart<T, D, K, S> originalCart,
        final Cart<T, D, K, S> resultingCart,
        final Map<Object, BigDecimal> alteration
    ) {
        this.originalCart = originalCart;
        this.resultingCart = resultingCart;
        this.alteration = alteration;
    }

    public Double getActualDiscountPercentage() {
        return originalCart.getActualDiscountPercentage();
    }

    public Long getActualVat() {
        if (originalCart.getActualVat() == null && resultingCart.getActualVat() == null) {
            return null;
        }
        return coalesce(resultingCart.getActualVat(), 0L) - coalesce(originalCart.getActualVat(), 0L);
    }

    public Long getDiscountValue() {
        if (originalCart.getDiscountValue() == null && resultingCart.getDiscountValue() == null) {
            return null;
        }
        return coalesce(resultingCart.getDiscountValue(), 0L) - coalesce(originalCart.getDiscountValue(), 0L);
    }

    public Long getDiscountVat() {
        if (originalCart.getDiscountVat() == null && resultingCart.getDiscountVat() == null) {
            return null;
        }
        return coalesce(resultingCart.getDiscountVat(), 0L) - coalesce(originalCart.getDiscountVat(), 0L);
    }

    public long getGrossValue() {
        return resultingCart.getGrossValue() - originalCart.getGrossValue();
    }

    public Long getGrossVat() {
        if (originalCart.getGrossVat() == null && resultingCart.getGrossVat() == null) {
            return null;
        }
        return coalesce(resultingCart.getGrossVat(), 0L) - coalesce(originalCart.getGrossVat(), 0L);
    }

    public int getNumberOfDiscounts() {
        return originalCart.getNumberOfDiscounts();
    }

    public Long getServiceChargeValue() {
        if (originalCart.getServiceChargeValue() == null && resultingCart.getServiceChargeValue() == null) {
            return null;
        }
        return coalesce(resultingCart.getServiceChargeValue(), 0L) - coalesce(originalCart.getServiceChargeValue(), 0L);
    }

    public long getValue() {
        return resultingCart.getValue() - originalCart.getValue();
    }

    /**
     * Returns the total value of all cart-wide discounts for this alteration (VAT still included)
     * @return the discount value, or null if there are no discounts
     */
    public Long getCartWideDiscountValue() {
        if (originalCart.getCartWideDiscountValue() == null && resultingCart.getCartWideDiscountValue() == null) {
            return null;
        }
        return coalesce(resultingCart.getCartWideDiscountValue(), 0L) - coalesce(originalCart.getCartWideDiscountValue(), 0L);
    }

    public SortedMap<Float, VatGroupValues> groupValuesByVatPercentage() {
        final SortedMap<Float, VatGroupValues> orignalVats = originalCart.groupValuesByVatPercentage();
        final SortedMap<Float, VatGroupValues> resultingVats = resultingCart.groupValuesByVatPercentage();
        final SortedMap<Float, VatGroupValues> vatGroupValues = new TreeMap<Float, VatGroupValues>();
        for (Map.Entry<Float, VatGroupValues> entrySet : orignalVats.entrySet()) {
            final Float vatPerc = entrySet.getKey();
            final VatGroupValues originalValues = entrySet.getValue();
            final VatGroupValues resultingValues = resultingVats.get(vatPerc);
            if (resultingValues == null) {
                vatGroupValues.put(vatPerc, originalValues);
            } else {
                final VatGroupValues newValues = new VatGroupValues(
                    vatPerc,
                    resultingValues.getActualVatValue() - originalValues.getActualVatValue(),
                    resultingValues.getActualValue() - originalValues.getActualValue()
                );
                vatGroupValues.put(vatPerc, newValues);
            }
        }
        return vatGroupValues;
    }

    /**
     * Method used for synthesizing "fake" item lines. As an alteration is not an actual cart, it's not possible to call
     * the cart methods and iterate over the line items. In reality, an alteration does not even consist of item lines,
     * just a number of reduced quantities.
     * Sometimes, however, there's a need for getting the list of altered items, and their corresponding "price". This
     * method provides that functionality by synthesizing fake item lines.
     * @return The altered items as a list of faked item lines.
     */
    public List<ItemLine> getItemLines() {
        final List<ItemLine> retList = new ArrayList<ItemLine>();
        for (Map.Entry<Object, BigDecimal> entrySet : alteration.entrySet()) {
            final Object identifier = entrySet.getKey();
            final BigDecimal quantity = entrySet.getValue();
            final ItemLine<T, D> originalItemLine = getItemLineFromIdentifier(originalCart, identifier, true);
            final ItemLine<T, D> resultingItemLine = getItemLineFromIdentifier(resultingCart, identifier, false);
            final long actualValue;
            final long grossValue;
            final Long discountValue;
            final Long grossVat;
            final Long actualVat;
            if (resultingItemLine == null) {
                actualValue = -originalItemLine.getActualValue();
                grossValue = -originalItemLine.getGrossValue();
                discountValue = originalItemLine.getDiscountValue() == null ? null : -originalItemLine.getDiscountValue();
                grossVat = originalItemLine.getGrossVat() == null ? null : -originalItemLine.getGrossVat();
                actualVat = originalItemLine.getActualVat() == null ? null : -originalItemLine.getActualVat();
            } else {
                actualValue = resultingItemLine.getActualValue() - originalItemLine.getActualValue();
                grossValue = resultingItemLine.getGrossValue() - originalItemLine.getGrossValue();
                if (resultingItemLine.getDiscountValue() == null && originalItemLine.getDiscountValue() == null) {
                    discountValue = null;
                } else {
                    discountValue = coalesce(resultingItemLine.getDiscountValue(), 0L)
                        - coalesce(originalItemLine.getDiscountValue(), 0L);
                }
                if (resultingItemLine.getGrossVat() == null && originalItemLine.getGrossVat() == null) {
                    grossVat = null;
                } else {
                    grossVat = coalesce(resultingItemLine.getGrossVat(), 0L)
                        - coalesce(originalItemLine.getGrossVat(), 0L);
                }
                if (resultingItemLine.getActualVat() == null && originalItemLine.getActualVat() == null) {
                    actualVat = null;
                } else {
                    actualVat = coalesce(resultingItemLine.getActualVat(), 0L)
                        - coalesce(originalItemLine.getActualVat(), 0L);
                }
            }
            final ReturnItem item = new ReturnItem(
                identifier,
                originalItemLine.getItem().getUnitPrice(),
                originalItemLine.getItem().getVatPercentage(),
                quantity,
                originalItemLine.getItem().getDiscount()
            );
            retList.add(new ItemLine(item, grossValue, grossVat, actualValue, actualVat, discountValue));
        }
        return retList;
    }

    public static class ReturnItem implements Item<ReturnItem, Discount<?>> {

        private final long unitPrice;
        private final Float vatPercentage;
        private final BigDecimal quantity;
        private final Discount discount;
        private final Object id;

        public ReturnItem(
            final Object id,
            final long unitPrice,
            final Float vatPercentage,
            final BigDecimal quantity,
            final Discount discount
        ) {
            this.unitPrice = unitPrice;
            this.vatPercentage = vatPercentage;
            this.quantity = quantity;
            this.discount = discount;
            this.id = id;
        }

        @Override
        public long getUnitPrice() {
            return unitPrice;
        }

        @Override
        public Float getVatPercentage() {
            return vatPercentage;
        }

        @Override
        public BigDecimal getQuantity() {
            return quantity;
        }

        @Override
        public Discount getDiscount() {
            return discount;
        }

        @Override
        public Object getId() {
            return id;
        }

        @Override
        public ReturnItem inverse() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private ItemLine<T, D> getItemLineFromIdentifier(
        final Cart<T, D, K, S> cart,
        final Object identifier,
        final boolean requireMatch
    ) {
        for (ItemLine<T, D> tmpItem : cart.getItemLines()) {
            if (identifier.equals(tmpItem.getItem().getId())) {
                return tmpItem;
            }
        }
        if (requireMatch) {
            throw new CartException("Unexpected error: Couldn't find the original item.");
        }
        return null;
    }
}
