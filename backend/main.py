import os
from typing import Dict, List

import jwt
import requests
from dotenv import load_dotenv
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from fastapi.responses import RedirectResponse
from jwt import PyJWKClient
from pydantic import BaseModel

# Load environment variables from your .env file
load_dotenv()

# --- Keycloak & Google Configuration ---
# These values are read from your .env file
# Internal URL used **inside Docker** (backend -> Keycloak)
KEYCLOAK_SERVER_URL = os.getenv("KEYCLOAK_SERVER_URL")
KEYCLOAK_REALM = os.getenv("KEYCLOAK_REALM")
KEYCLOAK_CLIENT_ID = os.getenv("KEYCLOAK_CLIENT_ID")

# Admin credentials used for Keycloak Admin REST API access
KEYCLOAK_ADMIN_USERNAME = os.getenv("KEYCLOAK_ADMIN_USERNAME")
KEYCLOAK_ADMIN_PASSWORD = os.getenv("KEYCLOAK_ADMIN_PASSWORD")

# Client secret will be set automatically later
KEYCLOAK_CLIENT_SECRET = None

# Public URL used by the browser / Swagger UI.
# Defaults to KEYCLOAK_SERVER_URL if not set.
KEYCLOAK_PUBLIC_URL = os.getenv("KEYCLOAK_PUBLIC_URL", KEYCLOAK_SERVER_URL)

# Google OAuth client ID used by the Android app (for ID token validation)
GOOGLE_CLIENT_ID = os.getenv("GOOGLE_CLIENT_ID")

# Secret used to mint short-lived app tokens when exchanging Google ID tokens.
# This lets the backend accept either a Keycloak JWT or an internal app JWT.
APP_JWT_SECRET = os.getenv("APP_JWT_SECRET")


# --- FastAPI Security and App Initialization ---

# This tells FastAPI how to find the token. It will look for an
# 'Authorization: Bearer <token>' header in incoming requests.
# IMPORTANT: Swagger/OpenAPI will call this URL from the browser,
# so it must be reachable from your machine (e.g. http://localhost:8080).
oauth2_scheme = OAuth2PasswordBearer(
    tokenUrl=f"{KEYCLOAK_PUBLIC_URL}realms/{KEYCLOAK_REALM}/protocol/openid-connect/token"
)

app = FastAPI(title="QTrobot Backend")


def get_keycloak_admin_access_token() -> str:
    """
    Obtain an access token for the Keycloak Admin REST API using the admin user.
    """
    if not KEYCLOAK_ADMIN_USERNAME or not KEYCLOAK_ADMIN_PASSWORD:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Keycloak admin credentials (KEYCLOAK_ADMIN_USERNAME / KEYCLOAK_ADMIN_PASSWORD) are not configured.",
        )

    token_url = f"{KEYCLOAK_SERVER_URL}realms/master/protocol/openid-connect/token"

    try:
        response = requests.post(
            token_url,
            data={
                "grant_type": "password",
                "client_id": "admin-cli",
                "username": KEYCLOAK_ADMIN_USERNAME,
                "password": KEYCLOAK_ADMIN_PASSWORD,
            },
            timeout=10,
        )
    except requests.RequestException as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to contact Keycloak token endpoint: {exc}",
        ) from exc

    if response.status_code != 200:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to obtain Keycloak admin token (status {response.status_code}): {response.text}",
        )

    data = response.json()
    token = data.get("access_token")
    if not token:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Keycloak admin token response did not contain an access_token.",
        )
    return token


