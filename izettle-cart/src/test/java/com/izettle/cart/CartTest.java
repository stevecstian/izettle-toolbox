package com.izettle.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CartTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void itShouldCalculateCorrectVatAndEffectivePrice() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(1299L, 25.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(0L, 0.0D, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(1299L, cart.getValue());
        assertEquals(0.0D, cart.getActualDiscountPercentage(), 0.0D);
        assEq(260L, cart.getActualVat());
    }

    @Test
    public void itShouldCalculateDiscountBasedOnTheLineItemsRoundedGrossValue() {
        /* Actual live bug when the cart library did calculate the discount amount based on the exact line amount
         * resulting in rounding in two different directions. This test assures that the total gross value of a line is
         * first calculated and rounded, then used as input for a possible discount calculation
         */
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(
            "Hogrev",
            14900L,
            null,
            new BigDecimal("1.092"),
            new TestDiscount(null, 50.0, BigDecimal.ONE)
        ));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);
        assertEquals(8135L, cart.getValue());
        /*
         * Just another (artificial, more extreme) example, to clarify:
         */
        List<TestItem> items2 = new LinkedList<TestItem>();
        items2.add(new TestItem(
            "Bananas",
            1L,
            null,
            new BigDecimal("1.4"),
            new TestDiscount(null, 40.0, BigDecimal.ONE)
        ));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart2 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items2, null, null);
        assertEquals(1L, cart2.getValue());
    }

    @Test
    public void itShouldCalculateCorrectVatAndEffectivePriceWithServiceChargePercentage() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(1299L, 25.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        TestServiceCharge serviceCharge = new TestServiceCharge(20.0f, null, 10.0D);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, serviceCharge);
        assertEquals(1429, cart.getValue());
        assEq(130L, cart.getServiceChargeValue());
        assEq(282L, cart.getActualVat());
    }

    @Test
    public void itShouldCalculateCorrectVatAndEffectivePriceWithServiceChargeFixedAmount() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(1299L, 25.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        TestServiceCharge serviceCharge = new TestServiceCharge(20.0f, 130L, null);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, serviceCharge);
        assertEquals(1429L, cart.getValue());
        assEq(130L, cart.getServiceChargeValue());
        assEq(282L, cart.getActualVat());
    }

    @Test
    public void itShouldCalculateCorrectVatAndEffectivePriceWithServiceChargeFixedAmountAndPercentage() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(1299L, 25.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        TestServiceCharge serviceCharge = new TestServiceCharge(20.0f, 130L, 10.0D);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, serviceCharge);
        assertEquals(1559L, cart.getValue());
        assEq(260L, cart.getServiceChargeValue());
        assEq(303L, cart.getActualVat());
    }

    @Test
    public void itShouldHandleNullVatProperly() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(1299L, null, new BigDecimal("1.0")));
        TestServiceCharge serviceCharge = new TestServiceCharge(null, 130L, null);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, serviceCharge);
        assertEquals(1429L, cart.getValue());
        assertNull(cart.getActualDiscountPercentage());
        assertNull(cart.getActualVat());
    }

    @Test
    public void itShouldHandleFixedDiscounts() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(1299L, 25.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(10L, 0.0D, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(1289L, cart.getValue());
        assEq(258L, cart.getActualVat());
        assertEquals(0.77d, cart.getActualDiscountPercentage(), 0.001d);
    }

    @Test
    public void itShouldDistributeVats() {
        List<TestItem> items = new LinkedList<TestItem>();
        items.add(new TestItem(500L, 10.0f, new BigDecimal("2.0")));
        items.add(new TestItem(100L, 50.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart1 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(1100L, cart1.getValue());
        assertEquals(1100L, cart1.getGrossValue());

        discounts.add(new TestDiscount(110L, 0.0D, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart2 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(10.0d, cart2.getActualDiscountPercentage(), 0.01d);
        assEq(990L, cart2.getValue());
        assEq(1100L, cart2.getGrossValue());

        List<ItemLine<TestItem, TestDiscount>> lineItems = cart2.getItemLines();
        assertEquals(2, lineItems.size());
        ItemLine<TestItem, TestDiscount> lineItem1 = lineItems.get(0);
        assEq(82L, lineItem1.getActualVat());
        ItemLine<TestItem, TestDiscount> lineItem2 = lineItems.get(1);
        assEq(30L, lineItem2.getActualVat());
        assEq(112L, cart2.getActualVat());
    }

    @Test
    public void itShouldReclaimDiscountsAfterRoundingError() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(33L, 10.0f, new BigDecimal("1.0")));
        items.add(new TestItem(33L, 10.0f, new BigDecimal("1.0")));
        items.add(new TestItem(32L, 50.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        //one fixed discount of 8
        discounts.add(new TestDiscount(8L, 0.0D, BigDecimal.ONE));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assEq(8L, cart.getDiscountValue());
        assEq(33L + 33L + 32L - 8L, cart.getValue());
    }

    @Test
    public void itShouldDistributeMoreAfterRoundingError() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(33L, 10.0f, new BigDecimal("1.0")));
        items.add(new TestItem(33L, 10.0f, new BigDecimal("1.0")));
        items.add(new TestItem(32L, 50.0f, new BigDecimal("1.0")));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        //one fixed discount of 10
        discounts.add(new TestDiscount(10L, 0.0D, BigDecimal.ONE));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assEq(10L, cart.getDiscountValue());
        assEq(33L + 33L + 32L - 10L, cart.getValue());
    }

    @Test
    public void itShouldDistributeDiscountValueOverDiscounts() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(93L, null, BigDecimal.ONE));
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        /*
         Adding 9 discounts with 10% each will give a rounding error on each discount line
         A naive implementation would then take these 9 for each discount, summarizing up to 37 in total discount, which
         would be suprising result, given that 93 * 0.9^9 ≃ 36
         */
        for (int i = 0; i < 9; i++) {
            discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE));
        }
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        //Verify that totals add up, and that we're as close as possible to a total discount of 90%
        assEq(36L, cart.getValue());
        assEq(57L, cart.getDiscountValue());
        assertEquals(61.0d, cart.getActualDiscountPercentage(), 1.0d);
        ////Verify that the sum of all discount items discount value equals the total discount:
        long totDiscountAmnt = 0L;
        for (DiscountLine<TestDiscount> discountLine : cart.getDiscountLines()) {
            totDiscountAmnt += discountLine.getValue();
        }
        assEq(cart.getDiscountValue(), totDiscountAmnt);
    }

    @Test
    public void itShouldCreateAProperInverse() {
        List<TestItem> items = new ArrayList<TestItem>();
        Random rnd = new Random();
        int iter = rnd.nextInt(100);
        for (int i = 0; i < iter; i++) {
            items.add(new TestItem(rnd.nextInt(10000), rnd.nextFloat() * 30.0f, new BigDecimal(String.valueOf(rnd
                .nextFloat()))));
        }
        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(2L, 0.0D, BigDecimal.TEN));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> inversedCart = cart.inverse();
        assEq(-1 * cart.getValue(), inversedCart.getValue());
        assEq(-1 * cart.getDiscountValue(), inversedCart.getDiscountValue());
        assEq(-1 * cart.getActualVat(), inversedCart.getActualVat());
        assertEquals(cart.getItemLines().size(), inversedCart.getItemLines().size());
    }

    @Test
    public void itShouldCreateAProperInverseWithServiceCharge() {
        List<TestItem> items = new ArrayList<TestItem>();
        Random rnd = new Random();
        int iter = rnd.nextInt(100);
        for (int i = 0; i < iter; i++) {
            items.add(new TestItem(rnd.nextInt(10000), rnd.nextFloat() * 30.0f, new BigDecimal(String.valueOf(rnd
                .nextFloat()))));
        }

        List<TestDiscount> discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(2L, 0.0D, BigDecimal.TEN));
        TestServiceCharge serviceCharge = new TestServiceCharge(20.0f, null, 10.0D);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, serviceCharge);
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> inversedCart = cart.inverse();

        assEq(-1 * cart.getValue(), inversedCart.getValue());
        assEq(-1 * cart.getDiscountValue(), inversedCart.getDiscountValue());
        assEq(-1 * cart.getActualVat(), inversedCart.getActualVat());
        assertEquals(cart.getItemLines().size(), inversedCart.getItemLines().size());
    }

    @Test
    public void itShouldTreatDiscountSignAndInversesCorrectly() {
        List<TestItem> items;
        List<TestDiscount> discounts;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart;

        //Normal discount and it's inversed cart
        items = new ArrayList<TestItem>();
        items.add(new TestItem(100L, null, new BigDecimal("1.0")));
        discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(5L, 10.0d, BigDecimal.ONE));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assEq(85L, cart.getValue());
        assEq(-85L, cart.inverse().getValue());

        //A negative discount, eg a "topup"
        items = new ArrayList<TestItem>();
        items.add(new TestItem(100L, null, new BigDecimal("1.0")));
        discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(-5L, -10.0d, BigDecimal.ONE));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assEq(115L, cart.getValue());
        assEq(-115L, cart.inverse().getValue());

        //A discount where amount and percentage has different signs
        items = new ArrayList<TestItem>();
        items.add(new TestItem(100L, null, new BigDecimal("1.0")));
        discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(-5L, 10.0d, BigDecimal.ONE));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assEq(95L, cart.getValue());
        assEq(-95L, cart.inverse().getValue());

        //A discount where all (including the quantity) is negative should be identical to a all positive one
        items = new ArrayList<TestItem>();
        items.add(new TestItem(100L, null, new BigDecimal("1.0")));
        discounts = new LinkedList<TestDiscount>();
        discounts.add(new TestDiscount(-5L, -10.0d, BigDecimal.ONE.negate()));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assEq(85L, cart.getValue());
        assEq(-85L, cart.inverse().getValue());
    }

    @Test
    public void itShouldGroupVatsProperly() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(2000L, 10.0f, new BigDecimal("3.0")));
        items.add(new TestItem(3500L, 12.0f, new BigDecimal("4.0")));
        items.add(new TestItem(1200L, 25.0f, BigDecimal.ONE));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);
        SortedMap<Float, VatGroupValues> valuesGroupedByVatPercentage = cart.groupValuesByVatPercentage();
        assEq(545L, valuesGroupedByVatPercentage.get(10.0f).getActualVatValue());
        assEq(1500L, valuesGroupedByVatPercentage.get(12.0f).getActualVatValue());
        assEq(240L, valuesGroupedByVatPercentage.get(25.0f).getActualVatValue());

        assEq(6000L, valuesGroupedByVatPercentage.get(10.0f).getActualValue());
        assEq(14000L, valuesGroupedByVatPercentage.get(12.0f).getActualValue());
        assEq(1200L, valuesGroupedByVatPercentage.get(25.0f).getActualValue());

    }

    @Test
    public void itShouldGroupVatWithDiscount() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(2000L, 10.0f, new BigDecimal("3.0")));
        items.add(new TestItem(3500L, 12.0f, new BigDecimal("4.0")));
        items.add(new TestItem(1200L, 25.0f, BigDecimal.ONE));
        items.add(new TestItem(999999L, 98.0f, new BigDecimal("3.0")));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart1 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);
        Long totVatWithoutDiscount = cart1.getActualVat();
        long totAmountWithoutDiscount = cart1.getValue();
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 1.0d, BigDecimal.ONE));
        discounts.add(new TestDiscount(999999L, null, BigDecimal.ONE));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart2 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        Long totVatWithDiscount = cart2.getActualVat();
        long totAmountWithDiscount = cart2.getValue();
        long discountAmount = totAmountWithoutDiscount - totAmountWithDiscount;
        double discountFrac = ((double) discountAmount) / totAmountWithoutDiscount;
        long totAmountVatWithDiscount = 0;
        SortedMap<Float, VatGroupValues> valuesGroupedByVatPercentage = cart2.groupValuesByVatPercentage();
        for (Map.Entry<Float, VatGroupValues> entry : valuesGroupedByVatPercentage.entrySet()) {
            totAmountVatWithDiscount += entry.getValue().getActualVatValue();
        }
        assEq(totVatWithDiscount, totAmountVatWithDiscount);
        //Verify that the sum of the discounted vats has about the same relation to the original vat
        assertEquals(
            (double) totVatWithoutDiscount - totAmountVatWithDiscount,
            discountFrac * totVatWithoutDiscount,
            0.5d
        );
    }

    @Test
    public void itShouldGroupVatWithDiscountAndServiceCharge() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(2000L, 10.0f, new BigDecimal("3.0")));
        items.add(new TestItem(3500L, 12.0f, new BigDecimal("4.0")));
        items.add(new TestItem(1200L, 25.0f, BigDecimal.ONE));
        items.add(new TestItem(999999L, 98.0f, new BigDecimal("3.0")));
        TestServiceCharge serviceCharge = new TestServiceCharge(20.0f, null, 10.0D);

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart1 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, serviceCharge);
        Long totVatWithoutDiscount = cart1.getActualVat();
        long totAmountWithoutDiscount = cart1.getValue();
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 1.0d, BigDecimal.ONE));
        discounts.add(new TestDiscount(999999L, null, BigDecimal.ONE));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart2 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, serviceCharge);
        Long totVatWithDiscount = cart2.getActualVat();
        long totAmountWithDiscount = cart2.getValue();
        long discountAmount = totAmountWithoutDiscount - totAmountWithDiscount;
        double discountFrac = ((double) discountAmount) / totAmountWithoutDiscount;
        long totAmountVatWithDiscount = 0;
        SortedMap<Float, VatGroupValues> valuesGroupedByVatPercentage = cart2.groupValuesByVatPercentage();
        for (Map.Entry<Float, VatGroupValues> entry : valuesGroupedByVatPercentage.entrySet()) {
            totAmountVatWithDiscount += entry.getValue().getActualVatValue();
        }
        assEq(totVatWithDiscount, totAmountVatWithDiscount);
        //Verify that the sum of the discounted vats has about the same relation to the original vat
        assertEquals(
            (double) totVatWithoutDiscount - totAmountVatWithDiscount,
            discountFrac * totVatWithoutDiscount,
            0.5d
        );
    }

    /**
     * Verifies that, when grouping vats, the sum of the groups is the same as the total. Wrote this check to control
     * rounding errors, so it iterates multiple times, each time with a new "cart"
     */
    @Test
    public void itShouldSumUpAfterDiscount() {
        int nrIterations = 1000;
        for (int iterIdx = 0; iterIdx < nrIterations; iterIdx++) {
            Random rnd = new Random();
            int nrProducts = rnd.nextInt(10) + 1;
            int nrDiscounts = rnd.nextInt(3);
            float[] vatGroups = {3 + rnd.nextInt(3), 6 + rnd.nextInt(3), 9 + rnd.nextInt(3), 12 + rnd.nextInt(3)};
            /*
             * Create products
             */
            List<TestItem> products = new ArrayList<TestItem>();
            for (int i = 0; i < nrProducts; i++) {
                int nrItems = rnd.nextInt(3) + 1;
                products.add(new TestItem(
                    rnd.nextInt(10000) + 100L,
                    vatGroups[rnd.nextInt(vatGroups.length)],
                    new BigDecimal(nrItems)
                ));
            }
            /*
             * Create discounts. As they are randomly generated, they might actually create negative total amounts but
             * this doesn't matter for this check
             */
            List<TestDiscount> discounts = new ArrayList<TestDiscount>();
            for (int i = 0; i < nrDiscounts; i++) {
                discounts.add(new TestDiscount((long) rnd.nextInt(4000), (double) rnd.nextInt(40), BigDecimal.ONE));
            }
            Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
                new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(products, discounts, null);
            SortedMap<Float, VatGroupValues> valuesGroupedByVatPercentage = cart.groupValuesByVatPercentage();
            long totVat = 0;
            for (Map.Entry<Float, VatGroupValues> floatVatGroupValuesEntry : valuesGroupedByVatPercentage.entrySet()) {
                totVat += floatVatGroupValuesEntry.getValue().getActualVatValue();
            }
            assEq(cart.getActualVat(), totVat);
        }
    }

    @Test
    public void totalAmountShouldBeCorrectForAmountDiscounts() {
        Random rnd = new Random();
        long itemPrice = rnd.nextInt(Integer.MAX_VALUE);
        long discountAmount = rnd.nextInt((int) itemPrice);
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(itemPrice, null, BigDecimal.ONE));
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(discountAmount, null, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(itemPrice - discountAmount, cart.getValue());
    }

    @Test
    public void totalAmountShouldBeCorrectForPercentageDiscounts() {
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 99.0d, BigDecimal.ONE));
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(10736439L, null, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(107364, cart.getValue());
    }

    @Test
    public void shouldNotBlowUpIfGrossValueIsZeroAndHaveDiscount() {
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 15.0d, BigDecimal.ONE));
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(0L, null, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(0L, cart.getValue());
    }

    @Test
    public void anEmptyCartIsAlsoACart() {
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(
                Collections.<TestItem>emptyList(),
                null,
                null
            );
        assertThat(cart.getValue()).isEqualTo(0);
        assertThat(cart.getActualVat()).isEqualTo(null);
        assertThat(cart.getGrossValue()).isEqualTo(0);
    }

    @Test
    public void itShouldHandleDiscountsPerItemFixed() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem("First", 100L, 25.0f, BigDecimal.ONE, new TestDiscount(10L, null, BigDecimal.ONE)));
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(90L, cart.getValue());
        assEq(18L, cart.getActualVat());
    }

    @Test
    public void itShouldHandleDiscountsPerItemPercentage() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem("First", 100L, 25.0f, BigDecimal.ONE, new TestDiscount(null, 50.0d, BigDecimal.ONE)));
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(50L, cart.getValue());
        assEq(10L, cart.getActualVat());
    }

    @Test
    public void itShouldHandleDiscountsPerItemPercentageAndFixed() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem("First", 100L, 25.0f, BigDecimal.ONE, new TestDiscount(10L, 50.0d, BigDecimal.ONE)));
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(40L, cart.getValue());
        assEq(8L, cart.getActualVat());
    }

    @Test
    public void itShouldHandleBothGlobalAndPerItemDiscount() {
        List<TestItem> items = new ArrayList<TestItem>();
        //Gives $100 - $1 - 10% -> 100 - 11% = 89, with 1% vat -> $1 VAT
        items.add(new TestItem("First", 100L, 1.0f, BigDecimal.ONE, new TestDiscount(1L, 10.0d, BigDecimal.ONE)));
        //Gives $400 - $4 - 40% -> 400 - 41% = 236, with 50% VAT -> $79 VAT
        items.add(new TestItem(
            "Second", 200L, 50.0f, BigDecimal.valueOf(2),
            new TestDiscount(2L, 20.0d, BigDecimal.valueOf(2))
        ));
        List<TestDiscount> discounts = new ArrayList<TestDiscount>();
        //First verify the non-global discount scenario, as it's already quite complex:
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart1 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(325L, cart1.getValue());
        assEq(80L, cart1.getActualVat());
        //Then verify with a global discount of $10 added:
        discounts.add(new TestDiscount(10L, null, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart2 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(315L, cart2.getValue());
        assEq(77L, cart2.getActualVat());
        ////Then verify with a global discount of 10% added:
        discounts.clear();
        discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE));
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart3 =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(292L, cart3.getValue());
        assEq(72L, cart3.getActualVat());
    }

    @Test
    public void itShouldHandleSurcharge() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(100L, null, BigDecimal.ONE));
        List<TestDiscount> discounts;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart;

        //Percentual
        discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE.negate()));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(110L, cart.getValue());

        //Fixed amount
        discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(1L, null, BigDecimal.ONE.negate()));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(101L, cart.getValue());
    }

    @Test
    public void itShouldHandleConsecutiveSurchargeAndDiscount() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem(100L, null, BigDecimal.ONE));
        List<TestDiscount> discounts;
        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart;

        discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE));
        discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE.negate()));
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        //Reducing the amount by ten and then increasing by 9
        assertEquals(99L, cart.getValue());

        Collections.reverse(discounts);
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        //Increasing by 10 and then reducing by 11 (as long as it's only percentages, the outcome should always be
        //identical, no matter the order of the discounts)
        assertEquals(99L, cart.getValue());

        //Let's also verify that an inversed setup will yield the same amount with opposite sign
        assertEquals(-99L, cart.inverse().getValue());

        //More complex scenario, mixing in fixed amounts also:
        discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE)); //->90
        discounts.add(new TestDiscount(20L, null, BigDecimal.ONE.negate())); //->110
        discounts.add(new TestDiscount(null, 10.0d, BigDecimal.ONE.negate())); //->121
        discounts.add(new TestDiscount(20L, null, BigDecimal.valueOf(2))); //->81
        cart = new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);
        assertEquals(81L, cart.getValue());
        assertEquals(-81L, cart.inverse().getValue());
    }

    @Test
    public void testTotalDiscount() {
        ArrayList<TestItem> items = new ArrayList<TestItem>();
        items.add(new TestItem("", 100L, 10.0f, new BigDecimal(2), new TestDiscount(100L, null, BigDecimal.ONE)));

        ArrayList<TestDiscount> discounts = new ArrayList<TestDiscount>();
        discounts.add(new TestDiscount(null, 50.0d, BigDecimal.ONE));

        Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart =
            new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, discounts, null);

        assertThat(cart.getDiscountValue()).isEqualTo(150);
        assertThat(cart.getNumberOfDiscounts()).isEqualTo(2);
        assertThat(cart.getDiscountVat()).isEqualTo(13L);
    }

    @Test
    public void itShouldThrowWhenDiscountsInvalid() {
        ArrayList<TestDiscount> discounts = new ArrayList<TestDiscount>();
        BigDecimal quantity = null;
        discounts.add(new TestDiscount(null, 50.0d, quantity));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Discount cannot have null quantity: TestDiscount{ amount = null, percentage = 50.0, "
            + "quantity = null}");
        new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(null, discounts, null);
    }

    @Test
    public void itShouldThrowWhenItemsInvalid() {
        ArrayList<TestItem> items = new ArrayList<TestItem>();
        BigDecimal quantity = null;
        items.add(new TestItem("", 100L, 10.0f, quantity, new TestDiscount(100L, null, BigDecimal.ONE)));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
            "Item cannot have null quantity: TestItem{ unitPrice = 100, vatPercentage = 10.0, quantity = null, name ="
                + " , discount = TestDiscount{ amount = 100, percentage = null, quantity = 1}}");
        new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(items, null, null);
    }

    //Dummy method for bypassing ambiguity against two similar Assert.assertEqual methods
    private void assEq(Long one, Long two) {
        assertEquals(one, two);
    }
}
