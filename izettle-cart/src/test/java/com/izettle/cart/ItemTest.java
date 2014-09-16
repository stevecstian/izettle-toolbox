package com.izettle.cart;

import static org.fest.assertions.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;

public class ItemTest {

	@Test
	public void testDiscountValue() {

		assertThat(
			new TestItem(
				"name",
				1000L,
				null,
				BigDecimal.ONE,
				new TestDiscount(50L, null, BigDecimal.ONE)
			).getDiscountValue()
		).isEqualTo(50L);

		assertThat(
			new TestItem(
				"name",
				1000L,
				null,
				BigDecimal.ONE,
				new TestDiscount(null, 50d, BigDecimal.ONE)
			).getDiscountValue()
		).isEqualTo(500L);

		assertThat(
			new TestItem(
				"name",
				1000L,
				null,
				BigDecimal.ONE,
				null
			).getDiscountValue()
		).isNull();
	}
}
