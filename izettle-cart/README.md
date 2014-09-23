# iZettle Cart

This set of utility classes aims at helping out doing generic calculations on a (shopping) cart.

## Why is this needed?
At a point we started having lot's of different implementation for doing this logic in different places. While not very complicated, it's easy to go wrong, and introduce small differences between implementations. Problems such as rounding and distribution of vat amounts when applying discounts have been seen.

## What is this?
This set of classes intends to help out only in doing calculations on the cart itself as an abstract concept. It handles no other business properties such as shipment, fees etc. As the calculations are purely numerical, there is no need to include information about actual currency in this scope. The general concept is that a Cart is cheap to build, and should be built (and directly thrown away) whenever one needs financial data from a collection of items and discounts. For example a complex entity containing lots of other stuff needn't necessarily itself represent a Cart, but could for example in a hypothetical method `getPrice()` just temporarily build a Cart and as that object for it's value.

## Basic types used:
### Money
Represented as whole numbers (`long` and `Long`), using the (unknown) currency's minimal denominator. E.g. $10 would be represented as 1000 (cents). This is to make sure that no fractional values are used, and that all necessary rounding takes place on each line.
### Quantities
Represented as `BigDecimal` to allow for arbitrarily precise values, e.g. 0.000123 kg of saffron.
### VAT Percentage
In most countries, valid VAT percentages are expressed in whole numbers, but as always, there are exceptions. Therefore, VAT percentages are expressed as a floating point number, with `Float`. nullable for scenarios when VAT is not applicable.
### Primitive vs Object
The distinction between `Float` and `float` has been made to indicate when a value is nullable or not. Same goes for `Long` vs `long`.

## Classes and interfaces
### `Item` and `Discount`
These are the interfaces that should be implemented by a user, each having getters for accessing `unitPrice`, `quantity` and `vatPercentage`. An Item can also contain an optional local discount that will be applied to only the item itself, as opposed to the cart-wide discount that will affect the entire carts value. Multiple cart-wide discounts is possible: they will then be calculated as if applied on the gross value of the cart in consecutive order. E.g. two 10% discounts will result in a total discount of 19%. Also noteworthy is that discounts can be negative, and would then represent a value addition/a 'top up'.
### `Cart`
The cart is constructed as an immutable object, where the collections of items and discounts are given at construction time. After construction, the cart holds information on a per line level, and a total level. Each line is represented by a `ItemLine` or a `DiscountLine`. The object is queryable for properties such as grossValue, value etc. Also, the cart can be cloned into it's negative counterpart by calling `Cart.inverse()`, which is useful for example when creating a cart that is representing a full refund of the original.
### `ItemLine` and `DiscountLine`
These are the objects that will populate the cart's 'item list'. Each item and discount line will hold information such as actual value and actual VAT, but also keeping a reference to it's original item/discount.

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

	Cart<TestItem, TestDiscount, TestDiscount> cart = new Cart<>(items, discounts);
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
		Value: 940
		Discount Value: 110
		Discount Percentage: 10.476190476190476
		VAT: 96
}
```

## Todos
* Possibility to treat a cart as an item, effectively opening up for adding one cart into another.
* Possibility to call inverse, but for just a subset of the items, effectively allowing for partial refunds.



