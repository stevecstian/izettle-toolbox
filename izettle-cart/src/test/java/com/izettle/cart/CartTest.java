package com.izettle.cart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CartTest {

	public CartTest() {
	}

	private static class TestItem implements Item {

		private final Long pricePerQuantity;
		private final Double vatPercentage;

		TestItem(Long pricePerQuantity, Double vatPercentage) {
			this.pricePerQuantity = pricePerQuantity;
			this.vatPercentage = vatPercentage;
		}

		@Override
		public Long getPricePerQuantity() {
			return pricePerQuantity;
		}

		@Override
		public Double getVatPercentage() {
			return vatPercentage;
		}

		@Override
		public String toString() {
			return "TestItem{" + ", pricePerQuantity=" + pricePerQuantity + ", vatPercentage=" + vatPercentage + '}';
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
		Map<TestItem, BigDecimal> items = new LinkedHashMap<TestItem, BigDecimal>();
		items.put(new TestItem(1299L, 25D), new BigDecimal("1.0"));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();
		discounts.put(new TestDiscount(0L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getTotalEffectivePrice());
		assertEquals(0D, cart.getTotalEffectiveDiscountPercentage(), 0D);
		assertEquals(260L, cart.getTotalEffectiveVat().longValue());
	}

	@Test
	public void itShouldHandleNullVatProperly() {
		Map<TestItem, BigDecimal> items = new LinkedHashMap<TestItem, BigDecimal>();
		items.put(new TestItem(1299L, null), new BigDecimal("1.0"));
		Map<TestDiscount, BigDecimal> discounts = null;
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1299L, cart.getTotalEffectivePrice());
		assertNull(cart.getTotalEffectiveDiscountPercentage());
		assertNull(cart.getTotalEffectiveVat());
	}

	@Test
	public void itShouldHandleFixedDiscounts() {
		Map<TestItem, BigDecimal> items = new LinkedHashMap<TestItem, BigDecimal>();
		items.put(new TestItem(1299L, 25d), new BigDecimal("1.0"));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();
		discounts.put(new TestDiscount(10L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1289L, cart.getTotalEffectivePrice());
		assertEquals(258L, cart.getTotalEffectiveVat().longValue());
		assertEquals(0.77d, cart.getTotalEffectiveDiscountPercentage(), 0.001d);
	}

	@Test
	public void itShouldDistributeVats() {
		Map<TestItem, BigDecimal> items = new LinkedHashMap<TestItem, BigDecimal>();
		items.put(new TestItem(500L, 10d), new BigDecimal("2.0"));
		items.put(new TestItem(100L, 50d), new BigDecimal("1.0"));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();

		Cart<TestItem, TestDiscount> cart1 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1100L, cart1.getTotalEffectivePrice());
		assertEquals(1100L, cart1.getTotalGrossPrice());

		discounts.put(new TestDiscount(110L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart2 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(10d, cart2.getTotalEffectiveDiscountPercentage().doubleValue(), 0.01d);
		assertEquals(990L, cart2.getTotalEffectivePrice());
		assertEquals(1100L, cart2.getTotalGrossPrice());

		List<ItemLine<TestItem>> lineItems = cart2.getItemLines();
		assertEquals(2, lineItems.size());
		ItemLine<TestItem> lineItem = lineItems.get(0);
		assertEquals(82L, lineItem.getEffectiveVat().longValue());
		lineItem = lineItems.get(1);
		assertEquals(30L, lineItem.getEffectiveVat().longValue());

		assertEquals(112L, cart2.getTotalEffectiveVat().longValue());
	}

	@Test
	public void itShouldDistributeDiscounts() {
		Map<TestItem, BigDecimal> items = new LinkedHashMap<TestItem, BigDecimal>();
		items.put(new TestItem(500L, 10d), new BigDecimal("2.0"));
		items.put(new TestItem(100L, 50d), new BigDecimal("1.0"));
		Map<TestDiscount, BigDecimal> discounts = new LinkedHashMap<TestDiscount, BigDecimal>();

		Cart<TestItem, TestDiscount> cart1 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(1100L, cart1.getTotalEffectivePrice());
		assertEquals(1100L, cart1.getTotalGrossPrice());

		discounts.put(new TestDiscount(110L, 0D), BigDecimal.ONE);
		Cart<TestItem, TestDiscount> cart2 = new Cart<TestItem, TestDiscount>(items, discounts);
		assertEquals(10d, cart2.getTotalEffectiveDiscountPercentage().doubleValue(), 0.01d);
		assertEquals(990L, cart2.getTotalEffectivePrice());
		assertEquals(1100L, cart2.getTotalGrossPrice());

		//Verify that 10% is deducted for each line item
		List<ItemLine<TestItem>> lineItems = cart2.getItemLines();
		assertEquals(2, lineItems.size());
		ItemLine<TestItem> lineItem = lineItems.get(0);
		assertEquals(1000L, lineItem.getGrossPrice());
		assertEquals(900L, lineItem.getEffectivePrice());
		lineItem = lineItems.get(1);
		assertEquals(100L, lineItem.getGrossPrice());
		assertEquals(90L, lineItem.getEffectivePrice());

		//Verify that the actual total price reduction is equal to the fixed discount
		assertEquals(110L, cart2.getTotalGrossPrice() - cart2.getTotalEffectivePrice());

	}
}
