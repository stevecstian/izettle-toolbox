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
import java.util.SortedMap;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.Test;

public class AlterationTest {

    @Test(expected = CartException.class)
    public void itShouldNotAcceptItemsThatsNotInOriginalCart() {

        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final Object id3 = UUID.randomUUID();

        final TestItem item1 = createItem(id1, 10L, null, BigDecimal.ONE);
        final TestItem item2 = createItem(id2, 10L, null, BigDecimal.ONE);

        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(item1, item2);

        originalCart.applyAlteration(Collections.singletonMap(id3, BigDecimal.ONE));
    }

    @Test(expected = CartException.class)
    public void itShouldNotAcceptItemsWithGreaterQuantityThanLeftInOriginalCart() {
        final Object id = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id, 10L, null, BigDecimal.TEN)
        );

        originalCart.createAlterationCart(
            singletonList(singletonMap(id, BigDecimal.valueOf(-9L))),
            singletonMap(id, BigDecimal.valueOf(-2L))
        );
    }

    @Test
    public void itShouldAcceptEnoughRemainingQuantity() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id1, 10L, 30f, BigDecimal.TEN),
            createItem(id2, 100L, 30f, BigDecimal.TEN)
        );

        final Map<Object, BigDecimal> previousAlterations = new HashMap<Object, BigDecimal>();
        previousAlterations.put(id1, BigDecimal.ONE.negate());
        previousAlterations.put(id2, BigDecimal.ONE.negate());

        final Map<Object, BigDecimal> newAlteration = singletonMap(id1, BigDecimal.ONE.negate());

        long alterationValue = originalCart
            .createAlterationCart(singletonList(previousAlterations), newAlteration)
            .getValue();
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
        final Object id1 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            new TestItem(id1, "Grain of salt", 1L, 30f, BigDecimal.TEN, new TestDiscount(null, 50d, BigDecimal.ONE))
        );
        assertEquals(originalCart.getValue(), 5L);
        final List<Map<Object, BigDecimal>> previousAlterations = new ArrayList<Map<Object, BigDecimal>>();
        final int[] expectedValues = {-1, 0, -1, 0, -1, 0, -1, 0, -1, 0};
        for (int i = 0; i < 10; i++) {
            final Map<Object, BigDecimal> newAlteration = singletonMap(id1, BigDecimal.ONE.negate());
            final long alterationValue = originalCart
                .createAlterationCart(previousAlterations, newAlteration)
                .getValue();
            assertEquals(
                "Expected the alteration to have value: " + expectedValues[i],
                expectedValues[i],
                alterationValue
            );
            previousAlterations.add(newAlteration);
        }
    }

    @Test
    public void itShouldHandleLineItemDiscounts() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final Object id3 = UUID.randomUUID();
        final TestItem itemWithPercentageDiscount = new TestItem(
            id1, "", 100L, 30f, new BigDecimal("4"), new TestDiscount(null, 50d, BigDecimal.ONE)
        );
        final TestItem itemWithFixedDiscount = new TestItem(
            id2, "", 200, 10f, new BigDecimal("4"), new TestDiscount(100L, null, BigDecimal.ONE)
        );
        final TestItem itemWithNoDiscount = new TestItem(
            id3, "", 300, 10f, new BigDecimal("4"), null
        );
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            itemWithPercentageDiscount,
            itemWithFixedDiscount,
            itemWithNoDiscount
        );
        assertEquals(2100L, originalCart.getValue());
        //refund an item part of a percentage disount: it shouldn't have it's full value
        final Map<Object, BigDecimal> alteration1 = singletonMap(id1, BigDecimal.ONE.negate());
        final long alterationValue1 = originalCart.createAlterationCart(null, alteration1).getValue();
        assertEquals(-50L, alterationValue1);
        //refund an item part of a fixed amount disount: it shouldn't have it's full value
        final Map<Object, BigDecimal> alteration2 = singletonMap(id2, BigDecimal.ONE.negate());
        final long alterationValue2 = originalCart.createAlterationCart(null, alteration2).getValue();
        assertEquals(-175L, alterationValue2);
        //refund an item without any discount at all should yield the same value as originally
        final Map<Object, BigDecimal> alteration3 = singletonMap(id3, BigDecimal.ONE.negate());
        final long alterationValue3 = originalCart.createAlterationCart(null, alteration3).getValue();
        assertEquals(-300L, alterationValue3);
    }

    @Test
    /**
     * Verifies that the returned map of available inventory is correct, and that all items are present even if they
     * have quantity of zero
     */
    public void itShouldReturnTheExpectedQuantitiesForRemainintItems() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final List<TestItem> itemList = Arrays.asList(
            createItem(id1, 100L, 30f, BigDecimal.TEN),
            createItem(id2, 300L, 10f, BigDecimal.valueOf(4))
        );
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(itemList, null, null);

        final Map<Object, BigDecimal> firstAlteration = Maps.newHashMap(id1, BigDecimal.valueOf(6).negate());
        final Map<Object, BigDecimal> remainingItemsAfterFirst = cart.getRemainingItems(Arrays.asList(firstAlteration));
        assertEquals(2, remainingItemsAfterFirst.size());
        assertEquals(BigDecimal.valueOf(4), remainingItemsAfterFirst.get(id1));
        assertEquals(BigDecimal.valueOf(4), remainingItemsAfterFirst.get(id2));

        final Map<Object, BigDecimal> secondAlteration = new HashMap<Object, BigDecimal>();
        secondAlteration.put(id1, BigDecimal.valueOf(4).negate());
        secondAlteration.put(id2, BigDecimal.valueOf(4).negate());
        final Map<Object, BigDecimal> remainingItemsAfterSecond = cart.getRemainingItems(
            Arrays.asList(firstAlteration, secondAlteration)
        );
        assertEquals(2, remainingItemsAfterFirst.size());
        assertEquals(BigDecimal.valueOf(0), remainingItemsAfterSecond.get(id1));
        assertEquals(BigDecimal.valueOf(0), remainingItemsAfterSecond.get(id2));
    }

    @Test
    /**
     * Verify that the protected method `applyAlteration` does what it should, and that discounts are changed
     * proportionally to the size of the alteration
     */
    public void itShouldApplyAlterationAsExpected() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
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
        final Cart<AlteredCartItem, AlteredCartDiscount, AlteredCartDiscount, AlteredCartServiceCharge> alteredCart =
            originalCart.applyAlteration(Maps.newHashMap(id1, BigDecimal.valueOf(4).negate()));
        assertEquals(originalCart.getValue(), 1750L);
        assertEquals(alteredCart.getValue(), 1284L);
        assertEquals(originalCart.getDiscountValue().longValue(), 100L);
        assertEquals(alteredCart.getDiscountValue().longValue(), 75L);
    }

    @Test
    public void itShouldGroupAlteredVatsAsExpected() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final Object id3 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart = createCart(
            createItem(id1, 100L, 6f, new BigDecimal("2")),
            createItem(id2, 200L, 12f, new BigDecimal("2")),
            createItem(id3, 300L, 25f, new BigDecimal("2"))
        );
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cartAlteration = cart
            .createAlterationCart(null, Maps.newHashMap(id1, BigDecimal.valueOf(1).negate()));
        final SortedMap<Float, VatGroupValues> groupedVats = cartAlteration.groupValuesByVatPercentage();
        //only the 6% VAT should be affected, and represent a value: the rest is not affected by this alteration
        assertEquals(-5L, groupedVats.get(6f).getActualVatValue());
        assertEquals(0L, groupedVats.get(12f).getActualVatValue());
        assertEquals(0L, groupedVats.get(25f).getActualVatValue());
    }

    @Test
    /**
     * Verifies that there is no amounts remaining after all items has been removed, even if there was a fixed line item
     * discount
     */
    public void itShouldHandleFullDrainageForFixedLineItemDiscounts() {
        final Object id1 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart = createCart(
            new TestItem(id1, "Main thing", 1L, 30f, BigDecimal.valueOf(1L), new TestDiscount(10L, null, BigDecimal.ONE))
        );
        final long originalValue = cart.getValue();
        final Map<Object, BigDecimal> currentRefund = Maps.newHashMap(id1, BigDecimal.ONE.negate());
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> alterationCart = cart
            .createAlterationCart(null, currentRefund);
        final long alterationValue = alterationCart.getValue();
        assertEquals(originalValue, -1 * alterationValue);
    }

    @Test
    /**
     * Verifies that there is no amounts remaining after all items has been removed, even if there was a percentage line
     * item discount
     */
    public void itShouldHandleFullDrainageForPercentageLineItemDiscounts() {
        final Object id = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart = createCart(
            new TestItem(id, "Main thing", 1L, 30f, BigDecimal.valueOf(1L), new TestDiscount(null, 50d, BigDecimal.ONE))
        );
        final long originalValue = cart.getValue();
        final Map<Object, BigDecimal> currentRefund = Maps.newHashMap(id, BigDecimal.ONE.negate());
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> alterationCart2 = cart
            .createAlterationCart(null, currentRefund);
        final long alterationValue2 = alterationCart2.getValue();
        assertEquals(originalValue, -1 * alterationValue2);
    }

    @Test
    /**
     * Verifies that there is no amounts remaining after all items has been removed, even if there was a fixed cart wide
     * discount
     */
    public void itShouldHandleFullDrainageForFixedCartWideDiscounts() {
        final Object id = UUID.randomUUID();
        final TestDiscount discount = new TestDiscount(10L, null, BigDecimal.ONE);
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart
            = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(
                Arrays.asList(createItem(id, 10L, 30f, BigDecimal.valueOf(2L))),
                Arrays.asList(discount),
                null
            );
        final long originalValue = cart.getValue();
        final Map<Object, BigDecimal> firstAlteration = Maps.newHashMap(id, BigDecimal.ONE.negate());
        final Map<Object, BigDecimal> currentRefund = Maps.newHashMap(id, BigDecimal.ONE.negate());
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> firstAlterationCart = cart
            .createAlterationCart(null, firstAlteration);
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> secondAlterationCart = cart
            .createAlterationCart(Arrays.asList(firstAlteration), currentRefund);
        final long firstAlterationValue = firstAlterationCart.getValue();
        final long secondalterationValue = secondAlterationCart.getValue();
        assertEquals(originalValue, -1 * (firstAlterationValue + secondalterationValue));
    }

    @Test
    /**
     * Verifies that there is no amounts remaining after all items has been removed, even if there was a percentage cart
     * wide discount
     */
    public void itShouldHandleFullDrainageForPercentageCartWideDiscounts() {
        final Object id = UUID.randomUUID();
        final TestDiscount discount = new TestDiscount(null, 20d, BigDecimal.ONE);
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart
            = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(
                Arrays.asList(createItem(id, 10L, 30f, BigDecimal.valueOf(2L))),
                Arrays.asList(discount),
                null
            );
        final long originalValue = cart.getValue();
        final Map<Object, BigDecimal> firstAlteration = Maps.newHashMap(id, BigDecimal.ONE.negate());
        final Map<Object, BigDecimal> currentRefund = Maps.newHashMap(id, BigDecimal.ONE.negate());
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> firstAlterationCart = cart
            .createAlterationCart(null, firstAlteration);
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> secondAlterationCart = cart
            .createAlterationCart(Arrays.asList(firstAlteration), currentRefund);
        final long firstAlterationValue = firstAlterationCart.getValue();
        final long secondalterationValue = secondAlterationCart.getValue();
        assertEquals(originalValue, -1 * (firstAlterationValue + secondalterationValue));
    }

    @Test
    public void itShouldCalculateCorrectCartWideDiscountValueAfterAlteration() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart
            = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(
                Arrays.asList(
                    new TestItem(id1, "banan", 1000L, 30f, BigDecimal.valueOf(2L), new TestDiscount(200L, null, BigDecimal.ONE)),
                    new TestItem(id2, "äpple", 1000L, 30f, BigDecimal.valueOf(1L), null)
                ),
                Arrays.asList(new TestDiscount(600L, null, BigDecimal.ONE)),
                null
            );
        final Map<Object, BigDecimal> currentAlteration = Maps.newHashMap(id1, BigDecimal.ONE.negate());
        final AlterationCart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> firstAlterationCart = cart
            .createAlterationCart(null, currentAlteration);
        final long originalCartWideDiscount = cart.getCartWideDiscountValue();
        assertEquals(600L, originalCartWideDiscount);
        final long alterationCartWideDiscount = firstAlterationCart.getCartWideDiscountValue();
        assertEquals(-193L, alterationCartWideDiscount);
    }

    @Test
    /**
     * Verifiy that a cart, after previous alterations, has the correct items (and it's quantities) available for
     * further alterations.
     */
    public void itShouldPresentCorrectRemainingItems() {
        final Object id1 = UUID.randomUUID();
        final Object id2 = UUID.randomUUID();
        final List<TestItem> itemList = Arrays.asList(
            createItem(id1, 100L, 30f, new BigDecimal("4")),
            createItem(id2, 300L, 10f, new BigDecimal("3.1415"))
        );
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(
                itemList,
                Arrays.asList(new TestDiscount(100L, null, BigDecimal.ONE)),
                new TestServiceCharge(30f, 100L, 10d, BigDecimal.ONE)
            );
            final List<Map<Object, BigDecimal>> previousAlterations = Lists.newArrayList(
                Maps.newHashMap(id1, BigDecimal.valueOf(4).negate())
            );
        final Map<Object, BigDecimal> alterableItems = originalCart.getRemainingItems(previousAlterations);
        assertEquals(
            "Expected 2 type of items to be alterable, but was " + alterableItems.size(),
            2,
            alterableItems.size()
        );
        assertEquals(
            "Expected item with id " + id2 + " to be alterable with a quantity of 3.1415",
            new BigDecimal("3.1415"),
            alterableItems.get(id2)
        );
    }

    private TestItem createItem(
        final Object id,
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
