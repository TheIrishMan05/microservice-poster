# Ручной прогон через Swagger

Предусловия:

1. `Copy-Item .env.example .env`
2. `docker compose up --build`
3. Открыть [Swagger UI](http://localhost:8080/swagger-ui.html)
4. В выпадающем списке выбрать сервис (`user-service` / `event-service` / `order-service`)

После миграций в БД уже есть:

| Данные | Зачем |
| --- | --- |
| ~30 пользователей (`admin` + `demo_user_01…25`) | offset-пагинация `GET /api/users` |
| 25 уведомлений у `admin` (id=1) | `X-Total-Count` на `GET /api/notifications` |
| 30 `Demo Event #01…#30`, теги, VIP-места на `#01` | infinite scroll + заказ |

---

## 1. Пагинация (offset) — `user-service`

1. Выбрать **user-service**.
2. `GET /api/users` → `Try it out`:
   - `page=0`, `size=5` → Execute  
   - в Response headers найти **`X-Total-Count`** (ожидается ≥ 30)  
   - в body ровно 5 пользователей
3. Повторить с `page=1`, `size=5` — следующий набор id/username.
4. Аналогично `GET /api/notifications?userId=1&page=0&size=5` — снова смотреть **`X-Total-Count: 25`**.

---

## 2. Infinite scroll (cursor) — `event-service`

1. Выбрать **event-service**.
2. `GET /api/events`:
   - `afterId=0`, `limit=5` → первые 5 событий, запомнить **последний `id`** в ответе (например `5`)
   - `afterId=<тот id>`, `limit=5` → следующая «страница» без `X-Total-Count` (так и задумано)
3. Опционально `status=ACTIVE` — только активные.

---

## 3. CRUD пользователя — `user-service`

### Create

`POST /api/users`

```json
{
  "userName": "swagger_demo",
  "email": "swagger@poster.ru",
  "role": "USER"
}
```

Ожидание: **201**, в теле есть `id` — сохранить.

### Read / Update / Delete

- `GET /api/users/{id}`
- `PUT /api/users/{id}`:

```json
{
  "userName": "swagger_demo2",
  "email": "swagger2@poster.ru",
  "role": "ADMIN"
}
```

- `DELETE /api/users/{id}` → **204**, повторный GET → **404**

---

## 4. CRUD события — `event-service`

### Create

`POST /api/events`

```json
{
  "eventName": "Swagger Live",
  "description": "Created from Swagger UI",
  "location": "Demo Hall",
  "dateTime": "2026-12-01T19:00:00",
  "status": "ACTIVE",
  "tagNames": ["music", "live"]
}
```

### Update / Delete

- `PUT /api/events/{id}` — сменить `eventName` / `status`
- `DELETE /api/events/{id}` → **204**

Теги: `GET/POST/PUT/DELETE /api/tags` — тот же сценарий create → list → delete.

---

## 5. Заказ (сквозной) — `order-service`

Нужны: пользователь (например id **1** = `admin`), событие **`Demo Event #01`** (ACTIVE + seats VIP), тип билета **VIP** (`GET /api/ticket-types`, обычно id **1**).

1. В **event-service**: `GET /api/events?afterId=0&limit=5` — найти id `Demo Event #01`.
2. В **order-service**: `POST /api/orders`

```json
{
  "userId": 1,
  "items": [
    {
      "eventId": <id Demo Event #01>,
      "ticketTypeId": 1,
      "quantity": 2
    }
  ]
}
```

Ожидание: **201**, `status: PENDING`, в items есть `reservationId` и tickets.

3. `GET /api/orders?userId=1&page=0&size=10` — список + **`X-Total-Count`**.
4. `PUT /api/orders/{id}/status`:

```json
{ "status": "CONFIRMED" }
```

или `{ "status": "CANCELLED" }` (для PENDING — снимет резерв в event-service).

---

## Что показать на защите

1. **Offset + `X-Total-Count`** — users / notifications / orders.  
2. **Cursor infinite scroll** — events `afterId`/`limit`.  
3. **CRUD** — create/update/delete user или event.  
4. **Межсервисный поток** — create order (Feign reserve) → confirm/cancel.
