package com.izettle.cart;

import java.math.BigDecimal;

/**
 * Concrete implementation of Item interface used for internal temporary calculations: never exposed publicly
 */
class AlteredCartItem implements Item<AlteredCartItem, AlteredCartDiscount> {

    private final long unitPrice;
    private final Float vatPercentage;
    private final BigDecimal quantity;
    private final AlteredCartDiscount discount;
    private final Comparable<?> id;

    static <T extends Item<T, D>, D extends Discount<D>> AlteredCartItem from(final T item) {
        return new AlteredCartItem(
            item.getId(),
            item.getUnitPrice(),
            item.getVatPercentage(),
            item.getQuantity(),
            item.getDiscount() != null ? AlteredCartDiscount.from(item.getDiscount()) : null
        );
    }

    private AlteredCartItem(
        final Comparable<?> id,
        final long unitPrice,
        final Float vatPercentage,
        final BigDecimal quantity,
        final AlteredCartDiscount discount
    ) {
        this.id = id;
        this.unitPrice = unitPrice;
        this.vatPercentage = vatPercentage;
        this.quantity = quantity;
        this.discount = discount;
    }

    @Override
    public long getUnitPrice() {
        return unitPrice;
    }

    @Override
    public Float getVatPercentage() {
        return vatPercentage;
    }

    @Override
    public BigDecimal getQuantity() {
        return this.quantity;
    }

    @Override
    public Comparable<?> getId() {
        return this.id;
    }

    @Override
    public AlteredCartItem inverse() {
        return new AlteredCartItem(
            id,
            unitPrice,
            vatPercentage,
            quantity.negate(),
            discount != null ? discount.inverse() : null
        );
    }

    public AlteredCartItem withQuantity(final BigDecimal newQuantity) {
        return new AlteredCartItem(
            id,
            unitPrice,
            vatPercentage,
            newQuantity,
            discount
        );
    }

    @Override
    public AlteredCartDiscount getDiscount() {
        return discount;
    }
}
