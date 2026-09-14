# CafeGestión360 — DSY1107 Desarrollo Cloud Native I — EP1

Sistema de gestión para una cafetería (menú, inventario, pedidos, clientes, pagos,
empleados, proveedores y reportes) que hoy funciona en papel. Arquitectura:
React + MSAL → Azure Entra ID (IDaaS) → AWS API Gateway → BFF (Spring Cloud
Gateway) → 8 microservicios Spring Boot → PostgreSQL.

> **Nota:** el enunciado y la pauta del EP1 piden explícitamente Angular. El
> cambio a React se hizo porque el estudiante confirmó por escrito con el
> docente que estaba autorizado — si tú lees esto y no tienes esa autorización,
> no lo copies tal cual, vuelve a la versión Angular.

---

## 0. Qué se evalúa realmente (para priorizar tu tiempo)

Según la pauta oficial del EP1, la nota se calcula sobre 2 indicadores:

| Indicador | Ponderación | Qué revisa |
|---|---|---|
| MSAL en el frontend | 60% | Login/logout funcionan, se obtienen los tokens, se adjuntan como Bearer a las llamadas al backend (en React: hook `useApiClient`, ver sección 6) |
| BFF valida el JWT | 40% | El backend valida issuer, audience y firma del token, autoriza por rol, responde códigos correctos |

El despliegue en AWS (EC2 + API Gateway) es la capa "de vitrina" que pide el
enunciado general, pero no es lo que más pesa en la pauta — ver sección 9 para
la versión rápida de eso.

---

## 1. Correrlo AHORA MISMO con Docker (la forma más rápida y confiable)

Todo el stack (Postgres + 8 microservicios + BFF + frontend) está dockerizado
y por defecto corre en modo `noauth` (sin Azure configurado todavía, JWT sin
validar) — es la forma más rápida de dejarlo funcionando en un computador
nuevo, por ejemplo el del instituto.

**Requisito único: Docker Desktop instalado y corriendo.**

```bash
# parado en la raíz del repo (donde está este README y docker-compose.yml)
docker compose up --build -d
```

La primera vez tarda varios minutos (descarga imágenes base y compila los 9
módulos Maven + el frontend). Cuando termine:

- Frontend: http://localhost:4200
- BFF: http://localhost:8080/api/productos
- Cada microservicio también queda expuesto individualmente (8081-8088) y
  Postgres en 5432, aunque en modo `noauth` no se usa (cada microservicio usa
  su propia base H2 embebida, persistida en un volumen Docker).

Comandos útiles:
```bash
docker compose ps              # ver estado de cada contenedor
docker compose logs -f bff-gateway   # logs de un servicio en particular
docker compose down            # apagar todo (los datos quedan en volúmenes)
docker compose down -v         # apagar y borrar también los datos
```

**El menú viene vacío en una base nueva.** Para cargar productos de prueba,
con el stack arriba:
```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Latte Vainilla","descripcion":"Espresso con leche vaporizada y jarabe de vainilla","precio":3200,"categoria":"Bebidas calientes"}'
```
(repite con los productos que quieras — en modo `noauth` no hace falta token).

Si ves el error "ports are not available" al levantar el stack, es porque ya
tienes el backend/frontend corriendo manualmente (sección 3) en esos mismos
puertos — ciérralos primero.

---

## 2. Requisitos previos

- **Docker Desktop** — la forma recomendada de correr el proyecto (sección 1).
- Java 17 y Node.js 18+ — solo si prefieres el modo manual sin Docker (sección 3).
- Cuenta de Azure for Students activa (`portal.azure.com`) — solo para el flujo
  con autenticación real (sección 4).
- Git y una cuenta de GitHub.

---

## 3. Modo manual sin Docker (alternativa, perfil `noauth`)

Si no tienes Docker a mano, puedes correr todo directo con Maven/npm. Deja
todos los endpoints abiertos, sin pedir JWT. **Nunca debe quedar activo en la
entrega final.**

### 3.1 Backend en modo noauth (con H2, sin Docker, sin instalar Maven)

El proyecto incluye **Maven Wrapper** (`mvnw` / `mvnw.cmd`): no necesitas
instalar Maven, solo Java 17.

