# Smart In-House DNS Cache
> A reactive, Redis-powered DNS cache with manual overrides, TTL control, and REST APIs.

---

## 📦 Features

- **Fast DNS Resolution** with configurable TTL
- **Manual Overrides** for any domain (custom IP + TTL)
- **Batch API support** for resolving or deleting many domains
- **Redis-backed** for centralized cache sharing
- **View, update, or delete** any cached domain
- Built with **Spring WebFlux** – reactive, non-blocking, and lightweight

---

## Try It Locally (Postman / cURL)

### 🛠 Requirements
- Java 17+
- Redis (localhost:6379)
- Maven

Run the App in IDE of your choice

```bash
mvn clean package
mvn spring-boot:run

TRY: API Endpoints

| Method | Endpoint                              | Description                        |
|--------|---------------------------------------|------------------------------------|
| GET    | `/api/dns/resolve?domain=example.com` | Resolve and cache a domain         |
| POST   | `/api/dns/cache`                      | Create a manual DNS entry          |
| GET    | `/api/dns/cache/{domain}`             | Fetch a cached DNS record          |
| GET    | `/api/dns/cache`                      | List all cached DNS records        |
| PATCH  | `/api/dns/cache/{domain}/ttl`         | Update TTL of a cached record      |
| DELETE | `/api/dns/cache/{domain}`             | Delete a cached record             |
| DELETE | `/api/dns/cache`                      | Clear all cached records           |
| DELETE | `/api/dns/cache/manual`               | Delete only manual entries         |
| POST   | `/api/dns/cache/batch`                | Get multiple DNS records at once   |
| DELETE | `/api/dns/cache/batch`                | Delete multiple records at once    |

Or use provided postmen collection for demo purposes.