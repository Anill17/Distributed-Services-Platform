# Distributed Services – API Client (Frontend)

A simple single-page web UI to call all APIs of the User, Product, and Order services.

## Prerequisites

1. **Backends running** (default ports):
   - **User service:** http://localhost:8081
   - **Order service:** http://localhost:8082
   - **Product service:** http://localhost:8083

2. **CORS** is enabled on all three services (see `WebConfig` in each service).

## How to run

Browsers block `file://` requests to other origins, so serve the frontend over HTTP.

**Option A – Python 3**
```bash
cd frontend
python3 -m http.server 5500
```
Then open: http://localhost:5500

**Option B – Node (npx)**
```bash
cd frontend
npx serve -p 5500
```
Then open: http://localhost:5500

**Option C – VS Code Live Server**  
Right‑click `index.html` → “Open with Live Server”.

## Config

Use the **sidebar** to change API base URLs if your services run on other hosts/ports.

## What you can do

- **Users:** List, get by ID/username/email, search, exists (username/email), create, update, delete.
- **Products:** List (paged, sort), get by ID/SKU, by category, search, available, create, update, patch quantity, delete.
- **Orders:** List, by user, by status, get by ID/order number, create (with line items), update, patch status, delete.

Responses are shown in the output area under each section (green = success, red = error).
