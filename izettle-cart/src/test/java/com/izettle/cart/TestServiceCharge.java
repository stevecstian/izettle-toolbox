package com.izettle.cart;

public class TestServiceCharge implements ServiceCharge<TestServiceCharge> {

    private final Float vatPercentage;
    private final Long amount;
    private final Double percentage;

    public TestServiceCharge(
        Float vatPercentage,
        Long amount,
        Double percentage
    ) {
        this.vatPercentage = vatPercentage;
        this.percentage = percentage;
        this.amount = amount;
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
        if (null != amount) {
            return new TestServiceCharge(vatPercentage, -1 * amount, percentage);
        }

        return new TestServiceCharge(vatPercentage, null, percentage);
    }
}
