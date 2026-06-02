# Guia de despliegue local con Docker

Esta configuracion levanta el MVP completo en tres contenedores:

- `servify_db`: PostgreSQL 16 con el script `init.sql`.
- `servify_backend`: API Spring Boot en Java 21.
- `servify_frontend`: React compilado y servido por Nginx.

## Variables de entorno

Copiar el ejemplo y ajustar credenciales si hace falta:

```powershell
Copy-Item .env.example .env
```

Valores principales:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: credenciales de PostgreSQL.
- `SERVIFY_GOOGLE_CLIENT_IDS`: audiences aceptadas por el backend para validar ID tokens de Google.
- `VITE_GOOGLE_CLIENT_ID`: client ID web que usa el frontend para mostrar el boton de Google.

## Levantar todo

```powershell
docker compose up --build
```

URLs:

- Frontend: `http://localhost:5173`
- Backend directo desde el host: `http://localhost:8082`
- PostgreSQL local: `localhost:5432`

El frontend usa `/api/v1` y Nginx lo proxyea internamente al backend en `backend:8080`, asi que para probar la app usar siempre `http://localhost:5173`.

## Bajar contenedores

```powershell
docker compose down
```

Para borrar tambien los datos de PostgreSQL:

```powershell
docker compose down -v
```

Usar `-v` solo si queres reiniciar la base desde cero. Docker ejecuta `init.sql` solo cuando el volumen `servify_data` se crea por primera vez.

## Ver logs

```powershell
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

## Reconstruir solo una parte

```powershell
docker compose build backend
docker compose build frontend
docker compose up -d
```
