package com.izettle.cart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import org.junit.Test;

public class CartTest {

	private static class TestItem implements Item<TestItem> {

		private final long unitPrice;
		private final Float vatPercentage;
		private final BigDecimal quantity;

		TestItem(long unitPrice, Float vatPercentage, BigDecimal quantity) {
			this.unitPrice = unitPrice;
			this.vatPercentage = vatPercentage;
			this.quantity = quantity;
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
		public String toString() {
			return "TestItem{" + ", unitPrice=" + unitPrice + ", vatPercentage=" + vatPercentage + '}';
		}

		@Override
		public BigDecimal getQuantity() {
			return this.quantity;
		}

		@Override
		public TestItem inverse() {
			return new TestItem(unitPrice, vatPercentage, quantity.negate());
		}
	}

	private static class TestDiscount implements Discount<TestDiscount> {

		private final Long amount;
		private final Double percentage;
		private final BigDecimal quantity;

		TestDiscount(Long amount, Double percentage, BigDecimal quantity) {
			this.amount = amount;
			this.percentage = percentage;
			this.quantity = quantity;
		}

		@Override
		public Long getAmount() {
			return amount;
		}

		@Override
		public Double getPercentage() {
			return percentage;
		}

		@Override
		public String toString() {
			return "TestDiscount{" + "amount=" + amount + ", percentage=" + percentage + '}';
		}

		@Override
		public BigDecimal getQuantity() {
			return this.quantity;
		}

		@Override
		public TestDiscount inverse() {
			return new TestDiscount(amount, percentage, quantity.negate());
		}
	}

	@Test
	public void itShouldCalculateCorrectVatAndEffectivePrice() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, 25f, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		discounts.add(new TestDiscount(0L, 0D, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getEffectivePrice());
		assertEquals(0D, cart.getEffectiveDiscountPercentage(), 0D);
		assEq(260L, cart.getEffectiveVat());
	}

