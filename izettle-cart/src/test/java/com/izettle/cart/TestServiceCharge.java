package com.izettle.cart;

import java.math.BigDecimal;

public class TestServiceCharge implements ServiceCharge<TestServiceCharge> {

    private final Float vatPercentage;
    private final Long amount;
    private final Double percentage;
    private final BigDecimal quantity;

    public TestServiceCharge(
        Float vatPercentage,
        Long amount,
        Double percentage,
        BigDecimal quantity
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
    public TestServiceCharge inverse() {
        return new TestServiceCharge(vatPercentage, amount, percentage, quantity.negate());
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }
}
