GetAvailability:
1. Get Stock from Stock Actor
2. Get Backorder from Backorder Actor
3. Create GetAvailabilityResponse and return to API

AddToCart
- backorderOk: Boolean
- splitOrderOk: Boolean
1. Get Stock from Stock Actor
2. Place in cart (Stock)
(3). Place in cart (Backorder)
(4). Reject if 

AbandonCart
1. Abandon cart (Stock)
2. Abandon cart (Backorder)

Checkout
1. Checkout (Stock)
* Don't checkout against Backorder Actor, since the reservation stays until the order is fulfilled

AcknowledgeShipment
1. Acknowledge shipment (Backorder)

AcceptShipment
1. Accept shipment (Backorder) - send message to Fulfillment module if there are reservations against that shipment
2. Send remainder count to 