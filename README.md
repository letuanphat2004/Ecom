# Ecom Monolith

Ecom là ứng dụng ecommerce full-stack gồm backend Spring Boot và frontend React/Vite. Dự án hiện tại vẫn là monolith, nhưng đang được refactor từng bước theo hướng modular monolith để sau này có thể tách dần thành microservice.

README này là tài liệu chính của dự án. Khi hoàn thành một thay đổi đáng kể về architecture, API, database hoặc refactor, cần cập nhật lại README.

Contract nội bộ giữa Product, Inventory và Order được mô tả trong [SERVICE_CONTRACTS.md](SERVICE_CONTRACTS.md).

## Tech Stack

```text
Backend   Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL
Frontend  React, Vite
Build     Maven, npm
```

## Cấu Trúc Dự Án

```text
Ecom/
  backend/
    src/main/java/com/ecom/
      product/
        client/
        controller/
        dto/
        entity/
        repository/
        service/
      inventory/
        client/
        controller/
        dto/
        entity/
        repository/
        service/
      order/
        controller/
        dto/
        entity/
        repository/
        service/
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

## Chạy Local

### Backend

Yêu cầu:

```text
Java 21
Maven
MySQL
```

File cấu hình backend:

```text
backend/src/main/resources/application.yml
```

Chạy backend:

```bash
cd backend
mvn spring-boot:run
```

Port mặc định:

```text
8080
```

### Frontend

Yêu cầu:

```text
Node.js
npm
```

Chạy frontend:

```bash
cd frontend
npm install
npm run dev
```

Port mặc định:

```text
5173
```

Biến môi trường frontend nếu cần đổi API base URL:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## API Chính

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

Create product request:

```json
{
  "name": "Ao thun cotton",
  "description": "Ao thun co ban",
  "price": 159000,
  "imageUrl": "https://example.com/image.jpg",
  "active": true,
  "initialStockQuantity": 42
}
```

`initialStockQuantity` chỉ được dùng khi tạo product mới. Khi update product, tồn kho vẫn được quản lý qua Inventory API.

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

## Domain Hiện Tại

Backend hiện có các nhóm nghiệp vụ chính:

```text
Auth/User
Product/Catalog
Inventory
Order
```

Hiện tại toàn bộ backend vẫn là một Spring Boot application và một deployable backend. Mục tiêu refactor hiện tại là giảm coupling bên trong monolith trước khi tách thành các service vật lý.

## Hướng Sở Hữu Database

Định hướng ownership cho các service sau này:

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

Trong giai đoạn monolith, database vẫn có thể được dùng chung về mặt vật lý. Điểm quan trọng là code và data model cần đi dần về hướng ownership rõ ràng, hạn chế JPA relationship vượt boundary.

## Tiến Độ Refactor

### 1. OrderItem lưu product snapshot

`OrderItem` không còn giữ JPA relationship trực tiếp tới `Product`.

Dữ liệu hiện tại của order item:

```text
productId
productName
unitPrice
quantity
```

Ý nghĩa:

```text
Order history giữ lại tên và giá sản phẩm tại thời điểm mua.
Order không phụ thuộc trực tiếp vào Product entity mapping.
```

### 2. InventoryMovement lưu product snapshot

`InventoryMovement` không còn giữ JPA relationship trực tiếp tới `Product`.

Dữ liệu hiện tại của inventory movement:

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

Ý nghĩa:

```text
Inventory history vẫn đọc được mà không cần join Product.
Inventory tiến gần hơn tới việc sở hữu historical records riêng.
```

### 3. Tách stock khỏi Product

`Product` không còn sở hữu stock quantity.

Phân tách hiện tại:

```text
products
- thông tin catalog như name, description, price, imageUrl, active

stock_items
- productId
- quantity
- updatedAt
```

Ý nghĩa:

```text
Product/Catalog owns product information.
Inventory owns stock quantity.
```

### 4. Thêm InventoryClient cho order flow

`OrderService` không còn gọi trực tiếp `InventoryService`.

Flow hiện tại:

```text
OrderService
  -> InventoryClient
  -> LocalInventoryClient
  -> InventoryService
```

Ý nghĩa:

```text
Order phụ thuộc vào contract thay vì concrete Inventory service.
Sau này LocalInventoryClient có thể được thay bằng HTTP client khi Inventory tách thành service riêng.
```

Contract hiện tại trả về:

```text
ReservedProduct
- productId
- productName
- unitPrice
```

### 5. Thêm ProductClient cho inventory flow

`InventoryService` không còn gọi trực tiếp `ProductRepository`.

Flow hiện tại:

```text
InventoryService
  -> ProductClient
  -> LocalProductClient
  -> ProductRepository
```

Ý nghĩa:

```text
Inventory phụ thuộc vào contract thay vì concrete Product repository.
Sau này LocalProductClient có thể được thay bằng HTTP client khi Product/Catalog tách thành service riêng.
```

Contract hiện tại trả về:

```text
ProductView
- productId
- productName
- unitPrice
- active
```

### 6. Tạo workflow create product + initialize stock

`ProductService` không tự ghi trực tiếp vào bảng tồn kho. Khi admin tạo product mới, Product domain tạo catalog record trước, sau đó gọi `InventoryClient.initializeStock(...)` để Inventory domain tạo `StockItem`.

Flow hiện tại:

```text
ProductController
  -> ProductService.create()
  -> InventoryClient.initializeStock()
  -> LocalInventoryClient
  -> InventoryService.initializeStock()
```

Request tạo product có thêm:

```text
initialStockQuantity
```

Ý nghĩa:

```text
Product owns catalog data.
Inventory owns stock quantity.
Create product flow phối hợp qua client contract thay vì trộn stock vào Product entity.
Sau này LocalInventoryClient có thể được thay bằng HTTP client khi Inventory tách thành service riêng.
```

### 7. Tái cấu trúc code theo domain package

Code backend đang được chuyển dần từ cách chia theo layer kỹ thuật sang cách chia theo domain nghiệp vụ. Mỗi domain sẽ gom các phần liên quan như controller, service, dto, entity, repository và client contract vào cùng một package domain.

Ý nghĩa:

```text
Boundary của từng domain rõ hơn.
Dễ kiểm soát domain nào được phụ thuộc domain nào.
Dễ tách từng domain thành service vật lý sau này.
```

### 8. Tách Order khỏi User về mặt model

`Order` không còn giữ JPA relationship trực tiếp tới `User`.

Dữ liệu người mua hiện tại trong order:

```text
userId
customerEmail
customerName
```

`OrderService` không còn gọi trực tiếp `UserRepository`. Flow hiện tại:

```text
OrderService
  -> UserClient
  -> LocalUserClient
  -> UserRepository
```

Ý nghĩa:

```text
Order lưu snapshot/tham chiếu người mua tại thời điểm đặt hàng.
Order không phụ thuộc trực tiếp vào User entity mapping.
Sau này LocalUserClient có thể được thay bằng HTTP client khi Auth/User tách thành service riêng.
```

## Order Flow Hiện Tại

Dự án hiện tại coi việc đặt hàng là hoàn tất ngay, chưa có payment flow riêng.

Flow hiện tại:

```text
Customer submits order
OrderService asks InventoryClient to reserve stock
Inventory decreases stock quantity
Order is saved
Order response is returned
```

## Các Bước Refactor Tiếp Theo


```text
1. Tiếp tục tái cấu trúc các domain còn lại theo domain package.
2. Chỉ tách thành service vật lý sau khi boundary đã ổn định.
```
