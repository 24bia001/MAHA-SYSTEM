# MAHA E-HOUSING local connection fix

The frontend now uses `/api` by default and Vite proxies it to Spring Boot at `http://localhost:8081`.
This removes local CORS/port mismatch problems.

## Start order

### Backend
From `houseSelling-main`:
```text
mvnw.cmd spring-boot:run
```
or run `HouseSellingApplication` from IntelliJ.

The backend must be listening on:
```text
http://localhost:8081
```

Test it in the browser:
```text
http://localhost:8081/api/health
```
It should return:
```json
{"status":"UP","service":"MAHA E-HOUSING API"}
```

### Frontend
From `EHouse-React-Frontend`:
```text
npm install
npm run dev
```

Open the Vite URL shown in the terminal, normally:
```text
http://localhost:5173
```

If `/api/health` does not open, the frontend cannot fetch data because the Spring Boot backend or its MySQL database is not running.

For a deployed frontend, create `.env`:
```text
VITE_API_URL=https://YOUR-BACKEND-DOMAIN/api
```
then restart Vite/redeploy.
