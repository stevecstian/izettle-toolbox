# iZettle Cart

This set of utility classes aims at helping out doing generic calculations on a (shopping) cart.

## Why is this needed?
At a point we started having lot's of different implementation for doing this logic in different places. While not very complicated, it's easy to go wrong, and introduce small differences between implementations. Problems such as rounding and distribution of vat amounts when applying discounts have been seen.
Also, the source level is intentionally kept at java 6 to allow android apps to use the library. In this way, both apps and backend services can apply the same logic for handling carts.

## What is this?
This set of classes intends to help out only in doing calculations on the cart itself as an abstract concept. It handles no other business properties such as shipment, fees etc. As the calculations are purely numerical, there is no need to include information about actual currency in this scope. The general concept is that a Cart is cheap to build, and should be built (and directly thrown away) whenever one needs financial data from a collection of items and discounts. For example a complex entity containing lots of other stuff needn't necessarily itself represent a Cart, but could for example in a hypothetical method `getPrice()` just temporarily build a Cart and then ask that object for it's value.

## Basic types used:
### Money
Represented as whole numbers (`long` and `Long`), using the (unknown) currency's minimal denominator. E.g. $10 would be represented as 1000 (cents). This is to make sure that no fractional values are used, and that all necessary rounding takes place on each line.
### Quantities
Represented as `BigDecimal` to allow for arbitrarily precise values, e.g. 0.000123 kg of saffron. Quantity is also the field indicating direction of the purchase: a refund cart would have negative quantities on most lines.
### VAT Percentage
In most countries, valid VAT percentages are expressed in whole numbers, but as always, there are exceptions. Therefore, VAT percentages are expressed as a floating point number, with `Float`. nullable for scenarios when VAT is not applicable. The constructed cart object has methods for getting different versions of the VAT outcome, for example `groupValuesByVatPercentage`, where all the different percentages are mapped against their values (commonly displayed on receipts).
### Primitive vs Object
The distinction between `Float` and `float` has been made to indicate when a value is nullable or not. Same goes for `Long` vs `long`.

## Classes and interfaces
### `Item`, `Discount` and `ServiceCharge`
These are the interfaces that should be implemented by a user of the library, providing getters for accessing `unitPrice`, `quantity`, `vatPercentage` etc. An Item can also contain an optional local discount that will be applied to only the item itself, as opposed to the cart-wide discount that will affect the entire carts value. Multiple cart-wide discounts is possible: they will then be calculated as if applied on the gross value of the cart in consecutive order. E.g. two 10% discounts will result in a total discount of 19%. Also noteworthy is that discounts can be negative, and would then represent a value addition/a 'top up'. A ServiceCharge is something that's applied as the last calculation when the value of the rest of the cart is known. This may be used for representing a top up or a mandatory tip, used in some countries.
### `Cart`
The cart is constructed as an immutable object, where the collections of items and discounts are given at construction time. After construction, the cart holds information on a per line level, and a total level. Each line is represented by a `ItemLine` or a `DiscountLine`. The object is queryable for properties such as grossValue, value, groupedVatAmounts etc. Also, the cart can be cloned into it's negative counterpart by calling `Cart.inverse()`, which is useful for example when creating a cart that is representing a full refund of the original.
### `ItemLine`, `DiscountLine` and `ServiceChargeLine`
These are the objects that will populate the cart's three different list sections. Each item, discount and service charge line will hold information such as actual value and actual VAT, but also keeping a reference to it's original item/discount/servicecharge.

## Example
```java
    long unitPrice;
    Float vat;
    BigDecimal quantity;
    List<TestItem> items = new LinkedList<>();

    unitPrice = 500L;
    vat = 10f;
    quantity = new BigDecimal("2.0");
    items.add(new TestItem(unitPrice, vat, quantity));

    unitPrice = 50L;
    vat = 50f;
    quantity = BigDecimal.ONE;
    items.add(new TestItem(unitPrice, vat, quantity));

    List<TestDiscount> discounts = new LinkedList<>();
    Long discountAmount = 110L;
    Double discountPercentage = null;
    quantity = BigDecimal.ONE;
    discounts.add(new TestDiscount(discountAmount, discountPercentage, quantity));

    TestServiceCharge serviceCharge = new TestServiceCharge(0F, 20L, null);

    Cart<TestItem, TestDiscount, TestDiscount, TestServiceCharge> cart = new Cart<>(items, discounts, serviceCharge);
    System.out.println(cart);
```
outputs:
```
Cart {
    ItemLines:
        ItemLine { item = TestItem{ unitPrice = 500, vatPercentage = 10.0, quantity = 2.0, name = null, discount = null}, grossValue = 1000, grossVat = 91, actualValue = 895, actualVat = 81}
        ItemLine { item = TestItem{ unitPrice = 50, vatPercentage = 50.0, quantity = 1, name = null, discount = null}, grossValue = 50, grossVat = 17, actualValue = 45, actualVat = 15}
    DiscountLines:
        DiscountLine { discount = TestDiscount{ amount = 110, percentage = null, quantity = 1}, actualPercentage = 10.476190476190476, value = 110}
    Gross Amounts:
        Gross Value: 1050
        Gross VAT: 108
    Actual Amounts:
        Value: 960
        Discount Value: 110
        Discount Percentage: 10.476190476190476
        Service Charge Value: 20
        VAT: 96
}
```

## Full refunds
When a full cart is refunded, there is no complex maths going on. The easiest
way to create a new cart, representing the refund itself is by simply calling
`Cart::inverse`: this will generate a cart with all quantities negated,
effectively generating a cart with the opposite value. It's guaranteed to have
the exact same (albeit negated) value as the original cart.

## Partial alterations (refunds or additions)
Partial refunds is a bit more complex than a full refund. Why so? Consider the
scenario when 10 units of item A with a price of 1 has been sold in the original
cart. Also, the original cart had a 50% discount, effectively making the total
value of the cart equal to 5. If each refunded unit of A would be calculated as
a separate cart, that would result in the customer getting 0 funds back each
time. The only way to address this is to *always* take the original cart (and
its possible previous refunds) into consideration, apply the sequence of
reductions (alterations) to it and calculate the residual value. The amount of
funds that's supposed to be paid back is the difference between the original
value and the residual value. To aid handling this logic, there are two public
methods on the `Cart` object: `Cart::getRemainingItems` and
`Cart:createAlterationCart`. The first is used to get a list of items (and their
quantities) remaining in the cart (think of a UI where the user by this can get
a list of returnable items). The second method is used to create a 'cart-like'
object `AlterationCart`, that can be queried for details such as value, vat,
discount etc.

Note one: Discounts are treated as distributed over all items, and will be to
the disadvantage for the customer when calculating the value for the refund (an
alteration with negative quantities). This applies no matter if the discount is
based on a percentage or on a fixed amount.

Note two: An alteration can have negative quantities, and would then represent a
refund event. The resulting `AlterationCart` will then also represent negative
amounts (as something is removed from the original cart). If an alteration is
positive, however, it will represent an addition to the original cart and
consequently have positive values.

### Example 1:
Item A has a price of 10
Original cart has quantity 2 of item A and a 50% cart-wide discount.
Customer returns one unit of item A
The value of the AlterationCart here is -5

### Example 2:
Item A has a price of 10
Original cart has quantity 2 of item A and a cart-wide discount of fixed value 4.
Customer returns one unit of item A
The value of the AlterationCart here is -8

## Todos
* Possibility to treat a cart as an item, effectively opening up for adding one
  cart into another.
* Consider a way for the user of the library to specify whether prices are
  including or excluding VAT (right now they're assumed to be including VAT).