def fetch_keycloak_client_secret_from_admin_api() -> str:
    """
    Use the Keycloak Admin REST API to retrieve the client secret for KEYCLOAK_CLIENT_ID.
    """
    if not KEYCLOAK_CLIENT_ID:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="KEYCLOAK_CLIENT_ID is not configured on the server.",
        )

    admin_token = get_keycloak_admin_access_token()
    headers = {"Authorization": f"Bearer {admin_token}"}

    # 1) Look up the internal client UUID by clientId
    clients_url = f"{KEYCLOAK_SERVER_URL}admin/realms/{KEYCLOAK_REALM}/clients"
    try:
        clients_resp = requests.get(
            clients_url,
            params={"clientId": KEYCLOAK_CLIENT_ID},
            headers=headers,
            timeout=10,
        )
    except requests.RequestException as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to contact Keycloak clients endpoint: {exc}",
        ) from exc

    if clients_resp.status_code != 200:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to query Keycloak clients (status {clients_resp.status_code}): {clients_resp.text}",
        )

    clients_data = clients_resp.json()
    if not clients_data:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Keycloak client with clientId '{KEYCLOAK_CLIENT_ID}' not found.",
        )

    client_id_internal = clients_data[0].get("id")
    if not client_id_internal:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Keycloak client lookup response did not contain an internal id.",
        )

    # 2) Fetch the client secret using the internal client UUID
    secret_url = f"{KEYCLOAK_SERVER_URL}admin/realms/{KEYCLOAK_REALM}/clients/{client_id_internal}/client-secret"
    try:
        secret_resp = requests.get(secret_url, headers=headers, timeout=10)
    except requests.RequestException as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to contact Keycloak client-secret endpoint: {exc}",
        ) from exc

    if secret_resp.status_code != 200:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to retrieve Keycloak client secret (status {secret_resp.status_code}): {secret_resp.text}",
        )

    secret_data = secret_resp.json()
    secret_value = secret_data.get("value")
    if not secret_value:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="Keycloak client-secret response did not contain a 'value'.",
        )

    return secret_value



# --- Keycloak Token Verification Logic ---

# We fetch the public keys from Keycloak, which are used to verify the token's signature.
# This is done once when the application starts.
jwks_url = f"{KEYCLOAK_SERVER_URL}realms/{KEYCLOAK_REALM}/protocol/openid-connect/certs"
jwks_client = PyJWKClient(jwks_url)

# Automatically fetch the client secret from Keycloak on startup
try:
    KEYCLOAK_CLIENT_SECRET = fetch_keycloak_client_secret_from_admin_api()
    print(f"✅ Automatically fetched client secret for '{KEYCLOAK_CLIENT_ID}'")
except Exception as e:
    print(f"❌ Failed to fetch client secret: {e}")
    # Fallback to environment variable if automatic fetch fails
    KEYCLOAK_CLIENT_SECRET = os.getenv("KEYCLOAK_CLIENT_SECRET")
    if not KEYCLOAK_CLIENT_SECRET:
        raise RuntimeError("Could not obtain KEYCLOAK_CLIENT_SECRET. Please check your configuration.")


async def get_current_user(token: str = Depends(oauth2_scheme)) -> Dict:
    """
    This function is a "dependency" that will be run for any endpoint that needs protection.
    It decodes and validates the JWT token from the request header.
    If the token is invalid or expired, it will raise an error.
    If the token is valid, it returns the user's information contained within the token.
    """
    last_error: Exception | None = None

    # First, try to treat the token as a Keycloak-issued JWT.
    try:
        signing_key = jwks_client.get_signing_key_from_jwt(token)
        data = jwt.decode(
            token,
            signing_key.key,
            algorithms=["RS256"],
            audience=KEYCLOAK_CLIENT_ID,
            options={"verify_exp": True},
        )
        return data
    except Exception as e:  # noqa: BLE001 - we re-map to HTTPException below
        last_error = e

    # If that fails and we have an app-level JWT secret configured, try to treat
    # the token as an internally issued app JWT (e.g. from Google token exchange).
    if APP_JWT_SECRET:
        try:
            data = jwt.decode(
                token,
                APP_JWT_SECRET,
                algorithms=["HS256"],
                options={"verify_exp": True},
            )
            return data
        except Exception as e:  # noqa: BLE001
            last_error = e

    # If we get here, neither Keycloak nor app token verification succeeded.
    message = "Could not validate user credentials."
    if isinstance(last_error, jwt.ExpiredSignatureError):
        message = "Your session has expired. Please log in again."
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=message,
        headers={"WWW-Authenticate": "Bearer"},
    )


def ensure_role(current_user: Dict, required_role: str) -> None:
    """
    Simple role-based access check using Keycloak realm roles.
    """
    roles: List[str] = current_user.get("realm_access", {}).get("roles", []) or []
    if required_role not in roles:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"Insufficient permissions, requires role '{required_role}'.",
        )


