package com.izettle.cart;

import static com.izettle.cart.CartUtils.coalesce;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class Cart<T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
    implements Serializable {

    private static final long serialVersionUID = 8764117057191413242L;
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
     * Produces a new immutable cart object from Items, Discounts and Service Charge
     * @param items the list of items, must not be empty (as a cart without items makes no sense)
     * @param discounts the list of cart wide discounts, possibly null or empty
     * @param serviceCharge The applied service charge, possibly null
     */
    public Cart(final List<T> items, final List<K> discounts, final S serviceCharge) {
        ItemUtils.validateItems(items);
        final List<T> itemList = coalesce(items, Collections.<T>emptyList());
        DiscountUtils.validateDiscounts(discounts);
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
     * Creates a new cart representing the results after altering quantities of some of the items. This would typically
     * be used when doing a partial return
     * This instance is immutable and unaffected by this method call.
     * @param alteration The items to be altered in the cart. Needs to be a subset of the items in this cart
     * @return A newly created cart representing the state after the alteration. This cart is not intended to be exposed
     * outside of this package
     */
    Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> applyAlteration(
            final Map<Object, BigDecimal> alteration
    ) {
        final MathContext mathContext = new MathContext(20, CartUtils.ROUNDING_MODE);
        ItemUtils.validateQuantities(alteration.values());
        //Verify that all referenced items are present in the original cart
        AlterationUtils.validateItems(this, alteration);
        //reduce items
        final List<AlteredCartItem> remainingItems = new LinkedList<AlteredCartItem>();
        for (ItemLine<T, D> itemLine : this.getItemLines()) {
            final T originalItem = itemLine.getItem();
            final AlteredCartItem newItem;
            if (alteration.containsKey(originalItem.getId())) {
                final BigDecimal quantityChange = alteration.get(originalItem.getId());
                final BigDecimal newQuantity = originalItem.getQuantity().add(quantityChange);
                if (originalItem.getDiscount() != null && originalItem.getDiscount().getAmount() != null) {
                    final BigDecimal quantityChangeRatio = newQuantity.divide(originalItem.getQuantity(), mathContext);
                    final BigDecimal newDiscountQuantity = originalItem
                        .getDiscount()
                        .getQuantity()
                        .multiply(quantityChangeRatio);
                    final AlteredCartDiscount newDiscount = AlteredCartDiscount
                        .from(originalItem.getDiscount())
                        .withQuantity(newDiscountQuantity);
                    newItem = AlteredCartItem
                        .from(originalItem)
                        .withQuantity(newQuantity)
                        .withDiscount(newDiscount);
                } else {
                    newItem = AlteredCartItem
                        .from(originalItem)
                        .withQuantity(newQuantity);
                }
            } else {
                newItem = AlteredCartItem.from(originalItem);
            }
            if (newItem.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
                remainingItems.add(newItem);
            }
        }
        final long newGrossValue = CartUtils.getGrossValue(remainingItems);
        final List<AlteredCartDiscount> remainingDiscounts;
        final AlteredCartServiceCharge remainingServiceCharge;
        if (this.grossValue != 0) {
            //how much the gross value of the cart has changed
            final BigDecimal grossValueRatio = BigDecimal.valueOf(newGrossValue)
                .divide(BigDecimal.valueOf(this.grossValue), mathContext);
            //reduce discounts
            remainingDiscounts = new LinkedList<AlteredCartDiscount>();
            for (DiscountLine<K> discountLine : discountLines) {
                final K oldDiscount = discountLine.getDiscount();
                if (oldDiscount.getPercentage() != null && oldDiscount.getAmount() == null) {
                    //add discount as is, as it's percentage only
                    remainingDiscounts.add(AlteredCartDiscount.from(oldDiscount));
                } else {
                    final BigDecimal newQuantity = oldDiscount.getQuantity().multiply(grossValueRatio);
                    if (newQuantity.signum() != 0) {
                        remainingDiscounts.add(AlteredCartDiscount.from(oldDiscount).withQuantity(newQuantity));
                    }
                }
            }
            //reduce service charge
            if (serviceChargeLine == null) {
                remainingServiceCharge = null;
            } else {
                final S oldServiceCharge = serviceChargeLine.getServiceCharge();
                final BigDecimal newQuantity = oldServiceCharge.getQuantity().multiply(grossValueRatio);
                remainingServiceCharge = AlteredCartServiceCharge.from(oldServiceCharge).withQuantity(newQuantity);
            }
        } else {
            //previous cart held no gross value, just copy discounts and service charge as-is
            remainingDiscounts = new LinkedList<AlteredCartDiscount>();
            for (DiscountLine<K> discountLine : discountLines) {
                remainingDiscounts.add(AlteredCartDiscount.from(discountLine.getDiscount()));
            }
            if (serviceChargeLine == null) {
                remainingServiceCharge = null;
            } else {
                remainingServiceCharge = AlteredCartServiceCharge.from(serviceChargeLine.getServiceCharge());
            }
        }
        return new Cart(remainingItems, remainingDiscounts, remainingServiceCharge);
    }

    /**
     * Creates a new cart representing the results after performing multiple quantity alterations of some of the items.
     * This would typically be used when doing a partial return
     * This instance is immutable and unaffected by this method call.
     * @param alterations The items to be altered in the cart. Needs to be a subset of the items in this cart
     * @return A newly created cart representing the reduced cart
     */
    Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> applyAlterations(
            final List<Map<Object, BigDecimal>> alterations
    ) {
        final Map<Object, BigDecimal> mergedAlterations = AlterationUtils.mergeAlterations(alterations);
        return applyAlteration(mergedAlterations);
    }

    /**
     * Because it's impossible to calculate the value of a return without the context of it's original cart and possible
     * previous returns, this method does just that: calculates the value of a specific alteration.
     * As an example, let's consider a cart of 2 apples and 1 carrot with a % discount on the entire cart. First, there
     * is an alteration where one of the apples is returned. Then the carrot is returned, and now we ask: "How much
     * money is the return of the carrot worth?"
     * @param previousAlterations possibly previous alterations that needs to be taken into consideration
     * @param alteration the altered quantities to apply
     * @return the value of the alteration
     */
    public AlterationCart<T, D, K, S> createAlterationCart(
        final List<Map<Object, BigDecimal>> previousAlterations,
        final Map<Object, BigDecimal> alteration
    ) {
        final Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> cartBeforeLastAlteration
            = applyAlterations(previousAlterations);
        final Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> cartAfterLatestAlteration
            = cartBeforeLastAlteration.applyAlteration(alteration);
        return new AlterationCart(cartBeforeLastAlteration, cartAfterLatestAlteration, alteration);
    }

    /**
     * Utility method to retrieve the full list of items and their remaining quantity available for alteration.
     * @param previousAlterations possibly previous alterations that needs to be taken into consideration
     * @return A map of quantities available for alteration
    */
    public Map<Object, BigDecimal> getRemainingItems(final List<Map<Object, BigDecimal>> previousAlterations) {
        final Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> cartAfterAlterations
            = applyAlterations(previousAlterations);
        final Map<Object, BigDecimal> alterableItems = new HashMap<Object, BigDecimal>();
        if (cartAfterAlterations.itemLines != null) {
            for (ItemLine<AlteredCartItem, AlteredCartDiscount> itemLine : cartAfterAlterations.itemLines) {
                final AlteredCartItem item = itemLine.getItem();
                alterableItems.put(item.getId(), item.getQuantity());
            }
        }
        //fill out missing ones with zeroes
        for (ItemLine<T, D> itemLine : getItemLines()) {
            final Object key = itemLine.getItem().getId();
            if (!alterableItems.containsKey(key)) {
                alterableItems.put(itemLine.getItem().getId(), BigDecimal.ZERO);
            }
        }
        return alterableItems;
    }

    /**
     * Produces a new cart that is inversed, eg an identical cart where all quantities are negated. Useful for example
     * for full returns.
     * This instance is immutable and unaffected by this method call.
     * @return the inversed cart
     */
    public Cart<T, D, K, S> inverse() {
        //Copy all items and discounts, but negate the quantities:
        final List<T> inverseItems;
        if (itemLines.isEmpty()) {
            inverseItems = Collections.emptyList();
        } else {
            inverseItems = new ArrayList<T>(itemLines.size());
            for (ItemLine<T, D> itemLine : itemLines) {
                inverseItems.add(itemLine.getItem().inverse());
            }
        }
        final List<K> inverseDiscounts;
        if (discountLines.isEmpty()) {
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
     * Returns the total value of all cart-wide discounts (VAT still included)
     * @return the discount value, or null if there are no discounts
     */
    public Long getCartWideDiscountValue() {
        return cartWideDiscountValue;
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
