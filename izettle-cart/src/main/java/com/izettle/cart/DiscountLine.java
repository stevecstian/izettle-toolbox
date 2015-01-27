package com.izettle.cart;

/**
 * Representing a line of a discount in a cart. The line consists of it's Discount member (of type K, originally
 * provided by the caller), and some calculated values that's relevant to it's context and location in the cart
 * @param <K> the type of Discount that this line contains, the same object that originally was provided by the caller
 */
public class DiscountLine<K extends Discount<K>> {

    private final K discount;
    private final Double actualPercentage;
    private final Long value;

    DiscountLine(K discount, Double actualPercentage, Long value) {
        this.discount = discount;
        this.actualPercentage = actualPercentage;
        this.value = value;
    }

    /**
     * Returns the Discount object originally provided at creation time. This is a utility function, enabling the
     * calling code to keep references to it's own data structures, while allowing this library to still perform all
     * financial calculations
     * @return The originally provided discount object
     */
    public K getDiscount() {
        return discount;
    }

    /**
     * Percentage of the cart gross amount that this line represents. This is the combination of the discounts
     * percentage and amount, multiplied with the quantity.
     * @return the percentage, or null of there is no discount
     */
    public Double getActualPercentage() {
        return actualPercentage;
    }

    /**
     * The value of this discount line. If the originally provided Discount had a fixed amount, this will be the same as
     * that, while if the originally provided discount had a percentage, this will be the resulting/actual amount that
     * discount affects the cart with
     * @return The actual value this discount represents, or null if there is no discount
     */
    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return ""
            + "DiscountLine {"
            + " discount = " + discount
            + ", actualPercentage = " + actualPercentage
            + ", value = " + value
            + '}';
    }
}