Atajo con un solo comando (abre una ventana por servicio):
```bash
cd cafeteria-backend/cafeteria-backend
# Windows (doble click o desde cmd/PowerShell):
start-noauth.bat
# Git Bash / Mac / Linux:
./start-noauth.sh
```

O manualmente, paso a paso:
```bash
cd cafeteria-backend/cafeteria-backend
./mvnw clean install
export SPRING_PROFILES_ACTIVE=noauth
./mvnw -f ms-productos/pom.xml spring-boot:run &
# ... el resto de los microservicios igual
./mvnw -f bff-gateway/pom.xml spring-boot:run
```
En Windows/Git Bash, si `&` no te deja varios procesos en la misma terminal,
abre una terminal nueva por cada comando (o usa `start-noauth.bat`).

### 3.2 Frontend en modo noauth
```bash
cd cafeteria-frontend/cafeteria-frontend
npm install
copy .env.example .env    # Windows: copy · Git Bash/Mac/Linux: cp
# En .env, deja VITE_AUTH_DISABLED=true
npm run dev
```
Abre `http://localhost:4200` — salta el login directo al menú.

### 3.3 Cuando tengas Azure configurado (sección 4)
1. Backend: no exportes `SPRING_PROFILES_ACTIVE` (o ponlo en cualquier otro
   valor), y exporta `AZURE_ISSUER_URI` / `AZURE_AUDIENCE` reales.
2. Frontend: en `.env`, cambia `VITE_AUTH_DISABLED=false` y completa
   `VITE_AZURE_CLIENT_ID` / `VITE_AZURE_TENANT_ID` con tus valores reales.
3. Antes de subir a GitHub, revisa que ningún `.env` real quede con `noauth`
   o `AUTH_DISABLED=true` — la pauta exige que el JWT SÍ se valide.

---

## 4. Configurar Azure Entra ID (IDaaS) — ~45 min

Necesitas **2 App Registrations** dentro del mismo Tenant: una para el backend
(API) y otra para el frontend (SPA).

### 4.1 Crear el Tenant (si no lo tienes)
1. Entra a `portal.azure.com` con tu cuenta académica.
2. Busca el recurso **Microsoft Entra External ID** y créalo.
3. Guarda el **Directory (tenant) ID**.

### 4.2 Registrar el Backend (API)
1. En tu Tenant → **Entra ID → Registros de aplicaciones → Nuevo registro**.
2. Nombre: `cafeteria-backend`. Tipo de cuenta: "Solo cuentas de este directorio
   organizativo".
3. Guarda el **Application (client) ID**.
4. Ve a **Expose an API**:
   - Application ID URI: acepta el propuesto o define `api://cafeteria-backend`.
   - Agrega los scopes: `pedidos.read` y `pedidos.write`.
5. Ve a **App roles** y crea roles (llegan en el claim `roles` del JWT, usado
   por `@PreAuthorize` en el backend): `ADMIN`, `BARISTA`, `CAJERO`.

### 4.3 Registrar el Frontend (SPA)
1. Nuevo registro → Nombre: `cafeteria-frontend`.
2. Tipo de plataforma: **SPA (Single-page application)**.
3. Redirect URI: `http://localhost:4200`.
4. Guarda el **Application (client) ID** (distinto al del backend).
5. **API permissions → Add a permission → APIs de mi organización** →
   `cafeteria-backend` → selecciona `pedidos.read` y `pedidos.write`.
6. **Grant admin consent**.

### 4.4 Asignarte un rol a ti mismo (para poder probar POST/PUT/DELETE)
Entra ID → Enterprise applications → `cafeteria-backend` → **Users and
groups → Add user/group** → tu usuario → rol `ADMIN`.

### 4.5 Datos que debes tener anotados al terminar esta sección
```
TENANT_ID            = ...
BACKEND_CLIENT_ID    = ...
BACKEND_APP_ID_URI   = api://cafeteria-backend
FRONTEND_CLIENT_ID   = ...
```

---

## 5. Backend local con Azure real (Postgres, sin `noauth`)

### 5.1 Base de datos
```bash
cd cafeteria-backend/cafeteria-backend
docker compose up -d postgres
```
Levanta PostgreSQL en `localhost:5432` (usuario/clave `cafeteria`/`cafeteria`)
con un esquema separado por microservicio (`init-db/01-schemas.sql`).

