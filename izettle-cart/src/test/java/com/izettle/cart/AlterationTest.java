package com.izettle.cart;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import com.izettle.cart.exception.CartException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.util.Maps;
import org.junit.Test;

public class AlterationTest {

    @Test
    public void itShouldNotAcceptInvalidItems() {
    }

    @Test(expected = CartException.class)
    public void itShouldNotAcceptItemsThatsNotInOriginalCart() {

        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID id3 = UUID.randomUUID();

        final TestItem item1 = createItem(id1, 10L, null, BigDecimal.ONE);
        final TestItem item2 = createItem(id2, 10L, null, BigDecimal.ONE);

        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(item1, item2);

        originalCart.applyAlteration(Collections.singletonMap(id3, BigDecimal.ONE));
    }

    @Test(expected = CartException.class)
    public void itShouldNotAcceptItemsWithGreaterQuantityThanLeftInOriginalCart() {
        final UUID id = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id, 10L, null, BigDecimal.TEN)
        );

        originalCart.getAlterationValue(
            singletonList(singletonMap(id, BigDecimal.valueOf(-9L))),
            singletonMap(id, BigDecimal.valueOf(-2L))
        );
    }

    @Test
    public void itShouldAcceptEnoughRemainingQuantity() {
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id1, 10L, 30f, BigDecimal.TEN),
            createItem(id2, 100L, 30f, BigDecimal.TEN)
        );

        final Map<UUID, BigDecimal> previousAlterations = new HashMap<UUID, BigDecimal>();
        previousAlterations.put(id1, BigDecimal.ONE);
        previousAlterations.put(id2, BigDecimal.ONE);

        final Map<UUID, BigDecimal> newAlteration = singletonMap(id1, BigDecimal.ONE);

        final long alterationValue = originalCart.getAlterationValue(singletonList(previousAlterations), newAlteration);
        assertEquals("Expected the value of the alteration to be -10L", -10L, alterationValue);
    }

    @Test
    /**
     * When the original line item has a rounded value, refunding small quantities at a time can be problematic: looking
     * at one of those alterations separately would result in the same value each time. This in turn would make multiple
     * alterations add up to an amount higher or lower than the original cart. This test verifies that returning small
     * amounts will have different value each time until the entire value of the original cart is exhausted.
    */
    public void itShouldHandleMultipleAlterationsGracefully() {
        final UUID id1 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            new TestItem(id1, "", 1L, 30f, BigDecimal.TEN, new TestDiscount(null, 50d, BigDecimal.ONE))
        );
        assertEquals(originalCart.getValue(), 5L);
        final List<Map<UUID, BigDecimal>> previousAlterations = new ArrayList<Map<UUID, BigDecimal>>();
        final int[] expectedValues = {1, 0, 1, 0, 1, 0, 1, 0, 1, 0};
        for (int i = 0; i < 10; i++) {
            final Map<UUID, BigDecimal> newAlteration = singletonMap(id1, BigDecimal.ONE.negate());
            final long alterationValue = originalCart.getAlterationValue(previousAlterations, newAlteration);
            assertEquals(
                "Expected the alteration to have value: " + expectedValues[i],
                expectedValues[i],
                alterationValue
            );
            previousAlterations.add(newAlteration);
        }
    }

    @Test
    /**
     * Verify that the protected method `applyAlteration` does what it should
     */
    public void itShouldApplyAlterationAsExpected() {
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final List<TestItem> itemList = Arrays.asList(
            createItem(id1, 100L, 30f, new BigDecimal("4")),
            createItem(id2, 300L, 10f, new BigDecimal("4"))
        );
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(
                itemList,
                Arrays.asList(new TestDiscount(100L, null, BigDecimal.ONE)),
                new TestServiceCharge(30f, 100L, 10d, BigDecimal.ONE)
            );
        final Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> alteredCart = originalCart.applyAlteration(
            Maps.newHashMap(id1, BigDecimal.valueOf(4).negate())
        );
        assertEquals(originalCart.getValue(), 1750L);
        assertEquals(alteredCart.getValue(), 1284L);
        assertEquals(originalCart.getDiscountValue().longValue(), 100L);
        assertEquals(alteredCart.getDiscountValue().longValue(), 75L);
    }

    private TestItem createItem(
        final UUID id,
        final long unitPrice,
        final Float vatPercentage,
        final BigDecimal quantity
    ) {
        return new TestItem(id, "", unitPrice, vatPercentage, quantity, null);
    }

    private Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> createCart(TestItem... items) {
        final List<TestItem> itemList = Arrays.asList(items);
        return new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(itemList, null, null);
    }

}
