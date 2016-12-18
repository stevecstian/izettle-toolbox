package com.izettle.cart;

import java.math.BigDecimal;

/**
 * Concrete implementation of Discount interface used for internal temporary calculations: never exposed publicly
 */
class TempDiscount implements Discount<TempDiscount> {

    private final Long amount;
    private final Double percentage;
    private final BigDecimal quantity;

    static <D extends Discount<D>> TempDiscount from(final D discount) {
        return new TempDiscount(discount.getAmount(), discount.getPercentage(), discount.getQuantity());
    }

    private TempDiscount(final Long amount, final Double percentage, final BigDecimal quantity) {
        this.amount = amount;
        this.percentage = percentage;
        this.quantity = quantity;
    }

    @Override
    public Long getAmount() {
        return amount;
    }

    @Override
    public Double getPercentage() {
        return percentage;
    }

    @Override
    public BigDecimal getQuantity() {
        return this.quantity;
    }

    @Override
    public TempDiscount inverse() {
        return new TempDiscount(amount, percentage, quantity.negate());
    }

    public TempDiscount withQuantity(final BigDecimal newQuantity) {
        return new TempDiscount(amount, percentage, newQuantity);
    }

}
