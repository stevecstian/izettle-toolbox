package com.izettle.cart;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class RefundTest {

    @Test
    public void itShouldNotAcceptInvalidItems() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAcceptItemsThatsNotInOriginalCart() {

        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID id3 = UUID.randomUUID();

        final TestItem item1 = createItem(id1, 10L, null, BigDecimal.ONE);
        final TestItem item2 = createItem(id2, 10L, null, BigDecimal.ONE);
        final TestItem item3 = createItem(id3, 10L, null, BigDecimal.ONE);

        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(item1, item2);

        originalCart.refund(null, singletonList(item3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAcceptItemsWithGreaterQuantityThanLeftInOriginalCart() {
        final UUID id = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id, 10L, null, BigDecimal.TEN)
        );
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> alreadyRefundedCart = createCart(
            createItem(id, 10L, null, BigDecimal.valueOf(-9L))
        );

        final List<TestItem> itemsToRefund = new LinkedList<TestItem>();
        itemsToRefund.add(createItem(id, 10L, null, BigDecimal.valueOf(-2L)));

        originalCart.refund(singletonList(alreadyRefundedCart), itemsToRefund);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAcceptItemsWithChangedPrice() {
        final UUID id = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id, 10L, null, BigDecimal.ONE)
        );
        final List<TestItem> itemsToRefund = new LinkedList<TestItem>();
        itemsToRefund.add(createItem(id, 11L, null, BigDecimal.ONE));

        originalCart.refund(null, itemsToRefund);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAcceptItemsWithChangedVat() {
        final UUID id = UUID.randomUUID();
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> originalCart = createCart(
            createItem(id, 10L, 31f, BigDecimal.TEN)
        );
        final Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> alreadyRefundedCart = createCart(
            createItem(id, 10L, 30f, BigDecimal.ONE)
        );
        final List<TestItem> itemsToRefund = new LinkedList<TestItem>();
        itemsToRefund.add(createItem(id, 10L, 31f, BigDecimal.ONE));

        originalCart.refund(singletonList(alreadyRefundedCart), itemsToRefund);
    }

    @Test
    public void itShouldUseTheLowerDistributedItemValue() {
    }

    @Test
    public void itShouldUseTheHigherDistributedItemValue() {
    }

    @Test
    public void itShouldAddUpMultipleRefundsToOriginalAmount() {
    }

    private TestItem createItem(
        final UUID id,
        final long unitPrice,
        final Float vatPercentage,
        final BigDecimal quantity
    ) {
        return new TestItem(id, "", unitPrice, vatPercentage, quantity, null);
    }

    private Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> createCart(TestItem... items) {
        final List<TestItem> itemList = Arrays.asList(items);
        return new Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge>(itemList, null, null);
    }

}
