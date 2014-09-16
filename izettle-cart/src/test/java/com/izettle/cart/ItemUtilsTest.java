package com.izettle.cart;

import static org.fest.assertions.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;

public class ItemUtilsTest {

	@Test
	public void testDiscountValue() {

		assertThat(
			ItemUtils.getDiscountValue(
				new TestItem(
					"name",
					1000L,
					null,
					BigDecimal.ONE,
					new TestDiscount(50L, null, BigDecimal.ONE)
				)
			)
		).isEqualTo(50L);

		assertThat(
			ItemUtils.getDiscountValue(
				new TestItem(
					"name",
					1000L,
					null,
					BigDecimal.ONE,
					new TestDiscount(null, 50d, BigDecimal.ONE)
				)
			)
		).isEqualTo(500L);

		assertThat(
			ItemUtils.getDiscountValue(
				new TestItem(
					"name",
					1000L,
					null,
					BigDecimal.ONE,
					null
				)
			)
		).isNull();
	}
}
