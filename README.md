# 🎂 HotBake — Online Cake Marketplace

> Bangladesh's premier full-stack cake marketplace built with Spring Boot, Thymeleaf, PostgreSQL, Spring Security, Docker, GitHub Actions, and deployed on Render.

---

## 📋 Table of Contents
- [Architecture Overview](#architecture-overview)
- [ER Diagram](#er-diagram)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [How to Run Locally](#how-to-run-locally)
- [CI/CD Explanation](#cicd-explanation)
- [Deployment on Render](#deployment-on-render)
- [Default Admin Credentials](#default-admin-credentials)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Browser (Thymeleaf)                  │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│              Controller Layer (Spring MVC)               │
│  HomeController · ProductController · CartController    │
│  AuthController · BuyerController · SellerController    │
│  AdminController · ReviewController · QuestionController│
│  CheckoutController · ImageController                   │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Service Layer                          │
│  UserService · ProductService · OrderService            │
│  SellerService · ReviewService · QuestionService        │
│  AddressService · CustomUserDetailsService              │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│               Repository Layer (Spring Data JPA)         │
│  UserRepository · ProductRepository · OrderRepository   │
│  SellerProfileRepository · ReviewRepository · etc.      │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  PostgreSQL Database                     │
│         (Docker locally · Render Postgres in prod)      │
└─────────────────────────────────────────────────────────┘
```

---

## ER Diagram

```
users ──────────────── user_roles ─────────────── roles
 │  id (PK)              user_id (FK)              id (PK)
 │  first_name           role_id (FK)              name
 │  last_name
 │  email
 │  password (BCrypt)
 │  phone
 │  enabled
 │  banned
 │
 ├──1:1──► seller_profiles
 │          id (PK)
 │          user_id (FK)
 │          shop_name
 │          shop_description
 │          shop_address
 │          business_phone
 │          nid_number
 │          status (PENDING/APPROVED/REJECTED)
 │          rejection_reason
 │
 ├──1:M──► orders
 │          id (PK)
 │          buyer_id (FK → users)
 │          status (PENDING→CONFIRMED→PROCESSING→SHIPPED→DELIVERED/CANCELLED)
 │          delivery_address
 │          delivery_phone
 │          delivery_note
 │          total_amount
 │
 │           └──1:M──► order_items
 │                      id (PK)
 │                      order_id (FK)
 │                      product_id (FK)
 │                      pounds
 │                      price_per_pound
 │                      subtotal
 │
 │                       └──1:1──► reviews
 │                                  id (PK)
 │                                  product_id (FK)
 │                                  buyer_id (FK)
 │                                  order_item_id (FK, unique)
 │                                  rating (1–5)
 │                                  comment
 │
 ├──1:M──► delivery_addresses
 │          id (PK)
 │          user_id (FK)
 │          recipient_name
 │          address_line
 │          city / district
 │          phone
 │          is_default
 │
seller_profiles ──1:M──► products
                          id (PK)
                          seller_id (FK)
                          name
                          description
                          category (enum)
                          price_per_pound
                          available
                          deleted
                          
                          └──1:M──► product_images
                                     id (PK)
                                     product_id (FK)
                                     data (bytea)
                                     content_type
                                     is_primary
                          
                          └──1:M──► questions
                                     id (PK)
                                     product_id (FK)
                                     asker_id (FK → users)
                                     question_text
                                     answer_text
                                     asked_at / answered_at
```

---

## Features

### 🛒 Buyer
- Browse & search cakes by category, name, description
- Sort by newest, price ascending/descending
- View detailed product page with image gallery
- Select pounds → price auto-calculated (per pound pricing)
- Session-based shopping cart
- Cash on delivery checkout
- Manage saved delivery addresses
- Track orders with live status (PENDING → DELIVERED)
- Cancel pending/confirmed orders
- Write reviews only after order is delivered (one review per order item)
- Ask questions on product pages

### 🏪 Seller
- Apply to become a seller (shop registration form with NID)
- Admin review & approval flow
- Full product CRUD (name, description, category, price per pound, multiple images)
- Image upload with live preview (stored as bytes in PostgreSQL)
- View & manage orders — update status step by step
- Answer buyer questions from dashboard

### 🛡️ Admin
- Dashboard with platform statistics
- Review & approve/reject seller applications with reason
- Ban / unban users
- Delete any product from the platform
- Moderate reviews (delete inappropriate reviews)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Spring MVC |
| Security | Spring Security 6 (BCrypt, role-based) |
| Frontend | Thymeleaf, HTML5, CSS3, Vanilla JS |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven 3.9 + Maven Wrapper |
| Container | Docker 24, Docker Compose |
| CI/CD | GitHub Actions |
| Hosting | Render |
| Testing | JUnit 5, Mockito, SpringBootTest, MockMvc |

---

## API Endpoints

### Public
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/` | Homepage |
| GET | `/products` | Product listing (with pagination, filter, sort) |
| GET | `/products/{id}` | Product detail |
| GET | `/images/{id}` | Serve product image |
| GET | `/search?q=&category=` | Search products |
| GET | `/auth/login` | Login page |
| GET | `/auth/register` | Registration page |

### Buyer `(ROLE_BUYER)`
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/cart/add` | Add item to cart |
| POST | `/cart/update` | Update pounds |
| POST | `/cart/remove/{id}` | Remove from cart |
| GET | `/checkout` | Checkout page |
| POST | `/checkout/place` | Place order |
| GET | `/buyer/profile` | View/edit profile |
| GET | `/buyer/orders` | Order history |
| POST | `/buyer/orders/{id}/cancel` | Cancel order |
| GET | `/buyer/addresses` | Saved addresses |
| POST | `/buyer/addresses/add` | Add address |
| POST | `/reviews/add` | Submit review (after delivery) |
| POST | `/questions/ask` | Ask a question |

### Seller `(ROLE_SELLER)`
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/seller/dashboard` | Seller dashboard |
| GET | `/seller/products` | Product list |
| POST | `/seller/products/add` | Create product |
| POST | `/seller/products/edit/{id}` | Update product |
| POST | `/seller/products/delete/{id}` | Delete product |
| GET | `/seller/orders` | View orders |
| POST | `/seller/orders/{id}/status` | Update order status |
| POST | `/questions/answer/{id}` | Answer a question |

### Admin `(ROLE_ADMIN)`
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/admin/dashboard` | Admin dashboard |
| GET | `/admin/sellers` | All seller applications |
| POST | `/admin/sellers/{id}/approve` | Approve seller |
| POST | `/admin/sellers/{id}/reject` | Reject with reason |
| GET | `/admin/users` | All users |
| POST | `/admin/users/{id}/ban` | Ban user |
| POST | `/admin/users/{id}/unban` | Unban user |
| GET | `/admin/products` | All products |
| POST | `/admin/products/{id}/delete` | Delete product |
| GET | `/admin/reviews` | All reviews |
| POST | `/admin/reviews/{id}/delete` | Delete review |

---

## How to Run Locally

### Option A — Docker Compose (Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/dipraru/hotbake.git
cd hotbake

# 2. (Optional) Create a .env file from the template
cp .env.example .env
# Edit .env and set your own POSTGRES_PASSWORD

# 3. Build and start
docker compose up --build

# App is now running at:  http://localhost:8080
# Default admin login:    admin@hotbake.com / Admin@1234

# Stop containers
docker compose down

# Stop and wipe database
docker compose down -v
```

### Option B — Local Maven (requires PostgreSQL installed)

```bash
# 1. Create PostgreSQL database
createdb hotbake

# 2. Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hotbake
export SPRING_DATASOURCE_USERNAME=your_pg_user
export SPRING_DATASOURCE_PASSWORD=your_pg_password

# 3. Run
./mvnw spring-boot:run

# 4. Run tests only
./mvnw test
```

---

## CI/CD Explanation

The pipeline is defined in `.github/workflows/ci-cd.yml`.

### Triggers
- **Pull Request** to `main` or `develop` → runs tests only
- **Push to `main`** → runs tests then deploys to Render

### Pipeline Steps

```
Push to main
    │
    ▼
[Job 1: build-and-test]
    ├── Checkout code
    ├── Set up JDK 17 (Temurin)
    ├── Run: ./mvnw clean verify   ← builds + all 18 tests
    └── Upload surefire test reports
    │
    ▼ (only if tests pass)
[Job 2: deploy]
    └── POST to Render API → triggers new deployment
```

### Required GitHub Secrets
Add these in `Settings → Secrets → Actions`:

| Secret | Value |
|--------|-------|
| `RENDER_SERVICE_ID` | Your Render service ID (from service URL) |
| `RENDER_API_KEY` | Your Render account API key |

### Branch Protection Setup
1. GitHub repo → **Settings → Branches → Add rule**
2. Pattern: `main`
3. Enable: **Require a pull request before merging**
4. Enable: **Require approvals → 1**
5. Enable: **Do not allow bypassing the above settings**
6. Save

---

## Deployment on Render

### Step 1 — Create PostgreSQL Database
1. Render Dashboard → **New → PostgreSQL**
2. Name: `hotbake-db`
3. Copy the **Internal Database URL**

### Step 2 — Create Web Service
1. Render Dashboard → **New → Web Service**
2. Connect your GitHub repository
3. **Environment**: Docker
4. **Branch**: `main`
5. Set environment variables:

| Key | Value |
|-----|-------|
| `SPRING_DATASOURCE_URL` | Your Render Postgres connection string |
| `SPRING_DATASOURCE_USERNAME` | Render DB username |
| `SPRING_DATASOURCE_PASSWORD` | Render DB password |
| `SPRING_PROFILES_ACTIVE` | `prod` |

6. Enable **Auto Deploy** from main branch

### Step 3 — Get Service ID for CI/CD
- Copy the service ID from the Render URL: `https://dashboard.render.com/web/srv-XXXXXXXXXX`
- Add `srv-XXXXXXXXXX` as `RENDER_SERVICE_ID` in GitHub Secrets

---

## Default Admin Credentials

```
Email:    admin@hotbake.com
Password: Admin@1234
```
> The admin account is created automatically on first startup via `DataInitializer.java`.

---

### Git Workflow
```
main (protected)  ← only via PR from develop
develop           ← integration branch
feature/*         ← individual features, PRed into develop
```

---

## Test Coverage

| Test Class | Type | Count |
|------------|------|-------|
| `ProductServiceTest` | Unit | 5 |
| `UserServiceTest` | Unit | 5 |
| `OrderServiceTest` | Unit | 5 |
| `ReviewServiceTest` | Unit | 2 |
| `AuthControllerIntegrationTest` | Integration | 3 |
| `ProductControllerIntegrationTest` | Integration | 3 |
| `AdminControllerIntegrationTest` | Integration | 3 |
| **Total** | | **26** |
