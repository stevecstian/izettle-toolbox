package com.izettle.cart;

import java.math.BigDecimal;

/**
 * Concrete implementation of Item interface used for internal temporary calculations: never exposed publicly
 */
class TempItem implements Item<TempItem, TempDiscount> {

    private final long unitPrice;
    private final Float vatPercentage;
    private final BigDecimal quantity;
    private final TempDiscount discount;
    private final Comparable<?> id;

    static <T extends Item<T, D>, D extends Discount<D>> TempItem from(final T item) {
        return new TempItem(
            item.getId(),
            item.getUnitPrice(),
            item.getVatPercentage(),
            item.getQuantity(),
            item.getDiscount() != null ? TempDiscount.from(item.getDiscount()) : null
        );
    }

    private TempItem(
        final Comparable<?> id,
        final long unitPrice,
        final Float vatPercentage,
        final BigDecimal quantity,
        final TempDiscount discount
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
    public TempItem inverse() {
        return new TempItem(
            id,
            unitPrice,
            vatPercentage,
            quantity.negate(),
            discount != null ? discount.inverse() : null
        );
    }

    public TempItem withQuantity(final BigDecimal newQuantity) {
        return new TempItem(
            id,
            unitPrice,
            vatPercentage,
            newQuantity,
            discount
        );
    }

    @Override
    public TempDiscount getDiscount() {
        return discount;
    }
}
