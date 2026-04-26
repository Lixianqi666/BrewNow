# Copilot instructions for 沏刻茶叶电商平台

## Build, test, and lint commands

Run commands from the repository root unless noted.

### Backend (`backend/`, Spring Boot + Maven)

- Start backend: `cd backend && mvn spring-boot:run`
- Run full backend tests: `cd backend && mvn test`
- Run a single backend test class: `cd backend && mvn -Dtest=RecommendControllerTest test`
- Alternative single-test example: `cd backend && mvn -Dtest=SystemControllerTest test`

### Frontend (`frontend/`, Vue 3 + Vite + TypeScript)

- Install dependencies: `cd frontend && npm install`
- Start dev server: `cd frontend && npm run dev`
- Lint: `cd frontend && npm run lint`
- Type-check: `cd frontend && npm run type-check`
- Build (type-check + vite build): `cd frontend && npm run build`
- Build only (skip type-check): `cd frontend && npm run build-only`
- Preview production build: `cd frontend && npm run preview`

### Local orchestration scripts

- Start local stack: `./start.sh`
- Stop local stack: `./stop.sh`

`start.sh` assumes local Homebrew services for MySQL/Redis and starts MinIO, backend, and frontend with logs under `/tmp/*.log`.

## High-level architecture

This repo is a full-stack monorepo:

1. **Backend (`backend/`)**
   - Spring Boot 2.7 app under `com.brewnow` with modules: `controller`, `service`, `service/impl`, `mapper`, `entity`, `config`, `interceptor`, `utils`.
   - MyBatis is XML-mapped (`src/main/resources/mapper/*.xml`) with Java mapper interfaces in `mapper/`.
   - API base path is `/api` (from `application.yml`).
   - Cross-cutting concerns:
     - `JwtInterceptor` handles Bearer token verification and injects `userId/userType/role/merchantId` into request attributes.
     - `AuditLogInterceptor` writes request audit logs to `AUDIT_LOG`.
     - `GlobalExceptionHandler` maps validation/runtime errors into unified API responses.
   - Infra integrations:
     - MySQL + Druid
     - Optional Redis cache via `app.cache.redis-enabled`
     - MinIO object storage (`MinioStorageServiceImpl`) with auto bucket setup.
   - Recommendation subsystem:
     - `RecommendController` + `RecommendationServiceImpl`
     - Uses behavior data (`VIEW/FAVORITE/CART/PURCHASE`), time decay, seasonality, fallback content rules, and cache layers.

2. **Frontend (`frontend/`)**
   - Vue 3 + Vite + TypeScript app with role-separated UX:
     - consumer views (`views/*.vue`)
     - merchant area (`views/merchant/*`)
     - admin area (`views/admin/*`)
   - `src/api/request.ts` is the shared HTTP layer (Axios instance, token injection, loading/error handling, 401 redirect).
   - `src/router/index.ts` enforces route auth and user-type permissions using the Pinia user store.
   - Vite base path is environment-driven (`VITE_BASE_PATH`) for GitHub Pages; API base defaults to `/api` unless `VITE_API_BASE_URL` is set.

3. **Database and data bootstrap (`sql/`)**
   - `brew-now.sql` is the main schema/data dump (production-like structure, soft delete fields, snapshots, constraints, seed data).
   - `init.sql` is a simplified bootstrap script and does not represent full production schema parity.

## Key conventions in this codebase

1. **Unified response envelope is mandatory**
   - Backend controllers return `Result<T>` (`code/message/data/success/timestamp`).
   - Frontend request layer expects this envelope and treats `code === 200` as success.

2. **Auth model is role-aware and claim-driven**
   - JWT carries `userId`, `userType`, `role`, `merchantId`.
   - Route access is enforced in both backend interceptors and frontend router/store permission maps.
   - When adding a protected endpoint, update `WebMvcConfig` interceptor path rules and corresponding frontend route/meta permissions.

3. **Recommendation behavior and cache invalidation are coupled to business actions**
   - User actions record behavior events from multiple flows (product detail/cart/favorite/order purchase).
   - Recommendation caches are explicitly evicted on product/favorite/cart/order mutations.
   - If a change impacts user behavior signals or product/order state, check whether recommendation cache eviction and behavior recording should be updated together.

4. **MyBatis + soft-delete pattern**
   - SQL uses `snake_case`; Java entities use `camelCase` with MyBatis `map-underscore-to-camel-case: true`.
   - Several entities use soft-delete semantics (`is_deleted` or `deleted_at`), and query methods often have include-deleted variants.
   - Prefer extending existing mapper XML and service methods over adding ad-hoc SQL in controllers.

5. **Frontend state/auth persistence conventions**
   - Auth token and user state persist through `userStorage` in `src/utils/storage.ts`.
   - HTTP token attachment and unauthorized redirects are centralized in `src/api/request.ts`; API modules should use this shared client instead of raw Axios calls.

6. **Element Plus postinstall patch is intentional**
   - `frontend/package.json` runs `postinstall` script `scripts/fix-element-plus-use-cursor.mjs`.
   - Do not remove this script casually; it patches a known `use-cursor` issue in the installed Element Plus package.