### 5.2 Variables de entorno
```bash
export AZURE_ISSUER_URI="https://login.microsoftonline.com/<TENANT_ID>/v2.0"
export AZURE_AUDIENCE="api://cafeteria-backend"
```

### 5.3 Compilar y levantar
```bash
./mvnw clean install
./mvnw -f ms-productos/pom.xml spring-boot:run &
# ... resto de microservicios ...
export FRONTEND_ORIGIN="http://localhost:4200"
./mvnw -f bff-gateway/pom.xml spring-boot:run
```
Sin token, `curl -i http://localhost:8080/api/productos` debe responder 401.

---

## 6. Frontend local con Azure real

```bash
cd cafeteria-frontend/cafeteria-frontend
npm install
copy .env.example .env
```
Edita `.env`: `VITE_AZURE_CLIENT_ID` y `VITE_AZURE_TENANT_ID` (sección 4.5),
`VITE_AUTH_DISABLED=false`. Luego `npm run dev` y abre `http://localhost:4200`.
Click en "Iniciar sesión" → Azure Entra ID → vuelves autenticado → las
llamadas al BFF llevan el token adjunto automáticamente (hook `useApiClient`,
`src/services/apiClient.js`). **Esto es lo que mide el 60% de la pauta.**

---

## 7. Docker — referencia completa

El `docker-compose.yml` de la raíz define:

- `postgres` — base para el modo con Azure real (sección 5).
- `ms-productos` … `ms-reportes` (8) y `bff-gateway` — cada uno se construye
  con [cafeteria-backend/cafeteria-backend/Dockerfile](cafeteria-backend/cafeteria-backend/Dockerfile)
  (multi-stage: compila con Maven, corre con JRE Alpine), parametrizado por
  `build.args.MODULE`. Todos corren en `SPRING_PROFILES_ACTIVE=noauth` por
  defecto, con su propia base H2 en un volumen nombrado.
- `frontend` — se construye con
  [cafeteria-frontend/cafeteria-frontend/Dockerfile](cafeteria-frontend/cafeteria-frontend/Dockerfile)
  (build con Node + `vite build`, se sirve con `vite preview`).

Para cambiar a modo con Azure real dentro de Docker **no edites
`docker-compose.yml`**: copia `.env.example` a `.env` en la raíz y completa
ahí `SPRING_PROFILES_ACTIVE=` (vacío), `AZURE_ISSUER_URI`, `AZURE_AUDIENCE`,
`VITE_AUTH_DISABLED=false`, `VITE_AZURE_CLIENT_ID`, `VITE_AZURE_TENANT_ID` y
`VITE_API_SCOPES` con tus datos reales — Docker Compose lo lee automático.
Ese `.env` nunca se sube a git (ver `.gitignore` raíz). Después:
```bash
docker compose up --build -d
```

**CORS y rutas públicas:** el CORS se configura dentro de la cadena de Spring
Security del BFF (`bff-gateway/.../config/CorsConfig.java`), no como un
filtro aparte — un `CorsWebFilter` separado no alcanza a agregar los headers
cuando Security corta la respuesta con 401/403, y el navegador termina
bloqueando hasta los errores legítimos como si fueran de CORS. Por diseño,
`GET /api/productos` es público (un cliente ve el menú sin loguearse); crear/
editar/eliminar productos y el resto de las rutas del staff siguen exigiendo
JWT + rol.

---

## 8. Subir a GitHub

Todo el proyecto vive en **un solo repositorio**:
```bash
# parado en la raíz (donde está este README)
git init
git add .
git commit -m "CafeGestion360: backend (8 microservicios + BFF) y frontend React con MSAL"
git branch -M main
git remote add origin https://github.com/<tu-usuario>/<tu-repo>.git
git push -u origin main
```

Los `.gitignore` (raíz, backend y frontend) ya excluyen `target/`,
`node_modules/`, las bases H2 locales (`data/`) y cualquier `.env` real.

**Importante:** antes de subir, revisa que ningún `.env` real quede en el
commit y que solo dejes `.env.example` con placeholders — nunca subas
secretos de producción a un repo público. Copia el link del repo a AVA y
envíalo también al correo del docente (según el enunciado).

---

