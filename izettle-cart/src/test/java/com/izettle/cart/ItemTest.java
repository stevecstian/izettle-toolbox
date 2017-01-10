package com.izettle.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ItemTest {

    @Test
    public void testDiscountValue() {

        List<TestItem> items = new ArrayList<TestItem>();

        items.add(new TestItem(
            UUID.randomUUID(),
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(50L, null, BigDecimal.ONE)
        ));

        items.add(new TestItem(
            UUID.randomUUID(),
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(null, 50.0d, BigDecimal.ONE)
        ));

        items.add(new TestItem(
            UUID.randomUUID(),
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            null
        ));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge, UUID> cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge, UUID>(items, null, null);

        assertThat(cart.getItemLines().get(0).getDiscountValue()).isEqualTo(50L);
        assertThat(cart.getItemLines().get(1).getDiscountValue()).isEqualTo(500L);
        assertThat(cart.getItemLines().get(2).getDiscountValue()).isNull();
    }

    @Test
    public void testThatInverseIsCorrectWhenIncludingDiscounts() {
        List<TestItem> items;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge, UUID> cart;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge, UUID> inverse;

        items = new ArrayList<TestItem>();
        items.add(new TestItem(
            UUID.randomUUID(),
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(50L, null, BigDecimal.ONE)
        ));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge, UUID>(items, null, null);
        inverse = cart.inverse();
        assertEquals(cart.getValue(), -1L * inverse.getValue());

        items = new ArrayList<TestItem>();
        items.add(new TestItem(
            UUID.randomUUID(),
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(null, 25.0d, BigDecimal.ONE)
        ));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge, UUID>(items, null, null);
        inverse = cart.inverse();
        assertEquals(cart.getValue(), -1L * inverse.getValue());

    }
}
