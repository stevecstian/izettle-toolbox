package com.izettle.cart;

/**
 * Object representing data that's interesting in situations when grouping different cart data by VAT percentage
 */
public class VatGroupValues {

    private final float vatPercentage;
    private final long actualVatValue;
    private final long actualValue;

    VatGroupValues(float vatPercentage, long actualVatValue, long actualValue) {
        this.vatPercentage = vatPercentage;
        this.actualVatValue = actualVatValue;
        this.actualValue = actualValue;
    }

    /**
     * The VAT percentage that this objects represents data for
     * @return the VAT percentage
     */
    public float getVatPercentage() {
        return vatPercentage;
    }

    /**
     * The sum of all VAT amount that is calculated for this objects percentage
     * @return the actual VAT value for this percentage
     */
    public long getActualVatValue() {
        return actualVatValue;
    }

    /**
     * The sum of all actual amounts that is calculated for this objects percentage
     * @return the actual amount for this percentage
     */
    public long getActualValue() {
        return actualValue;
    }

    @Override
    public String toString() {
        return "VatGroupValues{" + "vatPercentage=" + vatPercentage + ", actualVatValue=" + actualVatValue + ", actualValue=" + actualValue + '}';
    }


}
