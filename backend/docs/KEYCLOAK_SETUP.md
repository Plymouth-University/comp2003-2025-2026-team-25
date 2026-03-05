# Complete Keycloak Setup Guide (QTrobot)

This guide walks you through setting up Keycloak from scratch so your backend can authenticate users and enforce roles.

---

## Table of contents

1. [Prerequisites](#1-prerequisites)
2. [Start Keycloak](#2-start-keycloak)
3. [Log in to the Admin Console](#3-log-in-to-the-admin-console)
4. [Create the realm `qtrobot`](#4-create-the-realm-qtrobot)
5. [Create the client `qtrobot-realm`](#5-create-the-client-qtrobot-realm)
6. [Copy the client secret into `.env`](#6-copy-the-client-secret-into-env)
7. [Create realm roles](#7-create-realm-roles)
8. [Create users and assign roles](#8-create-users-and-assign-roles)
9. [Test login and backend](#9-test-login-and-backend)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Prerequisites

- **Docker** and **Docker Compose** installed.
- Your project’s `backend` folder with `docker-compose.yml` and `.env` (see below for required variables).

Your `.env` should eventually contain:

```env
KEYCLOAK_SERVER_URL=http://keycloak:8080/
KEYCLOAK_REALM=qtrobot
KEYCLOAK_CLIENT_ID=qtrobot-realm
KEYCLOAK_CLIENT_SECRET=<you will get this in Step 6>
```

---

## 2. Start Keycloak

From the **backend** directory:

```bash
cd backend
docker-compose up -d keycloak
```

Wait 30–60 seconds for Keycloak to start. You can check logs:

```bash
docker-compose logs -f keycloak
```

When you see something like “Running the server in development mode” or “Keycloak ... started”, it’s ready.

- **Admin console (browser):** http://localhost:8080  
- **Backend (from host):** http://localhost:8000  

Inside Docker, the backend uses `KEYCLOAK_SERVER_URL=http://keycloak:8080/` so it can reach Keycloak by service name.

---

## 3. Log in to the Admin Console

1. Open **http://localhost:8080** in your browser.
2. You may see a Keycloak welcome page; click **Administration Console** (or go straight to http://localhost:8080/admin).
3. Log in with the admin user from `docker-compose.yml`:
   - **Username:** `admin`
   - **Password:** `admin`
4. You are in the **master** realm (default). All steps below are done in a new realm called `qtrobot`.

### 3.1 Create a permanent admin user (recommended)

Keycloak may show: *"You are logged in as a temporary admin user. To harden security, create a permanent admin account and delete the temporary one."*

Do this in the **master** realm (realm selector at top-left = **master**):

1. In the left menu click **Users**.
2. Click **Add user**.
3. **Username:** e.g. `keycloak-admin` (required). **Email** and **First / Last name** optional.
4. Click **Create**.
5. Open the **Credentials** tab.
6. Set **Password** and **Password confirmation** (use a strong password).
7. Turn **Temporary** **OFF** (so they are not forced to change it on first login).
8. Click **Set password** and confirm.
9. Open the **Role mapping** tab.
10. Click **Assign role**.
11. Use **Filter by clients** and select **realm-management** (or **Filter by realm roles** and look for master-realm roles).
12. Assign **manage-realm** (or **realm-admin**). For full admin, assign all **realm-management** client roles, e.g. `manage-realm`, `manage-users`, `manage-clients`, `view-realm`, `view-users`, `view-clients`.
13. Click **Assign**.

Log out and log in again with the new username and password. For production, you can then disable or delete the temporary `admin` user in **Users**.

---

## 4. Create the realm `qtrobot`

A realm is a space for users, roles, and clients. Your backend is configured to use the realm `qtrobot`.

1. In the top-left dropdown, click the current realm name (**master**).
2. Click **Create realm**.
3. **Realm name:** `qtrobot` (must match `KEYCLOAK_REALM` in `.env`).
4. Leave other options as default (or enable **Enabled** if it’s not already).
5. Click **Create**.

You are now in the **qtrobot** realm. Confirm the realm selector at the top-left shows **qtrobot**.

---

## 5. Create the client `qtrobot-realm`

The “client” is your application (backend/frontend) that will use Keycloak for login. Your backend expects client ID `qtrobot-realm`.

1. In the left menu, go to **Clients**.
2. Click **Create client**.

### 5.1 General settings

- **Client type:** OpenID Connect  
- **Client ID:** `qtrobot-realm` (must match `KEYCLOAK_CLIENT_ID` in `.env`)  
- **Name:** e.g. `QTrobot App` (optional)  
- Click **Next**.

### 5.2 Capability config

- **Client authentication:** **ON** (so you get a client secret for the backend).
- **Authorization:** OFF (unless you use Keycloak’s fine-grained authz).
- **Authentication flow:**
  - **Standard flow:** ON (for browser redirect login).
  - **Direct access grants:** ON (so you can get tokens with username/password for testing or non-browser clients).
  - **Implicit flow:** OFF (deprecated).
- Click **Next**.

### 5.3 Login settings

- **Root URL:** leave empty or set e.g. `http://localhost:3000` if you have a frontend.
- **Home URL:** optional.
- **Valid redirect URIs:**  
  Add the URLs where Keycloak can redirect after login. For local dev, for example:
  - `http://localhost:3000/*`
  - `http://localhost:8000/*`
  - `http://127.0.0.1:3000/*`
  Add your real frontend URL in production.
- **Valid post logout redirect URIs:** optional (e.g. same as above).
- **Web origins:** for dev you can use `*` or e.g. `http://localhost:3000`, `http://localhost:8000` (no trailing slash).
- Click **Save**.

You are now on the client’s configuration page.

---

## 6. Copy the client secret into `.env`

The backend needs the client secret to validate tokens (and to request tokens if you use client credentials).

1. Open the client **qtrobot-realm** (Clients → **qtrobot-realm**).
2. Go to the **Credentials** tab.
3. Copy the **Secret** value.
4. Open `backend/.env` and set:

```env
KEYCLOAK_CLIENT_SECRET=<paste the secret here>
```

Example (your secret will be different):

```env
KEYCLOAK_SERVER_URL=http://keycloak:8080/
KEYCLOAK_REALM=qtrobot
KEYCLOAK_CLIENT_ID=qtrobot-realm
KEYCLOAK_CLIENT_SECRET=Ukd6Hp8r6202jYqGHc50zaJTslpiS6W5
```

If you run the backend in Docker, restart it so it picks up the new env:

```bash
docker-compose up -d backend
```

---

## 7. Create realm roles

---

## 7.1 (Optional) Add Google identity provider

If you would like users to log in via Google instead of managing local
credentials, Keycloak can broker the login. Follow these steps **after
creating the realm** but before adding users:

1. In the left-hand menu choose **Identity Providers**.
2. Click **Add provider** and select **Google** from the dropdown.
3. In parallel open the [Google Cloud Console credentials page](https://console.cloud.google.com/apis/credentials)
   and create a new **OAuth 2.0 Client ID** (application type **Web
   application**).
   - Add Keycloak’s broker endpoint as a redirect URI, e.g.
     `http://localhost:8080/realms/qtrobot/broker/google/endpoint`.
   - Copy the **Client ID** and **Client Secret** from Google.
4. Back in Keycloak, paste the Google values into the provider form.
   Optionally adjust other settings (default scope, sync mode, first
   login flow) then click **Save**.
5. Optionally, edit the **Mappers** for the provider to control which
   Google attributes end up in the Keycloak token (email, name, picture,
   etc.).

Once this is done, the Keycloak login page will show a “Log in with
Google” button. You can also automatically send users to the Google flow by
adding `kc_idp_hint=google` to the authorization URL.  The backend already
includes example redirect endpoints (`/auth/login` and `/auth/google`).

You may want to update `backend/keycloak/realm-export.json` to capture the
provider configuration; a template snippet is added there in the repo.

---

## 7. Create realm roles

Realm roles are in the JWT and your backend uses them (e.g. `admin`, `user`) for authorization.

1. In the left menu, go to **Realm roles**.
2. Click **Create role**.
3. **Role name:** `user` → **Save**.
4. Click **Create role** again.
5. **Role name:** `admin` → **Save**.

You should see both **user** and **admin** under Realm roles.

---

## 8. Create users and assign roles

Create at least one user for testing (e.g. an admin and a normal user).

### 8.1 Create user “admin”

1. Go to **Users** in the left menu.
2. Click **Add user**.
3. **Username:** `keycloak_admin` (required). **Email** / **First name** / **Last name:** optional.
4. Click **Create**.

Set password:

1. Open the **Credentials** tab for this user.
2. **Password:** e.g. `admin123` (or a strong password).  
   **Password confirmation:** same value.
3. Turn **Temporary** OFF if you don’t want “change password on first login”.
4. Click **Set password** and confirm.

Assign realm role:

1. Open the **Role mapping** tab.
2. Click **Assign role**.
3. Filter by **Filter by realm roles** (or leave default).
4. Select **admin** (and optionally **user**).
5. Click **Assign**.

### 8.2 Create user “user” (normal user)

1. **Users** → **Add user**.
2. **Username:** `user` (required). **Email:** optional.
3. Click **Create**.
4. **Credentials** tab: **Password** and **Password confirmation:** `password`, **Temporary** OFF, **Set password**.
5. **Role mapping** → **Assign role** → select **user** only → **Assign**.

You now have:

- **keycloak_admin** / `password` → role: `admin`.
- **user** / `password` → role: `user`.

---

## 9. Test login and backend

**Quick test (PowerShell):** From the `backend` folder run:
```powershell
.\scripts\test-auth.ps1 -Username "user" -Password "password"
```
The script reads `KEYCLOAK_CLIENT_SECRET` from `backend\.env`, gets a token, and calls `/protected`, `/user/me`, and `/user/roles`.

### 9.1 Get an access token (command line)

Use **Direct access** (password) grant. Example for user `user` / `password`:

```bash
curl -X POST "http://localhost:8080/realms/qtrobot/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=user" \
  -d "password=user123" \
  -d "grant_type=password" \
  -d "client_id=qtrobot-realm" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

Use the same URL and body for the **admin** user (with admin’s password).  
Response will contain `access_token`. Copy it (without quotes).

### 9.2 Call a protected endpoint

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8000/protected
```

You should get a JSON response like: `{"message":"Hello, user! You have accessed a protected resource."}`.

### 9.3 Call the admin-only endpoint

- With **admin** token:  
  `curl -H "Authorization: Bearer ADMIN_TOKEN" http://localhost:8000/admin/summary`  
  → should return 200 and a welcome message.
- With **user** token:  
  same URL → should return **403 Forbidden** (user doesn’t have `admin` role).

### 9.4 Get current user and roles

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8000/user/me
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8000/user/roles
```

This confirms authentication and role data from Keycloak.

---

## 10. Troubleshooting

| Problem | What to check |
|--------|----------------|
| Cannot open http://localhost:8080 | Keycloak container running? `docker-compose ps` and `docker-compose logs keycloak`. |
| Backend returns 401 “Could not validate” | Realm name and client ID in `.env` match Keycloak (`qtrobot`, `qtrobot-realm`). Backend uses `KEYCLOAK_SERVER_URL=http://keycloak:8080/` when running in Docker. |
| 401 after token expired | Tokens expire (e.g. 5 min). Get a new token and retry. |
| 403 on `/admin/summary` | User must have realm role **admin** in Keycloak (Role mapping for that user). |
| “Invalid redirect URI” when using browser login | Add the exact redirect URI (e.g. `http://localhost:3000/callback`) to **Valid redirect URIs** for client `qtrobot-realm`. |
| Backend can’t reach Keycloak (e.g. JWKS error) | From backend container, URL must be `http://keycloak:8080/` (service name), not `localhost`. Ensure backend and Keycloak are on the same Docker network (same `docker-compose`). |

### Useful URLs (realm: qtrobot)

- Admin console: http://localhost:8080/admin (realm: master for admin, then switch to qtrobot).
- OpenID configuration: http://localhost:8080/realms/qtrobot/.well-known/openid-configuration
- Token endpoint: http://localhost:8080/realms/qtrobot/protocol/openid-connect/token
- Login page (redirect flow): http://localhost:8080/realms/qtrobot/protocol/openid-connect/auth?client_id=qtrobot-realm&response_type=code&redirect_uri=...&scope=openid

---

## 11. Production checklist

Before going to production, consider:

- [ ] **Permanent Keycloak admin** – In **master** realm, create a new admin user, assign realm-management roles, then disable or delete the temporary `admin` user.
- [ ] **Strong passwords** – Use strong passwords for Keycloak admin and application users; consider a password policy in **Realm settings → Security defenses**.
- [ ] **HTTPS** – Run Keycloak and your app over HTTPS; set **Valid redirect URIs** and **Web origins** to your real frontend URL (e.g. `https://yourapp.com/*`).
- [ ] **Client secret** – Keep `KEYCLOAK_CLIENT_SECRET` in a secure secret store (e.g. env vars or vault), not in source control.
- [ ] **Token settings** – In **Realm settings → Tokens** you can adjust access token lifespan and refresh token settings if needed.

---

## 12. Sharing your Keycloak config via Docker

If you want teammates to get your exact Keycloak setup (realm, roles, users, clients) just by running Docker:

1. **Export your realm from your local Keycloak**
   - In the Keycloak Admin Console (top-left realm selector), select the `qtrobot` realm.
   - Go to **Realm settings → General**.
   - Click **Partial export** or **Export** (depending on version).
   - Choose:
     - **Export groups and roles**: ON
     - **Export clients**: ON
     - **Export users**: ON (if you want test users shared)
   - Download the JSON file and name it, for example, `qtrobot-realm-export.json`.

2. **Place the export in the project**
   - Create a folder `backend/keycloak` (if it doesn’t exist).
   - Save the exported file there, e.g. `backend/keycloak/qtrobot-realm-export.json`.
   - Commit this file if you’re happy to share the realm configuration (including any test users you exported).

3. **How Docker picks it up**
   - The `keycloak` service in `backend/docker-compose.yml` now mounts `./keycloak` into `/opt/keycloak/data/import` and starts Keycloak with `--import-realm`.
   - On first start, Keycloak will read any `*.json` files from `/opt/keycloak/data/import` and create the realm/clients/roles/users defined there.

4. **What your friend needs to do**
   - Pull your repo (with the `backend/keycloak/*.json` file included).
   - From the `backend` folder run:
     - `docker compose pull`
     - `docker compose up -d keycloak backend`
   - After Keycloak starts, they will have the same `qtrobot` realm configuration you exported.

---

## Summary checklist

- [ ] Keycloak running (`docker-compose up -d keycloak`).
- [ ] Logged in to Admin Console (admin / admin).
- [ ] Realm **qtrobot** created.
- [ ] Client **qtrobot-realm** created, with client authentication ON, direct access grants ON.
- [ ] Valid redirect URIs and Web origins set (e.g. localhost for dev).
- [ ] Client **secret** copied to `KEYCLOAK_CLIENT_SECRET` in `backend/.env`.
- [ ] Realm roles **user** and **admin** created.
- [ ] At least one user created, password set, role **user** or **admin** assigned.
- [ ] Token obtained via `/realms/qtrobot/.../token` and backend `/protected` and `/admin/summary` tested with that token.

After this, your backend can authenticate users and enforce roles using Keycloak.
