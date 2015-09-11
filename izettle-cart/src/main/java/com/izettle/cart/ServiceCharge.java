package com.izettle.cart;

public interface ServiceCharge<S> {

    /**
     * The percent VAT that is applied to this service charge, can be null for situations where VAT is not applicable
     * @return the vat percentage
     */
    Float getVatPercentage();

    /**
     * The amount that this service charge affects is target with
     * @return The amount, can be null if the percentage is not
     */
    Long getAmount();

    /**
     * The percentage that this service charge affects is target with. The actual effect will depend on properties on the
     * target, such as it's gross amount
     * @return The percentage, can be null if the amount is not
     */
    Double getPercentage();

    /**
     * Utility method that subclasses need to implement. Inverse here, means the concept of negating the service charge, which
     * would normally be done by cloning it's own fields, but negating the sign on the amount. Used for situations
     * such as refunds
     * @return the inversed ServiceCharge
     */
    S inverse();

}
