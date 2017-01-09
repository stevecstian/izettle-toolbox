package com.izettle.cart;

import java.math.BigDecimal;

/**
 * Concrete implementation of ServiceCharge interface used for internal temporary calculations: never exposed publicly
 */
class AlteredCartServiceCharge implements ServiceCharge<AlteredCartServiceCharge> {

    private final Float vatPercentage;
    private final Long amount;
    private final Double percentage;
    private final BigDecimal quantity;

    static <S extends ServiceCharge<S>> AlteredCartServiceCharge from(final S serviceCharge) {
        return new AlteredCartServiceCharge(
            serviceCharge.getVatPercentage(),
            serviceCharge.getAmount(),
            serviceCharge.getPercentage(),
            serviceCharge.getQuantity()
        );
    }

    private AlteredCartServiceCharge(
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
    public AlteredCartServiceCharge inverse() {
        return new AlteredCartServiceCharge(vatPercentage, amount, percentage, quantity.negate());
    }

    public AlteredCartServiceCharge withQuantity(final BigDecimal newQuantity) {
        return new AlteredCartServiceCharge(vatPercentage, amount, percentage, newQuantity);
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }
}
