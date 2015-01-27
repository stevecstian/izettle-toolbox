package com.izettle.cart;

import java.math.BigDecimal;

public class TestDiscount implements Discount<TestDiscount> {

    private final Long amount;
    private final Double percentage;
    private final BigDecimal quantity;

    TestDiscount(Long amount, Double percentage, BigDecimal quantity) {
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
    public String toString() {
        return "TestDiscount{ amount = " + amount + ", percentage = " + percentage + ", quantity = " + quantity + '}';
    }

    @Override
    public BigDecimal getQuantity() {
        return this.quantity;
    }

    @Override
    public TestDiscount inverse() {
        return new TestDiscount(amount, percentage, quantity.negate());
    }
}