## 9. ¿Hay que migrar esto a la nube? (versión rápida, si te queda tiempo)

El enunciado general describe el backend "desplegado en instancias EC2 y
protegido por AWS API Gateway", pero la pauta de evaluación **no** pide una URL
en producción como entregable — pide el código fuente. Trátalo como algo
deseable, no bloqueante. Si te sobra tiempo, la versión mínima es:

1. **EC2**: crea una instancia (Ubuntu, t2.micro/t3.micro), instala Docker, y
   corre `docker compose up --build -d` directo desde ahí (mismo repo
   clonado) — o, sin Docker, instala Java 17 y copia los `.jar` generados por
   `mvn package` de cada microservicio y del BFF (`java -jar nombre.jar`, con
   `nohup ... &` o systemd). Abre en el Security Group los puertos 8080-8088.
2. **RDS**: crea una instancia PostgreSQL (free tier), y cambia `DB_URL`,
   `DB_USER`, `DB_PASSWORD` como variables de entorno en la EC2 apuntando a esa
   RDS en vez de a tu Docker local.
3. **AWS API Gateway**: crea un HTTP API con una ruta `ANY /{proxy+}` que
   apunte a la IP pública (o DNS) del BFF en el puerto 8080, y configura CORS
   con el origen de tu frontend desplegado.
4. Crea un `.env.production` en el frontend con `VITE_API_BASE_URL` apuntando
   a la URL del API Gateway, y compila con `npm run build`.

Si el tiempo no alcanza para esto, prioriza sin culpa las secciones 1-6: un
sistema que corre local (o en Docker) con el flujo de seguridad completo y
bien explicado en el README vale más, frente a la pauta, que un despliegue a
medio hacer en AWS.

### 9.1 Cambiar la IP pública del EC2 (si la instancia se reinicia)

Una instancia EC2 sin Elastic IP cambia de IP pública cada vez que se
detiene y se vuelve a iniciar. Cuando eso pase, en la instancia (por SSH):

```bash
# 1. Actualizar el .env con la IP nueva
sed -i 's|<IP_VIEJA>|<IP_NUEVA>|g' .env
cat .env  # verificar que cambió

# 2. Regenerar el certificado SSL autofirmado para la nueva IP
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/ssl/private/cafeteria.key \
  -out /etc/ssl/certs/cafeteria.crt \
  -subj "/CN=<IP_NUEVA>"
sudo systemctl reload nginx

# 3. Reconstruir el frontend con la nueva IP (VITE_API_BASE_URL se hornea
#    en el build, no es una variable de entorno normal de contenedor)
docker compose stop frontend
docker compose rm -f frontend
docker compose build --no-cache frontend
docker compose up -d frontend
```

Para evitar este problema de raíz, lo correcto es asignarle una **Elastic
IP** a la instancia (gratis mientras esté asociada a una instancia corriendo)
para que la IP pública quede fija.

---

## 10. Estructura del repositorio

```
docker-compose.yml            # stack completo: postgres + 8 ms + bff + frontend

cafeteria-backend/cafeteria-backend/
  pom.xml                     # padre Maven multi-modulo (fix: repackage bindeado en package)
  Dockerfile                  # generico, parametrizado por build-arg MODULE
  ms-productos/ ... ms-reportes/   # 8 microservicios (mismo patron cada uno)
  bff-gateway/                # Spring Cloud Gateway + validacion JWT
  docker-compose.yml          # solo Postgres (uso puntual, ver seccion 5)
  init-db/01-schemas.sql      # esquemas por microservicio
  start-noauth.bat/.sh        # atajo modo manual sin Docker

cafeteria-frontend/cafeteria-frontend/
  Dockerfile
  src/auth/authConfig.js       # configuracion MSAL (clientId, scopes, authority)
  src/auth/ProtectedRoute.jsx  # guard de rutas (equivalente a MsalGuard)
  src/services/apiClient.js    # hook useApiClient (equivalente a MsalInterceptor)
  src/pages/                   # Store, Login, Pedidos, Productos, etc.
  public/productos/            # fotos de producto (<nombre-slug>.jpg, ver ProductVisual.jsx)
  public/banner/hero.jpg       # foto de fondo del banner principal
  .env.example                 # variables VITE_* (copiar a .env con tus datos)
```
