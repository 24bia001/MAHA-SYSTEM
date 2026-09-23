# MAHA E-HOUSING — Final Booking, Payment, Receipt & Contract Fix

## Authentication / 403
The frontend now stores JWT/user state in sessionStorage, preventing a customer login in one browser tab from overwriting a seller session in another tab. Sign out and sign in again after installing this version.

## Booking
Customer booking uses POST /api/customer/bookings with bookingDate, bookingTime and houseId. Seller/customer permissions are enforced by JWT role.

## Payment
Customer submits a PENDING payment for an approved booking. The amount defaults to the current remaining house balance but can be a smaller installment. Seller confirms it with PUT /api/payments/{id}/receive. A received payment sets the house SOLD_OUT and recalculates the remaining balance. Full payment creates a SOLD sale.

## Documents
Receipt and contract print reports contain company logo, watermark, property, customer, seller and payment details plus signature areas.

## House fields
House title, location, description, price, bedrooms, bathrooms, halls and kitchens are persisted and returned by the API. Hibernate ddl-auto=update will add missing columns on startup. Existing records with null/zero halls or kitchens must be edited once to enter their correct values.
