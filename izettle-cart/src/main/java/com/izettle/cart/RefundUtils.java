package com.izettle.cart;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefundUtils {

    static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
        void validateItems(
            final Cart<T, D, K, S> originalCart,
            final Collection<Cart<T, D, K, S>> previousRefunds,
            final Collection<T> itemsToRefund
    ) {
        final Map<Comparable, BigDecimal> originalQuantities = getQuantityById(originalCart);
        final Map<Comparable, BigDecimal> refundedQuantities = getQuantityById(previousRefunds);
        for (T item : itemsToRefund) {
            verifyItemSimilarity(originalCart, item);
            verifyItemSimilarity(previousRefunds, item);
            verifyEnoughRemaining(originalQuantities, refundedQuantities, item);
        }
    }

    static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
        void verifyItemSimilarity(
            final Collection<Cart<T, D, K, S>> carts,
            final T itemToRefund
    ) {
        if (carts == null) {
            return;
        }
        for (Cart<T, D, K, S> cart : carts) {
            RefundUtils.verifyItemSimilarity(cart, itemToRefund);
        }
    }

    static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
        void verifyItemSimilarity(
            final Cart<T, D, K, S> originalCart,
            final T itemToRefund
    ) {
        List<ItemLine<T, D>> itemLines = originalCart.getItemLines();
        for (ItemLine<T, D> itemLine : itemLines) {
            final T item = itemLine.getItem();
            if (item.getId().equals(itemToRefund.getId())) {
                if (item.getUnitPrice() != itemToRefund.getUnitPrice()) {
                    throw new IllegalArgumentException(
                        "Item with id " + item.getId()
                        + " cannot be refunded as it's unit price " + itemToRefund.getUnitPrice()
                        + " differs from original cart " + item.getUnitPrice());
                }
                if (!percentageEquals(item.getVatPercentage(), itemToRefund.getVatPercentage())) {
                    throw new IllegalArgumentException(
                        "Item with id " + item.getId()
                        + " cannot be refunded as it's vat percentage " + itemToRefund.getVatPercentage()
                        + " differs from original cart " + item.getVatPercentage());
                }
            }
        }
    }

    static <T extends Item<T, D>, D extends Discount<D>> void verifyEnoughRemaining(
        final Map<Comparable, BigDecimal> originalQuantities,
        final Map<Comparable, BigDecimal> refundedQuantities,
        final T itemToRefund
    ) {
        final Comparable id = itemToRefund.getId();
        final BigDecimal originalQuantity = originalQuantities.get(id);
        if (originalQuantity == null) {
            throw new IllegalArgumentException(
                "Cannot refund, as original cart did not contain any item with id: " + id
            );
        }
        final BigDecimal refundedQuantity = CartUtils.coalesce(refundedQuantities.get(id), BigDecimal.ZERO);
        final BigDecimal remaining = originalQuantity
            .add(refundedQuantity)
            .add(itemToRefund.getQuantity());
        if (remaining.signum() < 0) {
            throw new IllegalArgumentException(
                "Cannot refund more than " + remaining
                + " of item with id " + id
                + " but tried to refund " + itemToRefund.getQuantity()
            );
        }
    }

    static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
        Map<Comparable, BigDecimal> getQuantityById(
            final Collection<Cart<T, D, K, S>> carts
    ) {
        if (carts == null || carts.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        final Map<Comparable, BigDecimal> quantityById = new HashMap<Comparable, BigDecimal>();
        for (Cart<T, D, K, S> cart : carts) {
            for (ItemLine<T, D> itemLine : cart.getItemLines()) {
                final T item = itemLine.getItem();
                final Comparable id = item.getId();
                final BigDecimal quantity = CartUtils.coalesce(quantityById.get(id), BigDecimal.ZERO);
                quantityById.put(id, quantity.add(item.getQuantity()));
            }
        }
        return quantityById;
    }

    static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
        Map<Comparable, BigDecimal> getQuantityById(
            final Cart<T, D, K, S> cart
    ) {
        if (cart == null) {
            return Collections.EMPTY_MAP;
        }
        final Map<Comparable, BigDecimal> quantityById = new HashMap<Comparable, BigDecimal>();
        for (ItemLine<?, ?> itemLine : cart.getItemLines()) {
            final Item<?, ?> item = itemLine.getItem();
            final Comparable<?> id = item.getId();
            final BigDecimal quantity = CartUtils.coalesce(quantityById.get(id), BigDecimal.ZERO);
            quantityById.put(id, quantity.add(item.getQuantity()));
        }
        return quantityById;
    }

    private static boolean percentageEquals(final Float one, final Float two) {
        if (one == null && two == null) {
            return true;
        }
        if (one == null || two == null) {
            return false;
        }
        return Math.abs(one - two) < 0.000001F;
    }

}