	@Test
	public void itShouldHandleNullVatProperly() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, null, new BigDecimal("1.0")));
		List<TestDiscount> discounts = null;
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getEffectivePrice());
		assertNull(cart.getEffectiveDiscountPercentage());
		assertNull(cart.getEffectiveVat());
	}

	@Test
	public void itShouldHandleFixedDiscounts() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, 25f, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		discounts.add(new TestDiscount(10L, 0D, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1289L, cart.getEffectivePrice());
		assEq(258L, cart.getEffectiveVat());
		assertEquals(0.77d, cart.getEffectiveDiscountPercentage(), 0.001d);
	}

	@Test
	public void itShouldDistributeVats() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(500L, 10f, new BigDecimal("2.0")));
		items.add(new TestItem(100L, 50f, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();

		Cart<TestItem, TestDiscount> cart1 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1100L, cart1.getEffectivePrice());
		assertEquals(1100L, cart1.getGrossAmount());

		discounts.add(new TestDiscount(110L, 0D, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart2 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(10d, cart2.getEffectiveDiscountPercentage(), 0.01d);
		assEq(990L, cart2.getEffectivePrice());
		assEq(1100L, cart2.getGrossAmount());

		List<ItemLine<TestItem>> lineItems = cart2.getItemLines();
		assertEquals(2, lineItems.size());
		ItemLine<TestItem> lineItem = lineItems.get(0);
		assEq(82L, lineItem.getEffectiveVat());
		lineItem = lineItems.get(1);
		assEq(30L, lineItem.getEffectiveVat());
		assEq(112L, cart2.getEffectiveVat());
	}

	@Test
	public void itShouldReclaimDiscountsAfterRoundingError() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(33L, 10f, new BigDecimal("1.0")));
		items.add(new TestItem(33L, 10f, new BigDecimal("1.0")));
		items.add(new TestItem(32L, 50f, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		//one fixed discount of 8
		discounts.add(new TestDiscount(8L, 0D, BigDecimal.ONE));

		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assEq(8L, cart.getDiscountAmount());
		assEq(33L + 33L + 32L - 8L, cart.getEffectivePrice());
	}

	@Test
	public void itShouldDistributeMoreAfterRoundingError() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(33L, 10f, new BigDecimal("1.0")));
		items.add(new TestItem(33L, 10f, new BigDecimal("1.0")));
		items.add(new TestItem(32L, 50f, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		//one fixed discount of 10
		discounts.add(new TestDiscount(10L, 0D, BigDecimal.ONE));

		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assEq(10L, cart.getDiscountAmount());
		assEq(33L + 33L + 32L - 10L, cart.getEffectivePrice());
	}

	@Test
	public void itShouldDistributeDiscountAmountOverDiscounts() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(95L, null, BigDecimal.ONE));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		/*
		 Adding 9 discounts with 10% each will give a rounding error on each discount line (10% of 95 is 9.5 ->  9L)
		 A naive implementation would then take these 9 for each discount, summarizing up to 81 in total discount, which
		 would be suprising result, given that you'd expect ~90% discount
		 */
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		discounts.add(new TestDiscount(null, 10d, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		//Verify that totals add up, and that we're as close as possible to a total discount of 90%
		assEq(9L, cart.getEffectivePrice());
		assEq(86L, cart.getDiscountAmount());
		assertEquals(90d, cart.getEffectiveDiscountPercentage(), 1d);
		//Verify that the sum of all discount items discount amount equals the total discount:
		long totDiscountAmnt = 0L;
		for (DiscountLine<TestDiscount> discountLine : cart.getDiscountLines()) {
			totDiscountAmnt += discountLine.getEffectiveDiscountAmount();
		}
		assEq(cart.getDiscountAmount(), totDiscountAmnt);
	}

	@Test
	public void itShouldCreateAProperInverse() {
		List<TestItem> items = new ArrayList<TestItem>();
		Random rnd = new Random();
		int iter = rnd.nextInt(100);
		for (int i = 0; i < iter; i++) {
			items.add(new TestItem(rnd.nextInt(10000), rnd.nextFloat() * 30f, new BigDecimal("" + rnd.nextFloat())));
		}
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		discounts.add(new TestDiscount(2L, 0D, BigDecimal.TEN));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		Cart<TestItem, TestDiscount> inversedCart = cart.inverse();
		assEq(-1 * cart.getEffectivePrice(), inversedCart.getEffectivePrice());
		assEq(-1 * cart.getDiscountAmount(), inversedCart.getDiscountAmount());
		assEq(-1 * cart.getEffectiveVat(), inversedCart.getEffectiveVat());
		assertEquals(cart.getItemLines().size(), inversedCart.getItemLines().size());
	}

	public void itShouldGroupVatsProperly() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(2000l, 10f, new BigDecimal(3d)));
		items.add(new TestItem(3500l, 12f, new BigDecimal(4d)));
		items.add(new TestItem(1200l, 25f, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, null);
		SortedMap<Float, Long> groupedVatAmounts = cart.groupEffectiveVatAmounts();
		assEq(545L, groupedVatAmounts.get(10f));
		assEq(1500L, groupedVatAmounts.get(12f));
		assEq(240L, groupedVatAmounts.get(25f));
	}

	@Test
	public void itShouldGroupVatWithDiscount() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(2000l, 10f, new BigDecimal(3d)));
		items.add(new TestItem(3500l, 12f, new BigDecimal(4d)));
		items.add(new TestItem(1200l, 25f, BigDecimal.ONE));
		items.add(new TestItem(999999l, 98f, new BigDecimal(3d)));
		Cart<TestItem, TestDiscount> cart1 = new Cart<TestItem, TestDiscount>(items, null);
		Long totVatWithoutDiscount = cart1.getEffectiveVat();
		long totAmountWithoutDiscount = cart1.getEffectivePrice();
		List<TestDiscount> discounts = new ArrayList<TestDiscount>();
		discounts.add(new TestDiscount(null, 1d, BigDecimal.ONE));
		discounts.add(new TestDiscount(999999l, null, BigDecimal.ONE));

		Cart<TestItem, TestDiscount> cart2 = new Cart<TestItem, TestDiscount>(items, discounts);
		Long totVatWithDiscount = cart2.getEffectiveVat();
		long totAmountWithDiscount = cart2.getEffectivePrice();
		long discountAmount = totAmountWithoutDiscount - totAmountWithDiscount;
		double discountFrac = ((double) discountAmount) / totAmountWithoutDiscount;
		long totAmountVatWithDiscount = 0;
		SortedMap<Float, Long> groupedVatAmounts = cart2.groupEffectiveVatAmounts();
		for (Map.Entry<Float, Long> entry : groupedVatAmounts.entrySet()) {
			totAmountVatWithDiscount += entry.getValue();
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
			Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(products, discounts);
			SortedMap<Float, Long> groupedVatAmounts = cart.groupEffectiveVatAmounts();
			long totVat = 0;
			for (Float key : groupedVatAmounts.keySet()) {
				totVat += groupedVatAmounts.get(key);
			}
			assEq(cart.getEffectiveVat(), totVat);
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
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(itemPrice - discountAmount, cart.getEffectivePrice());
	}

	@Test
	public void totalAmountShouldBeCorrectForPercentageDiscounts() {
		List<TestDiscount> discounts = new ArrayList<TestDiscount>();
		discounts.add(new TestDiscount(null, 99d, BigDecimal.ONE));
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(10736439L, null, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(107364, cart.getEffectivePrice());
	}

	//Dummy method for bypassing ambiguity against two similar Assert.assertEqual methods
	private void assEq(Long one, Long two) {
		assertEquals(one, two);
	}
}
