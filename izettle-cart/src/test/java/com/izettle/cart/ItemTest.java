package com.izettle.cart;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ItemTest {

    @Test
    public void testDiscountValue() {

        List<TestItem> items = new ArrayList<>();

        items.add(new TestItem(
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(50L, null, BigDecimal.ONE)
        ));

        items.add(new TestItem(
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(null, 50d, BigDecimal.ONE)
        ));

        items.add(new TestItem(
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            null
        ));

        Cart<TestItem, TestDiscount, TestDiscount> cart = new Cart<>(items, null);

        assertThat(cart.getItemLines().get(0).getDiscountValue()).isEqualTo(50L);
        assertThat(cart.getItemLines().get(1).getDiscountValue()).isEqualTo(500L);
        assertThat(cart.getItemLines().get(2).getDiscountValue()).isNull();
    }

    @Test
    public void testThatInverseIsCorrectWhenIncludingDiscounts() {
        List<TestItem> items;
        Cart<TestItem, TestDiscount, TestDiscount> cart;
        Cart<TestItem, TestDiscount, TestDiscount> inverse;

        items = new ArrayList<>();
        items.add(new TestItem(
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(50L, null, BigDecimal.ONE)
        ));
        cart = new Cart<>(items, null);
        inverse = cart.inverse();
        assertEquals(cart.getValue(), -1L * inverse.getValue());

        items = new ArrayList<>();
        items.add(new TestItem(
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(null, 25d, BigDecimal.ONE)
        ));
        cart = new Cart<>(items, null);
        inverse = cart.inverse();
        assertEquals(cart.getValue(), -1L * inverse.getValue());

    }
}
