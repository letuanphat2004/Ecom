# Service Contracts

Tài liệu này mô tả contract nội bộ giữa các domain trong Ecom monolith. Mục tiêu là giữ code hiện tại theo hướng modular monolith và chuẩn bị cho việc thay local client bằng HTTP client khi tách microservice.

## Nguyên Tắc Boundary

- Service không import entity/repository của domain khác.
- Service không query trực tiếp bảng do domain khác sở hữu.
- Giao tiếp giữa domain đi qua client contract hoặc public API.
- DTO/record trong contract chỉ chứa dữ liệu cần thiết, không expose JPA entity nội bộ.
- Adapter local như `LocalProductClient` và `LocalInventoryClient` được phép bridge vào service/repository hiện tại vì dự án vẫn là monolith.

## Product Contract

Product/Catalog sở hữu thông tin catalog:

```text
products
```

### `ProductClient.getProduct(productId)`

Bên gọi hiện tại:

```text
InventoryService
```

Input:

```text
productId: Long
```

Output:

```text
ProductView
- productId
- productName
- unitPrice
- active
```

Lỗi:

```text
404 Product not found
```

HTTP mapping dự kiến khi tách service:

```text
GET /internal/products/{productId}
```

### `ProductClient.findAllProducts()`

Bên gọi hiện tại:

```text
InventoryService
```

Output:

```text
List<ProductView>
```

HTTP mapping dự kiến khi tách service:

```text
GET /internal/products
```

## Inventory Contract

Inventory sở hữu tồn kho và lịch sử biến động kho:

```text
stock_items
inventory_movements
```

### `InventoryClient.initializeStock(productId, initialQuantity, reason, principal)`

Bên gọi hiện tại:

```text
ProductService
```

Input:

```text
productId: Long
initialQuantity: int
reason: String
principal: Principal
```

Output:

```text
void
```

Behavior:

```text
Tạo StockItem cho product mới.
Ghi InventoryMovement type INITIALIZATION.
```

Lỗi:

```text
400 Initial stock quantity must be greater than or equal to 0
400 Stock already initialized for product
404 Product not found
```

HTTP mapping dự kiến khi tách service:

```text
POST /internal/inventory/stock-items
```

Body dự kiến:

```json
{
  "productId": 10,
  "initialQuantity": 42,
  "reason": "Initial stock for new product"
}
```

### `InventoryClient.reserveStock(productId, quantity, reason, principal)`

Bên gọi hiện tại:

```text
OrderService
```

Input:

```text
productId: Long
quantity: int
reason: String
principal: Principal
```

Output:

```text
ReservedProduct
- productId
- productName
- unitPrice
```

Behavior:

```text
Kiểm tra product tồn tại và active.
Kiểm tra số lượng tồn kho đủ để đặt hàng.
Trừ stock quantity.
Ghi InventoryMovement type RESERVATION.
Trả snapshot tối thiểu để Order tạo order item.
```

Lỗi:

```text
400 Product is not active
400 Not enough stock
404 Product not found
```

HTTP mapping dự kiến khi tách service:

```text
POST /internal/inventory/reservations
```

Body dự kiến:

```json
{
  "productId": 10,
  "quantity": 2,
  "reason": "Order reservation"
}
```

## Order Contract

Order sở hữu đơn hàng và order item snapshot:

```text
orders
order_items
```

Order lưu thông tin người mua bằng snapshot/tham chiếu:

```text
userId
customerEmail
customerName
```

API hiện tại cho frontend:

```text
POST /api/orders
GET  /api/orders/me
```

### `POST /api/orders`

Input:

```text
CreateOrderRequest
- items
  - productId
  - quantity
```

Output:

```text
OrderResponse
- id
- status
- totalAmount
- createdAt
- items
  - productId
  - productName
  - quantity
  - unitPrice
```

Behavior:

```text
OrderService gọi UserClient.getCurrentUser để lấy snapshot người mua.
OrderService gọi InventoryClient.reserveStock cho từng item.
OrderItem lưu product snapshot, không lưu JPA relation tới Product.
```

### `GET /api/orders/me`

Output:

```text
List<OrderResponse>
```

## Planned Contract

Chưa implement vì hiện tại chưa có payment/cancel-order flow:

```text
InventoryClient.releaseStock(productId, quantity, reason, principal)
```

Cần thêm khi hệ thống có một trong các flow sau:

```text
Payment failed
Order cancelled
Order timeout
```

HTTP mapping dự kiến:

```text
POST /internal/inventory/releases
```

## Kết Quả Dependency Scan Hiện Tại

- `OrderService` đang phụ thuộc `InventoryClient`, không import `Product` hoặc `ProductRepository`.
- `OrderService` đang phụ thuộc `UserClient`, không import `User` hoặc `UserRepository`.
- `InventoryService` đang phụ thuộc `ProductClient`, không import `Product` hoặc `ProductRepository`.
- `ProductService` đang phụ thuộc `InventoryClient`, không import `StockItem` hoặc `StockItemRepository`.
- `LocalProductClient`, `LocalInventoryClient`, `LocalUserClient` và `DataSeeder` vẫn bridge vào implementation nội bộ vì dự án còn là monolith.
