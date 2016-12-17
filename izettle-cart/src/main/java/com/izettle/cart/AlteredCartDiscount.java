package com.izettle.cart;

import java.math.BigDecimal;

/**
 * Concrete implementation of Discount interface used for internal temporary calculations: never exposed publicly
 */
class AlteredCartDiscount implements Discount<AlteredCartDiscount> {

    private final Long amount;
    private final Double percentage;
    private final BigDecimal quantity;

    static <D extends Discount<D>> AlteredCartDiscount from(final D discount) {
        return new AlteredCartDiscount(discount.getAmount(), discount.getPercentage(), discount.getQuantity());
    }

    private AlteredCartDiscount(final Long amount, final Double percentage, final BigDecimal quantity) {
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
    public AlteredCartDiscount inverse() {
        return new AlteredCartDiscount(amount, percentage, quantity.negate());
    }

    public AlteredCartDiscount withQuantity(final BigDecimal newQuantity) {
        return new AlteredCartDiscount(amount, percentage, newQuantity);
    }

}
