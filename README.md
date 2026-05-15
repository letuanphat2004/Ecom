# Ecom Monolith

Ecom is a full-stack ecommerce application with a Spring Boot backend and a React/Vite frontend. The project is currently a monolith, but it is being refactored step by step toward a modular architecture that can later be extracted into microservices.

This README is the main project document. Update it whenever a meaningful architecture, API, database, or refactor change is completed.

## Tech Stack

```text
Backend   Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL
Frontend  React, Vite
Build     Maven, npm
```

## Project Structure

```text
Ecom/
  backend/
    src/main/java/com/ecom/
      client/
      config/
      controller/
      dto/
      entity/
      exception/
      repository/
      security/
      service/
    src/main/resources/
      application.yml

  frontend/
    src/
      app/
      components/
      pages/
      services/
      utils/
```

## Local Setup

### Backend Requirements

```text
Java 21
Maven
MySQL
```

Backend configuration lives in:

```text
backend/src/main/resources/application.yml
```

Run backend:

```bash
cd backend
mvn spring-boot:run
```

Default backend port:

```text
8080
```

### Frontend Requirements

```text
Node.js
npm
```

Run frontend:

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

### Auth

```text
POST /api/auth/register
POST /api/auth/login
```

### Products

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

### Inventory

```text
GET   /api/inventory
GET   /api/inventory/{productId}
PATCH /api/inventory/{productId}/restock
PATCH /api/inventory/{productId}/adjust
GET   /api/inventory/movements
```

### Orders

```text
POST /api/orders
GET  /api/orders/me
```

## Current Domains

The backend currently has these business areas:

```text
Auth/User
Product/Catalog
Inventory
Order
```

It is still one Spring Boot application and one deployable backend. The current refactor goal is to reduce coupling inside the monolith before extracting physical services.

## Database Ownership Direction

Current direction for future service ownership:

```text
Auth/User owns:
- users
- user_roles

Product/Catalog owns:
- products

Inventory owns:
- stock_items
- inventory_movements

Order owns:
- orders
- order_items
```

The database may still be physically shared while the project is a monolith. The important rule during refactor is to move toward clear ownership and avoid cross-domain JPA relationships.

## Refactor Progress

### 1. OrderItem Product Snapshot

`OrderItem` no longer keeps a JPA relationship to `Product`.

Current order item data:

```text
productId
productName
unitPrice
quantity
```

Why:

```text
Order history must keep product name and price at the time of purchase.
Order should not depend directly on Product entity mapping.
```

### 2. InventoryMovement Product Snapshot

`InventoryMovement` no longer keeps a JPA relationship to `Product`.

Current movement data:

```text
productId
productName
type
quantityChange
stockAfter
reason
createdBy
createdAt
```

Why:

```text
Inventory history should remain readable without joining Product.
Inventory should move toward owning its own historical records.
```

### 3. Stock Moved Out Of Product

`Product` no longer owns stock quantity.

Current split:

```text
products
- catalog information such as name, description, price, imageUrl, active

stock_items
- productId
- quantity
- updatedAt
```

Why:

```text
Product/Catalog owns product information.
Inventory owns stock quantity.
```

### 4. InventoryClient Introduced For Order Flow

`OrderService` no longer calls `InventoryService` directly.

Current flow:

```text
OrderService
  -> InventoryClient
  -> LocalInventoryClient
  -> InventoryService
```

Why:

```text
Order depends on a contract instead of a concrete Inventory service.
Later, LocalInventoryClient can be replaced by an HTTP client when Inventory becomes a separate service.
```

Current client contract returns:

```text
ReservedProduct
- productId
- productName
- unitPrice
```

## Current Order Flow

The project currently treats order placement as completed immediately. There is no separate payment flow yet.

Current flow:

```text
Customer submits order
OrderService asks InventoryClient to reserve stock
Inventory decreases stock quantity
Order is saved
Order response is returned
```

Because there is no pending payment state yet, there is no release-stock flow. If payment is added later, the system will need a compensation flow such as:

```text
reserveStock
payment success -> confirm order
payment failure/cancel/timeout -> releaseStock
```

## Next Refactor Candidates

Recommended next steps:

```text
1. Add ProductClient so InventoryService does not depend directly on ProductRepository.
2. Add a dedicated workflow for create product + initialize stock.
3. Define internal API contracts for Product, Inventory, and Order.
4. Add releaseStock when payment or cancel-order flow is introduced.
5. Split into physical services only after boundaries are stable.
```

## README Update Rule

Whenever a meaningful change is completed, update this README in the same commit or the next commit.

Examples of changes that should update this file:

```text
New API endpoint
Database table or ownership change
Domain boundary change
Client/interface contract change
New service extraction step
Security or configuration requirement change
Run/build instruction change
```

Keep this README updated enough to explain the current architecture and refactor state.
