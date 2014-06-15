# iZettle Cart

This set of utility classes aims at helping out doing generic calculations on a (shopping) cart.

## Why is this needed?
At a point we started having lot's of different implementation for doing this logic in different places. While not very complicated, it's easy to go wrong, and introduce small differences between implementations. Problems such as rounding and distribution of vat amounts when applying discounts have been seen.

## What is this?
This set of classes intends to help out only in doing calculations on the cart itself as an abstract concept. It handles no other business properties such as shipment, fees etc. As the calculations are purely numerical, there is no need to include information about actual currency in this scope.

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
These are the interfaces that should be implemented by a user, each having getters for accessing `unitPrice`, `quantity` and `vatPercentage`
### `Cart`
The cart is constructed as an immutable object, where the collections of items and discounts are given at construction time. After construction, the cart holds information on a per line level, and a total level. Each line is represented by a `ItemLine` or a `DiscountLine`. The object is queryable for properties such as totalGrossAmount, effectivePrice etc. Also, the cart can be cloned into it's negative counterpart by calling Cart.inverse(), which is useful for example when creating a cart that is representing a full refund of the original.
### `ItemLine` and `DiscountLine`
These are the objects that will populate the cart's 'item list'. Each item and discount line will hold information such as effectivePrice and effectiveVat, but also keeping a reference to it's original item/discount.

## Examples
For now, the easiest way is probably to look at the [`CartTest`](https://github.com/iZettle/izettle-toolbox/blob/master/izettle-cart/src/test/java/com/izettle/cart/CartTest.java) class.

## Todos
* Currently there is only support for 'global' discounts, e.g. any discount (percent or amount) is applied cart wide. Discount on a per item basis might be a candidate for future improvements.
* Possibility to treat a cart as an item, effectively opening up for adding one cart into another.
* Possibility to call inverse, but for just a subset of the items, effectively allowing for partial refunds.



