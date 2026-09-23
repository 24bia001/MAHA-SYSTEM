# MAHA E-HOUSING — Payment, Receipt & Contract Workflow

## Fixed
- Customer payments use `/api/customer/payments` and remain `PENDING` until the seller confirms receipt.
- Seller confirmation uses the dedicated `PUT /api/payments/{paymentId}/receive` endpoint.
- The seller confirms the submitted amount without changing it from the browser.
- On seller confirmation, the payment becomes `PAID`, the booking remains `CONFIRMED`, and the house becomes `SOLD_OUT`.
- A partial payment keeps the remaining balance visible.
- When total confirmed payments reach the house price, a `SOLD` sale is recorded automatically.
- Other active bookings for the sold house are cancelled.
- Customer and seller receive the full payment response data needed for reports.
- Customer and seller can print an official receipt and a property purchase/payment contract.

## 403 fix
The frontend no longer confirms a customer payment with `PUT /api/payments/{id}`. It uses the seller-only `/receive` endpoint after the seller is authenticated. The frontend also gives a clear role/session message for stale JWT sessions.

If an old 403 is still displayed after replacing the project:
1. Stop the old Spring Boot process.
2. Start the backend from this project.
3. Stop and restart Vite.
4. Sign out of E-House.
5. Sign in again using the correct role.
6. Test customer payment submission, then seller confirmation.

## Normal flow
1. Customer books a house.
2. Seller approves the booking.
3. Customer opens Payments & receipts.
4. The amount defaults to the remaining house balance.
5. Customer may pay a smaller installment.
6. Seller opens Payments and clicks **Confirm & approve**.
7. The house becomes **SOLD OUT** and the remaining balance is updated.
8. Customer and seller can print **Receipt** and **Contract**.
9. When the balance reaches zero, the system automatically records the sale.

## Printed contract
The contract report includes:
- MAHA E-HOUSING logo
- MAHA E-HOUSING watermark
- House title, location, description, bedrooms, bathrooms and price
- Booking date and time
- Seller name, email, phone, address, NIDA and seller ID
- Customer name, email, phone, address, NIDA and customer ID
- Current payment, total paid and remaining balance
- Payment method and status
- Seller signature section
- Customer/buyer signature section
- Transaction and legal-document note

The report opens the browser print dialog so it can be printed or saved as PDF.
