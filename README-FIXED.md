# E-House Fixed Full Project

This package contains:
- `houseSelling-main` — your Spring Boot + MySQL backend
- `EHouse-React-Frontend` — React/Vite frontend

## Why the screenshot showed "Failed to fetch"
The frontend was calling `/api/houses` while the browser was not authenticated. Your backend originally protected `GET /api/houses` for sellers only, and it had no CORS configuration. The browser can therefore show `Failed to fetch` / 401/CORS errors.

This fixed version:
1. Allows public `GET /api/houses` and `GET /api/houses/{id}` for the public property catalogue.
2. Enables CORS for Vite (`localhost:5173` and `127.0.0.1:5173`).
3. Allows OPTIONS preflight requests.
4. Removes the NIDA field from customer registration because `CustomerRequest` in the backend does not define a NIDA property.
5. Stops the frontend from loading house data while you are on unrelated pages.

## Run
### Backend
Open `houseSelling-main` in IntelliJ and run the Spring Boot application. It uses port `8081`.

### Frontend
Open `EHouse-React-Frontend` in VS Code:
```bash
npm install
npm run dev
```

Open the Vite address, normally `http://localhost:5173` or `http://localhost:5174`.

Do not change the frontend API URL unless your backend port changes.
