# Ecom Monolith

Full-stack ecommerce application with a Spring Boot backend and a React/Vite frontend.

## Tech Stack

```text
Backend   Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL
Frontend  React, Vite
```

## Project Structure

```text
Ecom/
  backend/
  frontend/
```

## Backend

Requirements:

```text
Java 21
Maven
MySQL
```

Configuration file:

```text
backend/src/main/resources/application.yml
```

Run:

```bash
cd backend
mvn spring-boot:run
```

Default backend port:

```text
8080
```

## Frontend

Requirements:

```text
Node.js
npm
```

Install and run:

```bash
cd frontend
npm install
npm run dev
```

Default frontend port:

```text
5173
```

Optional frontend environment variable:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## Main APIs

```text
POST   /api/auth/register
POST   /api/auth/login

GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

GET    /api/inventory
GET    /api/inventory/{productId}
PATCH  /api/inventory/{productId}/restock
PATCH  /api/inventory/{productId}/adjust
GET    /api/inventory/movements

POST   /api/orders
GET    /api/orders/me
```

## Current Architecture

The backend is still a monolith, but the domain boundaries are being prepared for future service extraction:

```text
Auth/User
Product/Catalog
Inventory
Order
```

Inventory data is stored separately from product catalog data through `stock_items`, and order/inventory history stores product snapshots instead of JPA references to product entities.
