# Лабораторная работа №2. Высокопроизводительные системы. Микросервисная информационная система для покупки/продажи билетов на события

Перед запуском создайте локальный файл окружения:

```powershell
Copy-Item .env.example .env
```

## Маршрутизация через API Gateway

Внешняя точка входа: `http://localhost:8080`.

| Путь | Сервис |
| --- | --- |
| `/api/users/**`, `/api/notifications/**` | `user-service` |
| `/api/events/**`, `/api/registrations/**`, `/api/ticket-types/**`, `/api/ticket-waitlist/**` | `event-service` |
| `/api/orders/**`, `/api/tickets/**` | `order-service` |

Gateway находит экземпляры сервисов через Eureka. Для каждого запроса он сохраняет
переданный `X-Request-Id` или создаёт новый и пишет начало и окончание запроса в лог.

## Внутренние контракты Order Service

`order-service` обращается к другим сервисам через OpenFeign:

- `GET /internal/users/{id}` — получить пользователя;
- `GET /internal/events/{id}` — получить краткую информацию о мероприятии;
- `POST /internal/events/reservations` — зарезервировать билеты;
- `DELETE /internal/events/reservations/{reservationId}` — отменить резерв.

Общие DTO лежат в модуле `shared-contracts`. Feign является блокирующим, поэтому
его вызовы выполняются на планировщике Reactor `boundedElastic`. Вызовы защищены
`CircuitBreaker` и `Retry`; при недоступности зависимого сервиса наружу возвращается
HTTP 503 с понятным описанием ошибки.

## Проверка

```powershell
.\gradlew.bat test
```
