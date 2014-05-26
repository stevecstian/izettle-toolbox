package com.izettle.cart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class CartTest {

	private static class TestItem implements Item<TestItem> {

		private final long unitPrice;
		private final Double vatPercentage;
		private final BigDecimal quantity;

		TestItem(long unitPrice, Double vatPercentage, BigDecimal quantity) {
			this.unitPrice = unitPrice;
			this.vatPercentage = vatPercentage;
			this.quantity = quantity;
		}

		@Override
		public long getUnitPrice() {
			return unitPrice;
		}

		@Override
		public Double getVatPercentage() {
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
		items.add(new TestItem(1299L, 25D, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		discounts.add(new TestDiscount(0L, 0D, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getTotalEffectivePrice());
		assertEquals(0D, cart.getTotalEffectiveDiscountPercentage(), 0D);
		assEq(260L, cart.getTotalEffectiveVat());
	}

	@Test
	public void itShouldHandleNullVatProperly() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, null, new BigDecimal("1.0")));
		List<TestDiscount> discounts = null;
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getTotalEffectivePrice());
		assertNull(cart.getTotalEffectiveDiscountPercentage());
		assertNull(cart.getTotalEffectiveVat());
	}

	@Test
	public void itShouldHandleFixedDiscounts() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, 25d, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		discounts.add(new TestDiscount(10L, 0D, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1289L, cart.getTotalEffectivePrice());
		assEq(258L, cart.getTotalEffectiveVat());
		assertEquals(0.77d, cart.getTotalEffectiveDiscountPercentage(), 0.001d);
	}

	@Test
	public void itShouldDistributeVats() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(500L, 10d, new BigDecimal("2.0")));
		items.add(new TestItem(100L, 50d, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();

		Cart<TestItem, TestDiscount> cart1 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1100L, cart1.getTotalEffectivePrice());
		assertEquals(1100L, cart1.getTotalGrossAmount());

		discounts.add(new TestDiscount(110L, 0D, BigDecimal.ONE));
		Cart<TestItem, TestDiscount> cart2 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(10d, cart2.getTotalEffectiveDiscountPercentage(), 0.01d);
		assEq(990L, cart2.getTotalEffectivePrice());
		assEq(1100L, cart2.getTotalGrossAmount());

		List<ItemLine<TestItem>> lineItems = cart2.getItemLines();
		assertEquals(2, lineItems.size());
		ItemLine<TestItem> lineItem = lineItems.get(0);
		assEq(82L, lineItem.getEffectiveVat());
		lineItem = lineItems.get(1);
		assEq(30L, lineItem.getEffectiveVat());
		assEq(112L, cart2.getTotalEffectiveVat());
	}

	@Test
	public void itShouldReclaimDiscountsAfterRoundingError() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(32L, 50d, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		//one fixed discount of 8
		discounts.add(new TestDiscount(8L, 0D, BigDecimal.ONE));

		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assEq(8L, cart.getTotalDiscountAmount());
		assEq(33L + 33L + 32L - 8L, cart.getTotalEffectivePrice());
	}

	@Test
	public void itShouldDistributeMoreAfterRoundingError() {
		List<TestItem> items = new ArrayList<TestItem>();
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(32L, 50d, new BigDecimal("1.0")));
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		//one fixed discount of 10
		discounts.add(new TestDiscount(10L, 0D, BigDecimal.ONE));

		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assEq(10L, cart.getTotalDiscountAmount());
		assEq(33L + 33L + 32L - 10L, cart.getTotalEffectivePrice());
	}

	@Test
	public void itShouldCreateAProperInverse() {
		List<TestItem> items = new ArrayList<TestItem>();
		Random rnd = new Random();
		int iter = rnd.nextInt(100);
		for (int i = 0; i < iter; i++) {
			items.add(new TestItem(rnd.nextInt(10000), rnd.nextDouble() * 30d, new BigDecimal("" + rnd.nextFloat())));
		}
		List<TestDiscount> discounts = new LinkedList<TestDiscount>();
		discounts.add(new TestDiscount(2L, 0D, BigDecimal.TEN));
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		Cart<TestItem, TestDiscount> inversedCart = cart.inverse();
		assEq(-1 * cart.getTotalEffectivePrice(), inversedCart.getTotalEffectivePrice());
		assEq(-1 * cart.getTotalDiscountAmount(), inversedCart.getTotalDiscountAmount());
		assEq(-1 * cart.getTotalEffectiveVat(), inversedCart.getTotalEffectiveVat());
		assertEquals(cart.getItemLines().size(), inversedCart.getItemLines().size());
	}

	//Dummy method for bypassing ambiguity against two similar Assert.assertEqual methods
	private void assEq(Long one, Long two) {
		assertEquals(one, two);
	}
}
