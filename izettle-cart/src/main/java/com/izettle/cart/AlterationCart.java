package com.izettle.cart;

import static com.izettle.cart.CartUtils.coalesce;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A cart-like object representing the altered items. An alteration in itself doesn't behave exactly like a normal cart,
 * which is why this class is needed: to avoid rounding errors on repeated refunds issued on the same original cart,
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
     * The residual cart after the current alteration has been applied
    */
    private final Cart<T, D, K, S> resultingCart;

    AlterationCart(final Cart<T, D, K, S> originalCart, final Cart<T, D, K, S> resultingCart) {
        this.originalCart = originalCart;
        this.resultingCart = resultingCart;
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
}
