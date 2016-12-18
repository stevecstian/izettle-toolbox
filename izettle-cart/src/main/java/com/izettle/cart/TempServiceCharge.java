package com.izettle.cart;

import java.math.BigDecimal;

/**
 * Concrete implementation of ServiceCharge interface used for internal temporary calculations: never exposed publicly
 */
class TempServiceCharge implements ServiceCharge<TempServiceCharge> {

    private final Float vatPercentage;
    private final Long amount;
    private final Double percentage;
    private final BigDecimal quantity;

    static <S extends ServiceCharge<S>> TempServiceCharge from(final S serviceCharge) {
        return new TempServiceCharge(
            serviceCharge.getVatPercentage(),
            serviceCharge.getAmount(),
            serviceCharge.getPercentage(),
            serviceCharge.getQuantity()
        );
    }

    private TempServiceCharge(
        final Float vatPercentage,
        final Long amount,
        final Double percentage,
        final BigDecimal quantity
    ) {
        this.vatPercentage = vatPercentage;
        this.percentage = percentage;
        this.amount = amount;
        this.quantity = quantity;
    }

    @Override
    public Float getVatPercentage() {
        return vatPercentage;
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
    public TempServiceCharge inverse() {
        return new TempServiceCharge(vatPercentage, amount, percentage, quantity.negate());
    }

    public TempServiceCharge withQuantity(final BigDecimal newQuantity) {
        return new TempServiceCharge(vatPercentage, amount, percentage, newQuantity);
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }
}
