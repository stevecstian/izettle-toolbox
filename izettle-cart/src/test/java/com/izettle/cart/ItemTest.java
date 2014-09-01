package com.izettle.cart;

import static org.fest.assertions.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;

public class ItemTest {

	@Test
	public void testItemVat() {
		assertThat(ItemUtils.grossVatValue(new TestItem(BigDecimal.ONE, 100, 25f))).isEqualTo(20);
		assertThat(ItemUtils.grossVatValue(new TestItem(BigDecimal.ONE, 100, null))).isEqualTo(null);
	}

	private class TestItem implements Item<TestItem> {

		private final BigDecimal quantity;
		private final long unitPrice;
		private final Float vatPercentage;

		private TestItem(BigDecimal quantity, long unitPrice, Float vatPercentage) {
			this.quantity = quantity;
			this.unitPrice = unitPrice;
			this.vatPercentage = vatPercentage;
		}

		@Override
		public BigDecimal getQuantity() {
			return quantity;
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
		public TestItem inverse() {
			return new TestItem(quantity.negate(), unitPrice, vatPercentage);
		}
	}
}
