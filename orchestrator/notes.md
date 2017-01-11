Search for products:
- productcatalog: get products that match search category/manufacturer/searchstring
- get availability for above products from inventory
- merge results

Place in Cart
- check availability in inventory and reserve/hold as applicable
- if inventory check successful, post Hold/Reservation to ShoppingCart

AbandonCart
- post Abandon to Inventory
- post Abandon to ShoppingCart

Checkout
- post payment to Payment and receive receipt
- post Checkout to Inventory
- post Checkout to ShoppingCart
- post Order to OrderTracking