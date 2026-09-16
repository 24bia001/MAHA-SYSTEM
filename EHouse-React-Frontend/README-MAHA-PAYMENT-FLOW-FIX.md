# MAHA E-HOUSING payment/booking flow fix

## What was fixed
- Seller dashboard now loads houses, bookings, payments, and sales independently. If one endpoint fails, the other collections still appear.
- Booking statuses are normalized and `CONFIRMED` is displayed as `APPROVED` in the seller/customer UI.
- Seller payment dropdown only contains approved (`CONFIRMED`) bookings that do not already have a `PAID` payment.
- Seller payment button is disabled when there is no approved booking.
- A clear message tells the seller to open **Bookings received**, approve a pending booking, then return to **Payments**.
- Customer payment uses the same approved-booking rule.

## Correct workflow
1. Customer signs in.
2. Customer opens a house and requests a viewing date.
3. The request is saved as `PENDING`.
4. Seller opens **Bookings received** and clicks **Approve**.
5. The booking becomes `CONFIRMED` in the database and is shown as **APPROVED** in the UI.
6. Seller opens **Payments**. The approved booking is now selectable.
7. Seller enters the amount and clicks **Mark PAID**.
8. Customer sees the approved booking in **Payments & receipts** and can make the payment.
9. A paid booking can then be used for the sale workflow.

If the seller Payments dropdown is empty, there is currently no `CONFIRMED` booking belonging to that seller that is still unpaid. This is expected until step 4 is completed.