def require_role(required_role: str):
    """
    Dependency that enforces a Keycloak realm role. Use in route: Depends(require_role("admin")).
    """
    async def role_checker(current_user: dict = Depends(get_current_user)):
        ensure_role(current_user, required_role)
        return current_user
    return role_checker


class SayCommand(BaseModel):
    text: str


class QRLoginRequest(BaseModel):
    qr_code: str


# --- API Endpoints ---

# helper for building Keycloak authorization URLs

def build_keycloak_auth_url(idp_hint: str | None = None) -> str:
    """Return the Keycloak authorization endpoint URL. Optionally include
    `kc_idp_hint` so the login page pre-selects an identity provider (e.g.
    Google). The frontend can redirect users here or call the dedicated
    /auth/google endpoint below.
    """
    base = f"{KEYCLOAK_PUBLIC_URL}realms/{KEYCLOAK_REALM}/protocol/openid-connect/auth"
    params = {
        "client_id": KEYCLOAK_CLIENT_ID,
        "response_type": "code",
        "scope": "openid email profile",
    }
    if idp_hint:
        params["kc_idp_hint"] = idp_hint

    # build query string without importing urllib for simplicity
    query = "&".join(f"{k}={requests.utils.quote(v)}" for k, v in params.items())
    return f"{base}?{query}"


@app.get("/auth/login")
async def redirect_to_login(idp: str | None = None):
    """Redirect the caller to the Keycloak login page.

    If an `idp` query parameter is supplied (for example `?idp=google`), the
    `kc_idp_hint` parameter will be added so Keycloak pre‑selects that
    identity provider. This is useful if your frontend has a "Login with
    Google" button and you want to avoid an extra click on Keycloak's
    chooser page.
    """
    url = build_keycloak_auth_url(idp_hint=idp)
    return RedirectResponse(url)


@app.get("/auth/google")
async def login_with_google():
    """Shorthand endpoint that redirects directly to Keycloak's Google
    identity provider flow.  """
    return RedirectResponse(build_keycloak_auth_url(idp_hint="google"))


class GoogleTokenExchange(BaseModel):
    """Model for the Android app to exchange Google ID token for access token."""
    google_id_token: str


@app.post("/auth/exchange-google-token")
async def exchange_google_token(request: GoogleTokenExchange):
    """
    Exchange a Google ID token (from the Android client) for an **app access
    token** that this backend accepts on protected endpoints.

    Flow:
    1. Validate the Google ID token with Google's `tokeninfo` endpoint.
    2. If valid and the audience matches our `GOOGLE_CLIENT_ID`, mint a
       short-lived JWT signed with `APP_JWT_SECRET` that encodes the user's
       basic identity (sub, email, name).
    3. Return that JWT in a standard-looking `{access_token, token_type,
       expires_in}` payload so the Android client can store and reuse it.

    Note: This does **not** create a user in Keycloak; instead, the backend's
    `get_current_user` can understand both Keycloak tokens and these app tokens.
    """
    if not GOOGLE_CLIENT_ID:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="GOOGLE_CLIENT_ID is not configured on the server.",
        )
    if not APP_JWT_SECRET:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="APP_JWT_SECRET is not configured on the server.",
        )

    google_id_token = request.google_id_token
    if not google_id_token:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="google_id_token is required.",
        )

    # 1) Validate the Google ID token with Google's tokeninfo endpoint.
    tokeninfo_url = "https://oauth2.googleapis.com/tokeninfo"
    try:
        resp = requests.get(tokeninfo_url, params={"id_token": google_id_token}, timeout=10)
    except requests.RequestException as exc:  # noqa: TRY003
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to contact Google tokeninfo endpoint: {exc}",
        ) from exc

    if resp.status_code != 200:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Google ID token validation failed (status {resp.status_code}).",
        )

    tokeninfo = resp.json()
    aud = tokeninfo.get("aud")
    if aud != GOOGLE_CLIENT_ID:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Google ID token audience does not match this app.",
        )

    # Basic sanity check on issuer
    iss = tokeninfo.get("iss", "")
    if iss not in ("accounts.google.com", "https://accounts.google.com"):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Google ID token issuer is invalid.",
        )

    # 2) Build app-level claims from the Google token
    user_sub = tokeninfo.get("sub")
    email = tokeninfo.get("email")
    email_verified = tokeninfo.get("email_verified")
    name = tokeninfo.get("name")

    if not user_sub:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Google ID token did not contain a subject.",
        )

    claims = {
        "sub": user_sub,
        "email": email,
        "email_verified": email_verified,
        "name": name,
        "auth_source": "google",
    }

    # 3) Mint a short-lived app JWT (1 hour by default)
    access_token = jwt.encode(
        claims,
        APP_JWT_SECRET,
        algorithm="HS256",
    )

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "expires_in": 3600,
    }


