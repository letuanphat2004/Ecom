# Ecom Monolith

Du an thuong mai dien tu monolith, tach rieng backend va frontend de sau nay co the tach module thanh microservice.

## Cau truc

```text
Ecom/
  backend/   Spring Boot, Spring Security JWT, JPA, MySQL
  frontend/  ReactJS, Vite
```

## Backend

Backend dung MySQL voi cau hinh mac dinh:

```yaml
url: jdbc:mysql://localhost:3306/ecom_db
username: root
password: 15032004
```

Chay backend:

```bash
cd backend
mvn spring-boot:run
```

API chinh:

```text
POST   /api/auth/register
POST   /api/auth/login
GET    /api/products
GET    /api/products/{id}
POST   /api/products        ROLE_ADMIN
PUT    /api/products/{id}   ROLE_ADMIN
DELETE /api/products/{id}   ROLE_ADMIN
POST   /api/orders
GET    /api/orders/me
```

Tai khoan admin seed san:

```text
email: admin@ecom.local
password: admin123
```

## Frontend

Chay frontend:

```bash
cd frontend
npm install
npm run dev
```

Mac dinh frontend goi backend tai:

```text
http://localhost:8080/api
```

Neu can doi API URL, tao file `frontend/.env` theo `frontend/.env.example`.

## Huong tach microservice sau nay

Backend hien chia ro theo boundary:

```text
auth/user
product/catalog
order
security/config
```

Khi tach microservice, co the uu tien tach `product` va `order` thanh service rieng, sau do thay repository call bang HTTP/gRPC/message broker.
