# MAHA E-HOUSING — Booking Fix

## What was fixed
- Customer booking endpoint remains protected and now explicitly requires `ROLE_CUSTOMER`.
- CORS accepts the deployed frontend when JWT is sent through the `Authorization` header.
- Customer booking now stores both **date and time**.
- A house response now includes seller name, email, phone and profile image.
- Customer house details show the seller profile card.
- Booking form validates a future date and viewing time.
- A successful booking displays a polished **Congratulations!** confirmation with house, seller, date and time.
- Customer booking history shows booking date and time.
- Two customers can request different viewing times on the same day; the same house/date/time cannot have two active bookings.

## Important after replacing the files
1. Restart the Spring Boot backend so Hibernate adds the new `booking_time` column automatically (`ddl-auto=update`).
2. In the browser, sign out and sign in again as a **CUSTOMER**. This refreshes the JWT token used for the booking request.
3. Open a house, choose a date and time, then click **Request private viewing**.
4. The seller will see the booking in **Bookings received** and can approve/reject it.

## If the browser still shows 403
Open DevTools → Application → Local Storage and remove the old `ehouse_token` and `ehouse_user`, then sign in again. The current frontend/backend use `ROLE_CUSTOMER` consistently.
