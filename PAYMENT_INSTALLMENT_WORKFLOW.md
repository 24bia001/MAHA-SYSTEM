# MAHA E-HOUSING — Payment & Sold-Out Workflow

## What changed

- The customer payment amount is automatically filled from the house price.
- Customers may pay an installment smaller than the house price.
- Customer-submitted payments are stored as `PENDING` until the seller confirms receipt.
- The seller Payments page has **Confirm received** for customer-submitted payments.
- The seller can also record a payment received directly; the amount is pre-filled with the remaining balance.
- After the seller records/confirms any payment, the house status becomes `SOLD_OUT` as requested.
- The system calculates:
  - House price
  - Total paid
  - Remaining balance
- The remaining balance is shown to the customer and seller after every received payment.
- When cumulative received payments reach the full house price, a `SOLD` sale is automatically created.
- Other active bookings for the same house are cancelled when the seller records a payment.
- Customer receipts now include house price, payment received, total paid and remaining balance.

## Database

The backend uses Hibernate `ddl-auto=update`, so the new `House.status` column and new `PaymentStatus` values should be added automatically when the application starts.

If your production database does not use `ddl-auto=update`, add the equivalent nullable `status` column to `house` manually and deploy the backend migration before starting the new version.

## Recommended test

1. Seller creates a house for TSh 200,000,000.
2. Customer books it.
3. Seller approves the booking.
4. Customer opens **Payments & receipts**.
5. Select the approved booking. The amount field automatically shows TSh 200,000,000.
6. Change it to TSh 50,000,000 and submit.
7. Seller opens **Payments** and clicks **Confirm received**.
8. The house becomes **SOLD OUT**.
9. Customer sees:
   - House price: TSh 200,000,000
   - Total paid: TSh 50,000,000
   - Remaining: TSh 150,000,000
10. Seller can record another installment. After the total reaches TSh 200,000,000, the sale is automatically completed.
