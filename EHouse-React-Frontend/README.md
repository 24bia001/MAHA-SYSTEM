# E-House Unique Frontend

A polished React/Vite frontend connected to the uploaded Spring Boot backend.

## Backend detected
- Base API: `http://localhost:8081/api`
- Customer: register/login + customer portal
- Seller: login + property listing CRUD
- Management: admin login + management tables
- Customer portal: available houses, bookings, payments, sales, documents

## Run
1. Start the Spring Boot backend first.
2. Open this folder in VS Code.
3. Run:
   ```bash
   npm install
   npm run dev
   ```
4. Open the Vite URL shown in the terminal.

If your backend is not on port 8081, copy `.env.example` to `.env` and change:
`VITE_API_URL=http://localhost:YOUR_PORT/api`

## Important: CORS
The uploaded Spring Security configuration does not currently define a CORS policy. If the browser reports a CORS error, add a CORS configuration in the backend allowing the Vite origin (normally `http://localhost:5173` or `http://localhost:5174`) and enable `.cors(...)` in `SecurityConfig`.

## Notes
- HouseResponse does not contain an image field, so the UI uses high-quality placeholder property photography for cards. The house data itself comes from your backend.
- Customer booking uses `/api/customer/bookings`, matching the backend's JWT-protected customer portal.
- The admin tables use `/api/management/...`.
