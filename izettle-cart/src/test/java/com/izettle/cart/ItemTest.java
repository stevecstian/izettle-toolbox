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

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);

        assertThat(cart.getItemLines().get(0).getDiscountValue()).isEqualTo(50L);
        assertThat(cart.getItemLines().get(1).getDiscountValue()).isEqualTo(500L);
        assertThat(cart.getItemLines().get(2).getDiscountValue()).isNull();
    }

    @Test
    public void testTaxableValueIsCorrect() {
        //Arrange
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart;
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(
            UUID.randomUUID(),
            "Without VAT",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(null, 50d, BigDecimal.ONE)
        ));
        items.add(new TestItem(
            UUID.randomUUID(),
            "With VAT",
            1000L,
            25f,
            BigDecimal.ONE,
            new TestDiscount(null, 50d, BigDecimal.ONE)
        ));
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 20d, BigDecimal.ONE));

        //Act
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);

        //Assert
        assertEquals(800L, cart.getValue());
        List<ItemLine<TestItem, TestDiscount>> itemLines = cart.getItemLines();
        ItemLine<TestItem, TestDiscount> line1 = itemLines.get(0);
        assertEquals(400L, line1.getActualValue());
        assertEquals(400L, line1.getActualTaxableValue());
        ItemLine<TestItem, TestDiscount> line2 = itemLines.get(1);
        assertEquals(400L, line2.getActualValue());
        assertEquals(320L, line2.getActualTaxableValue());
    }

    @Test
    public void testThatInverseIsCorrectWhenIncludingDiscounts() {
        List<TestItem> items;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> inverse;

        items = new ArrayList<TestItem>();
        items.add(new TestItem(
            UUID.randomUUID(),
            "name",
            1000L,
            null,
            BigDecimal.ONE,
            new TestDiscount(50L, null, BigDecimal.ONE)
        ));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);
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
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);
        inverse = cart.inverse();
        assertEquals(cart.getValue(), -1L * inverse.getValue());

    }
}
