package com.izettle.cart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CartTest {

	private static class TestItem implements Item {

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
	}

	private static class TestDiscount implements Discount {

		private final Long amount;
		private final Double percentage;

		TestDiscount(Long amount, Double percentage) {
			this.amount = amount;
			this.percentage = percentage;
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
	}

	@Test
	public void itShouldCalculateCorrectVatAndEffectivePrice() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, 25D, new BigDecimal("1.0")));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();
		discounts.put(new TestDiscount(0L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getTotalEffectivePrice());
		assertEquals(0D, cart.getTotalEffectiveDiscountPercentage(), 0D);
		assertEquals(260L, cart.getTotalEffectiveVat().longValue());
	}

	@Test
	public void itShouldHandleNullVatProperly() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, null, new BigDecimal("1.0")));
		Map<TestDiscount, BigDecimal> discounts = null;
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getTotalEffectivePrice());
		assertNull(cart.getTotalEffectiveDiscountPercentage());
		assertNull(cart.getTotalEffectiveVat());
	}

	@Test
	public void itShouldHandleFixedDiscounts() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(1299L, 25d, new BigDecimal("1.0")));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();
		discounts.put(new TestDiscount(10L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1289L, cart.getTotalEffectivePrice());
		assertEquals(258L, cart.getTotalEffectiveVat().longValue());
		assertEquals(0.77d, cart.getTotalEffectiveDiscountPercentage(), 0.001d);
	}

	@Test
	public void itShouldDistributeVats() {
		List<TestItem> items = new LinkedList<TestItem>();
		items.add(new TestItem(500L, 10d, new BigDecimal("2.0")));
		items.add(new TestItem(100L, 50d, new BigDecimal("1.0")));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();

		Cart<TestItem, TestDiscount> cart1 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1100L, cart1.getTotalEffectivePrice());
		assertEquals(1100L, cart1.getTotalGrossAmount());

		discounts.put(new TestDiscount(110L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart2 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(10d, cart2.getTotalEffectiveDiscountPercentage().doubleValue(), 0.01d);
		assertEquals(990L, cart2.getTotalEffectivePrice());
		assertEquals(1100L, cart2.getTotalGrossAmount());

		List<ItemLine<TestItem>> lineItems = cart2.getItemLines();
		assertEquals(2, lineItems.size());
		ItemLine<TestItem> lineItem = lineItems.get(0);
		assertEquals(82L, lineItem.getEffectiveVat().longValue());
		lineItem = lineItems.get(1);
		assertEquals(30L, lineItem.getEffectiveVat().longValue());

		assertEquals(112L, cart2.getTotalEffectiveVat().longValue());
	}

	@Test
	public void itShouldReclaimDiscountsAfterRoundingError() {
		List<TestItem> items = new ArrayList<CartTest.TestItem>();
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(32L, 50d, new BigDecimal("1.0")));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();
		//one fixed discount of 8
		discounts.put(new TestDiscount(8L, 0D), BigDecimal.ONE);

		System.out.println("Reclaiming");
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
	}

	@Test
	public void itShouldDistributeMoreAfterRoundingError() {
		List<TestItem> items = new ArrayList<CartTest.TestItem>();
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(33L, 10d, new BigDecimal("1.0")));
		items.add(new TestItem(32L, 50d, new BigDecimal("1.0")));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();
		//one fixed discount of 10
		discounts.put(new TestDiscount(10L, 0D), BigDecimal.ONE);

		System.out.println("Disctribute more");
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
	}
}
