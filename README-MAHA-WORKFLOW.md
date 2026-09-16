# MAHA E-HOUSING — Role Workflow

## Roles
- ADMIN: creates seller accounts and monitors system totals/sales.
- SELLER: logs in with an account created by admin, adds houses, receives/handles bookings, records payments, completes sales, and prints sales reports.
- CUSTOMER: registers themselves, browses available houses, books viewings, pays confirmed bookings, prints receipts, and views purchases/documents.

## Default admin
- Email: admin@mahaehousing.co.tz
- Password: Admin@12345

## Customer payment workflow
1. Customer requests a viewing.
2. Seller confirms the booking.
3. Customer selects the confirmed booking under Payments and submits a payment.
4. Seller sees the payment and can complete the sale.
5. Completed SOLD houses disappear from the available customer catalogue.
6. Customer can print the payment receipt.

## Frontend
From `EHouse-React-Frontend` run:

```bash
npm install
npm run dev
```

## Backend
Run the Spring Boot application from `houseSelling-main` with Java 24 and MySQL configured in `application.properties`.
