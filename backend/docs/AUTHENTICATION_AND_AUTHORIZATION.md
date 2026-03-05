# Authentication & Authorization with Keycloak

This guide explains what is already implemented and what you can add for your QTrobot app.

---

## What You Already Have

| Feature | How it works |
|--------|----------------|
| **Login / tokens** | Users log in via Keycloak (OpenID Connect). Your backend accepts a **JWT** in the `Authorization: Bearer <token>` header. |
| **Authentication** | `get_current_user` in `main.py` verifies the JWT using Keycloak’s public keys (JWKS) and returns the token payload (e.g. `preferred_username`, `sub`, `realm_access`). |
| **Protected routes** | Any endpoint that uses `Depends(get_current_user)` requires a valid token. Example: `/protected`, `/user/me`, `/robot/say`. |
| **Authorization (roles)** | Realm roles from Keycloak are in the token. `ensure_role(current_user, "admin")` checks for a role; `/admin/summary` is admin-only. |
| **Public routes** | `/` and `/health` do not require a token. |

So: **Keycloak = who can log in and which roles they have; your FastAPI backend = checks the token and enforces who can call which endpoint.**

---

## 1. Keycloak Setup (so others can log in)

Your `.env` expects a realm `qtrobot` and client `qtrobot-realm`. Do this once in the Keycloak Admin UI (e.g. http://localhost:8080 when Keycloak is running).

### 1.1 Create realm `qtrobot`

1. Open Keycloak Admin Console → **Create realm**.
2. Name: `qtrobot` → Create.

### 1.2 Create client `qtrobot-realm`

1. **Clients** → **Create client**.
2. **Client ID**: `qtrobot-realm`.
3. **Client authentication**: ON (confidential).
4. **Valid redirect URIs**: add your frontend URLs (e.g. `http://localhost:3000/*`, `https://yourapp.com/*`).
5. **Web origins**: same as above or `*` for dev.
6. Save → **Credentials** tab: copy the **Secret** into `.env` as `KEYCLOAK_CLIENT_SECRET`.

### 1.3 Create roles

1. **Realm roles** → **Create role**.
2. Create at least: `admin`, `user` (or `manager` if you need it).

### 1.4 Create users and assign roles

1. **Users** → **Add user** (username, email, etc.) → Save.
2. **Credentials** tab: set a password.
3. **Role mapping**: assign realm roles (e.g. `user` or `admin`).

After this, users can log in via Keycloak and get a JWT that your backend will accept.

---

## 2. What You Can Implement Next

### 2.1 Reusable “require role” dependency

You already have `ensure_role()`. You can turn it into a dependency so any route can require a role in one line:

```python
# In main.py, add:
def require_role(required_role: str):
    def role_checker(current_user: dict = Depends(get_current_user)):
        ensure_role(current_user, required_role)
        return current_user
    return role_checker

# Usage: admin-only endpoint
@app.get("/admin/dashboard")
def admin_dashboard(current_user: dict = Depends(require_role("admin"))):
    return {"message": "Admin dashboard", "user": current_user.get("preferred_username")}
```

### 2.2 Client roles (optional)

If you define **client roles** in Keycloak (e.g. on `qtrobot-realm`: `robot-operator`, `viewer`), they appear in the token under `resource_access["qtrobot-realm"]["roles"]`. You can add a helper and use it the same way:

```python
def get_client_roles(current_user: Dict, client_id: str = KEYCLOAK_CLIENT_ID) -> List[str]:
    return (
        current_user.get("resource_access", {}).get(client_id, {}).get("roles", [])
        or []
    )

def ensure_client_role(current_user: Dict, required_role: str) -> None:
    roles = get_client_roles(current_user)
    if required_role not in roles:
        raise HTTPException(status_code=403, detail=f"Requires client role '{required_role}'")
```

Then protect endpoints with `ensure_client_role(current_user, "robot-operator")` (or a dependency built on it).

### 2.3 Frontend login (OIDC)

- **Option A – Redirect flow**: Frontend redirects to Keycloak login; Keycloak redirects back with an authorization code; frontend exchanges it for tokens (access + optional refresh). Use Keycloak’s OIDC endpoints (e.g. `/realms/qtrobot/protocol/openid-connect/auth` and `.../token`).
- **Option B – Keycloak JS adapter**: Use a library such as `keycloak-js` so the frontend handles redirects and token refresh; then send the access token in `Authorization: Bearer ...` to your FastAPI backend.

Your backend does not need to change: it only validates the JWT and reads roles.

### 2.4 QR-code login (`/auth/qr-login`)

Right now this is a stub. You can implement it by:

1. **Frontend**: When the user is logged in, generate a short-lived code (or QR payload) and show it as a QR code.
2. **Robot**: Scans QR, sends the code to `/auth/qr-login`.
3. **Backend**: Validates the code, maps it to a user/session (e.g. in cache or DB), and returns a token or session id for the robot to use for later API calls (or triggers a “device login” in Keycloak if you use a flow for that).

This gives “login on the robot via QR” while still using Keycloak for user identity.

### 2.5 More granular permissions

- **Resource-level**: e.g. “user X can only control robot Y”. Store mappings in your DB; in the endpoint, use `current_user["sub"]` (Keycloak user id) and check against your data.
- **Combining realm + client roles**: Use `ensure_role` for broad access (e.g. “admin”) and `ensure_client_role` for app-specific roles (e.g. “robot-operator”).

---

## 3. Quick reference

| Goal | Where | What to do |
|------|--------|------------|
| Only logged-in users | Backend | `current_user: dict = Depends(get_current_user)` |
| Only certain role (realm) | Backend | `ensure_role(current_user, "admin")` or `Depends(require_role("admin"))` |
| Who is logged in | Backend | `current_user["preferred_username"]`, `current_user["sub"]` |
| User’s roles | Backend | `current_user.get("realm_access", {}).get("roles", [])` |
| Login UI / get token | Frontend | Keycloak login page or Keycloak JS (OIDC) |
| Create users/roles | Keycloak Admin | Realm: Users, Realm roles, Client roles |

If you tell me which of these you want first (e.g. “reusable role dependency” or “frontend login”), I can give the exact code changes in your repo.


---

## 2.6 Third‑party identity providers (Google)

Keycloak is already acting as an OpenID Connect provider for your
application. You can add additional login methods (for example "Login with
Google") by configuring an **identity provider** within the same realm.
Once that is set up, the token your backend receives looks exactly the same
as for normal users – there is **no change to the FastAPI code**. The
backend simply validates the JWT issued by Keycloak.

### a) Keycloak configuration

1. In the Keycloak Admin Console select your `qtrobot` realm.
2. In the left menu click **Identity Providers**.
3. Click **Add provider** and choose **Google**.
4. Open the [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
   and create an **OAuth 2.0 Client ID** (type: Web application).
   - Add your Keycloak URL as an **authorized redirect URI**, e.g.:
     `http://localhost:8080/realms/qtrobot/broker/google/endpoint`.
   - Note the **Client ID** and **Client Secret** that Google gives you.
5. Back in Keycloak, fill in the **Client ID** and **Client Secret** fields
   under the provider settings. Leave the rest as defaults unless you need
   custom scopes/claims. Save.
6. (Optional) adjust the **First Login Flow** and mapper settings if you
   want to automatically assign realm roles to new Google users.

You can also export the realm (or the `identityProviders` portion) and save
it in `backend/keycloak/realm-export.json`. An example entry is already
included there.

### b) Frontend integration

- If you use a redirect‑based login flow, simply send the user to the
  Keycloak authorization endpoint with a hint: `...?kc_idp_hint=google`.
  We added two convenience endpoints in the backend:
  - `GET /auth/login` – generic redirect, accepts `?idp=<alias>` query
    parameter.
  - `GET /auth/google` – immediately starts the Google login flow.

  Example: from JavaScript `window.location = '/auth/google';` or
  `<a href="/auth/google">Login with Google</a>`.
- The rest of your frontend code (exchanging code for tokens, calling the
  backend with `Authorization: Bearer <token>`) remains unchanged.

### c) Backend changes

No code is required to **accept Google logins** – they appear as standard
Keycloak tokens. We only added helper routes to ease redirects (above).
Your `get_current_user` function and route dependencies work exactly the
same.

### d) Environment variables

The backend does not need Google-specific secrets because Keycloak handles
all communication with Google. If you prefer to template your realm export
with environment variables, you can use placeholders such as
`${KEYCLOAK_GOOGLE_CLIENT_ID}` and
`${KEYCLOAK_GOOGLE_CLIENT_SECRET}` in `realm-export.json`.


---
