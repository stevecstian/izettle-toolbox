package com.izettle.cart;

import static com.izettle.cart.CartUtils.coalesce;
import static com.izettle.cart.CartUtils.empty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

public class Cart<T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
    implements Serializable {

    private final List<ItemLine<T, D>> itemLines;
    private final List<DiscountLine<K>> discountLines;
    private final ServiceChargeLine<S> serviceChargeLine;
    private final long grossValue;
    private final Long discountValue;
    private final Long serviceChargeValue;
    private final Long cartWideDiscountValue;
    private final Long actualVat;
    private final Double actualDiscountPercentage;
    private final Long grossVat;

    /**
     * Produces a new immutable cart object from two lists if Items and Discounts
     * @param items the list of items, must not be empty (as a cart without items makes no sense)
     * @param discounts the list of cart wide discounts, possibly null or empty
     * @param serviceCharge The applied service charge, possibly null
     */
    public Cart(final List<T> items, final List<K> discounts, final S serviceCharge) {
        final List<T> itemList = coalesce(items, Collections.<T>emptyList());
        final List<K> discountList = coalesce(discounts, Collections.<K>emptyList());
        this.grossValue = CartUtils.getGrossValue(itemList);
        this.discountValue = CartUtils.getTotalDiscountValue(discountList, grossValue, itemList);
        this.actualDiscountPercentage = CartUtils.getDiscountPercentage(grossValue, discountValue);
        this.cartWideDiscountValue = CartUtils.getTotalCartWideDiscountValue(discountList, grossValue);
        this.discountLines = CartUtils.buildDiscountLines(discountList, grossValue, cartWideDiscountValue);
        this.itemLines = CartUtils.buildItemLines(itemList, grossValue, cartWideDiscountValue);
        this.serviceChargeLine = CartUtils.buildServiceChargeLine(grossValue, cartWideDiscountValue, serviceCharge);
        this.serviceChargeValue = CartUtils.getServiceChargeValue(grossValue, cartWideDiscountValue, serviceCharge);
        this.grossVat = CartUtils.summarizeGrossVat(itemLines);
        this.actualVat = CartUtils.summarizeEffectiveVat(itemLines, serviceChargeLine);
    }

    /**
     * Produces a new cart that is inversed, eg an identical cart where all quantities are negated. Useful for example
     * for refunds
     * @return the inversed cart
     */
    public Cart<T, D, K, S> inverse() {
        //Copy all items and discounts, but negate the quantities:
        final List<T> inverseItems;
        if (empty(itemLines)) {
            inverseItems = Collections.emptyList();
        } else {
            inverseItems = new ArrayList<T>(itemLines.size());
            for (ItemLine<T, D> itemLine : itemLines) {
                inverseItems.add(itemLine.getItem().inverse());
            }
        }
        final List<K> inverseDiscounts;
        if (empty(discountLines)) {
            inverseDiscounts = Collections.emptyList();
        } else {
            inverseDiscounts = new ArrayList<K>(discountLines.size());
            for (DiscountLine<K> discountLine : discountLines) {
                inverseDiscounts.add(discountLine.getDiscount().inverse());
            }
        }

        final S inverseServiceCharge;
        if (serviceChargeLine == null) {
            inverseServiceCharge = null;
        } else {
            inverseServiceCharge = serviceChargeLine.getServiceCharge().inverse();
        }

        return new Cart<T, D, K, S>(inverseItems, inverseDiscounts, inverseServiceCharge);
    }

    /**
     * The actual value of the cart, when all kinds of discounts has been taken into consideration
     * @return the actual value of the cart
     */
    public long getValue() {
        return grossValue - coalesce(cartWideDiscountValue, 0L) + coalesce(serviceChargeValue, 0L);
    }

    /**
     * The carts list of items lines. Each provided item at construction time will have it's own line, Each line will
     * contain additional information, specific to the context of this cart, such as actual amounts and VATs
     * @return an immutable list of item lines
     */
    public List<ItemLine<T, D>> getItemLines() {
        return Collections.unmodifiableList(itemLines);
    }

    /**
     * The carts list of discount lines. Each provided discount at construction time will have it's own line, Each line
     * contain additional information, specific to the context of this cart, such as actual amounts
     * @return an immutable list of discount lines
     */
    public List<DiscountLine<K>> getDiscountLines() {
        return Collections.unmodifiableList(discountLines);
    }

    /**
     * The total value of this cart before cart wide discounts has been applied
     * @return the gross value of the cart
     */
    public long getGrossValue() {
        return grossValue;
    }

    /**
     * The actual value of all cart wide discounts and item line discounts.
     * @return the amount all discounts
     */
    public Long getDiscountValue() {
        return discountValue;
    }

    /**
     * The number of discounts, both item line discounts and cart-wide discounts.
     * @return Total number of discounts.
     */
    public int getNumberOfDiscounts() {
        int numberOfDiscounts = 0;

        if (discountLines != null) {
            numberOfDiscounts = discountLines.size();
        }

        for (ItemLine<T, D> itemLine : itemLines) {
            if (itemLine.getItem().getDiscount() != null) {
                numberOfDiscounts++;
            }
        }

        return numberOfDiscounts;
    }

    /**
     * The actual VAT amount for this cart. That is the sum of all item lines's vat percentage applied to each lines
     * actual value
     * @return The actual VAT amount for this cart, or null of VAT is not applicable
     */
    public Long getActualVat() {
        return actualVat;
    }

    /**
     * The VAT amount for this cart should there be no cart wide discounts.
     * @return The VAT amount for this cart before cart wide discounts, or null if VAT is not applicable
     */
    public Long getGrossVat() {
        return grossVat;
    }

    /**
     * The amount VAT that's "lost" after applying cart-wide discounts, eg the difference between the gross VAT and
     * actual VAT
     * @return the discount VAT, or null if VAT is not applicable
     */
    public Long getDiscountVat() {
        if (grossVat == null || actualVat == null || discountValue == null) {
            return null;
        }
        return grossVat - actualVat;
    }

    /**
     * Given multiple cart-wide discounts, using both percentages and amounts, this is the actual percentage applied to
     * the cart's gross amount. This value won't always be identical to the provided percentage, even if there is only
     * one cart-wide discount with only percentage. This is because rounding takes place per line. The purpose of this
     * method is to be able to get the resulting data. As an counter example, if only discounts with amounts are
     * provided, the returned value in getDiscountValue() will always be the expected.
     * @return the actual percentage the cart-wide discounts represents of the cart's gross value, or null if VAT is not
     * applicable
     */
    public Double getActualDiscountPercentage() {
        return actualDiscountPercentage;
    }

    /**
     * Will produce a summary of data per used VAT percentage, returned mapped by it's respective VAT percentage
     * @return the map of values by percentage, or empty maps if VAT is not applicable
     */
    public SortedMap<Float, VatGroupValues> groupValuesByVatPercentage() {
        return CartUtils.groupValuesByVatPercentage(getItemLines(), getServiceChargeLine());
    }

    public ServiceChargeLine<S> getServiceChargeLine() {
        return serviceChargeLine;
    }

    public Long getServiceChargeValue() {
        return serviceChargeValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cart {\n");
        sb.append("\tItemLines:\n");
        for (ItemLine<T, D> itemLine : itemLines) {
            sb.append("\t\t").append(itemLine).append("\n");
        }
        sb.append("\tDiscountLines:\n");
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
        sb.append("\t\tService Charge Value: ").append(this.getServiceChargeValue()).append("\n");
        sb.append("\t\tVAT: ").append(this.getActualVat()).append("\n");
        sb.append('}');
        return sb.toString();
    }
}
