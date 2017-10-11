package com.izettle.cart;

import static com.izettle.cart.CartUtils.coalesce;

import com.izettle.cart.exception.InsufficientQuantityException;
import com.izettle.cart.exception.UnknownItemException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AlterationUtils {

    static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>>
        void validateItems(
        final Cart<T, D, K, S> originalCart,
        final Map<Object, BigDecimal> alteredItems
    ) {
        final Map<Object, BigDecimal> originalQuantities = getQuantityById(originalCart);
        for (Map.Entry<Object, BigDecimal> entry : alteredItems.entrySet()) {
            final Object itemId = entry.getKey();
            final BigDecimal quantityChange = entry.getValue();
            final BigDecimal originalQuantity = originalQuantities.get(itemId);
            if (originalQuantity == null) {
                throw new UnknownItemException(
                    "Cannot alter quantity, as original cart did not contain any item with id: " + itemId
                );
            }
            final BigDecimal remaining = originalQuantity.add(quantityChange);
            if (remaining.signum() < 0) {
                throw new InsufficientQuantityException(
                    "Cannot alter quantity more than " + originalQuantity
                    + " items with id " + itemId
                    + " but tried changing with " + quantityChange
                );
            }
        }
    }

    private static <T extends Item<T, D>, D extends Discount<D>, K extends Discount<K>, S extends ServiceCharge<S>, I extends Object>
        Map<Object, BigDecimal> getQuantityById(
        final Cart<T, D, K, S> cart
    ) {
        if (cart == null) {
            return Collections.EMPTY_MAP;
        }
        final Map<Object, BigDecimal> quantityById = new HashMap<Object, BigDecimal>();
        for (ItemLine<T, D> itemLine : cart.getItemLines()) {
            final T item = itemLine.getItem();
            final Object id = item.getId();
            quantityById.put(id, coalesce(quantityById.get(id), BigDecimal.ZERO).add(item.getQuantity()));
        }
        return quantityById;
    }

    static Map<Object, BigDecimal> mergeAlterations(
        final List<Map<Object, BigDecimal>> alterations
    ) {
        //aggregate all alterations into one:
        final Map<Object, BigDecimal> mergedAlterations = new HashMap<Object, BigDecimal>();
        if (alterations != null) {
            for (Map<Object, BigDecimal> previousAlteration : alterations) {
                for (Entry<Object, BigDecimal> entry : previousAlteration.entrySet()) {
                    final Object id = entry.getKey();
                    final BigDecimal quantity = entry.getValue();
                    mergedAlterations.put(id, coalesce(mergedAlterations.get(id), BigDecimal.ZERO).add(quantity));
                }
            }
        }
        return mergedAlterations;
    }
}