@app.get("/")
def read_root():
    """A public endpoint that anyone can access."""
    return {"message": "Hello from the public side of the QTrobot Backend"}


@app.get("/protected")
def read_protected_resource(current_user: dict = Depends(get_current_user)):
    """
    A protected endpoint.

    Because of `Depends(get_current_user)`, the code in this function will only
    run if the user provides a valid token. The 'current_user' variable will
    contain the decoded token data.
    """
    # You can now use the user's info from the token
    username = current_user.get("preferred_username", "user")
    return {"message": f"Hello, {username}! You have accessed a protected resource."}


@app.get("/health")
def health_check():
    """
    Public health-check endpoint for monitoring and uptime probes.
    """
    return {"status": "ok", "service": "QTrobot Backend"}


@app.get("/user/me")
def read_current_user(current_user: dict = Depends(get_current_user)):
    """
    Return key details about the currently authenticated user.
    """
    return {
        "id": current_user.get("sub"),
        "username": current_user.get("preferred_username"),
        "email": current_user.get("email"),
        "name": current_user.get("name"),
    }


@app.get("/user/roles")
def read_user_roles(current_user: dict = Depends(get_current_user)):
    """
    Expose the Keycloak realm roles associated with the current user.
    """
    roles: List[str] = current_user.get("realm_access", {}).get("roles", []) or []
    return {"roles": roles}


@app.get("/admin/summary")
def admin_summary(current_user: dict = Depends(get_current_user)):
    """
    Example admin-only endpoint using a Keycloak realm role.
    """
    ensure_role(current_user, "admin")
    username = current_user.get("preferred_username", "admin")
    return {
        "message": f"Welcome, {username}. This is an admin-only summary endpoint.",
        "user": username,
    }


@app.get("/admin/keycloak-client")
def get_keycloak_client(current_user: dict = Depends(get_current_user)):
    """
    Admin-only endpoint that returns the Keycloak client ID and secret.

    The client secret is fetched from Keycloak's Admin REST API at request time.
    """
    ensure_role(current_user, "admin")

    if not KEYCLOAK_CLIENT_ID:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="KEYCLOAK_CLIENT_ID is not configured on the server.",
        )

    # Prefer fetching the secret from Keycloak dynamically.
    client_secret = fetch_keycloak_client_secret_from_admin_api()

    return {
        "client_id": KEYCLOAK_CLIENT_ID,
        "client_secret": client_secret,
    }


@app.post("/robot/say")
def robot_say(command: SayCommand, current_user: dict = Depends(get_current_user)):
    """
    Protected endpoint representing a simple QTrobot command.

    In a real deployment this would forward the text to the robot;
    here we simply echo the intent.
    """
    username = current_user.get("preferred_username", "user")
    return {
        "status": "queued",
        "action": "say",
        "text": command.text,
        "requested_by": username,
    }


@app.post("/auth/qr-login")
def qr_login(request: QRLoginRequest):
    """
    Endpoint to handle QR-code-based login.

    The robot reads a QR code generated by the frontend and sends the decoded
    value to this endpoint as 'qr_code'. Your existing QR generation/verification
    logic should be plugged in here to map the QR code to a user/session and
    produce whatever token or redirect the client needs.
    """
    # Replace this placeholder with your existing QR verification + login logic.
    return {
        "status": "received",
        "qr_code": request.qr_code,
    }