import os
from typing import Dict, List

import jwt
from dotenv import load_dotenv
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jwt import PyJWKClient
from pydantic import BaseModel

# Load environment variables from your .env file
load_dotenv()

# --- Keycloak Configuration ---
# These values are read from your .env file
# Internal URL used **inside Docker** (backend -> Keycloak)
KEYCLOAK_SERVER_URL = os.getenv("KEYCLOAK_SERVER_URL")
KEYCLOAK_REALM = os.getenv("KEYCLOAK_REALM")
KEYCLOAK_CLIENT_ID = os.getenv("KEYCLOAK_CLIENT_ID")

# Public URL used by the browser / Swagger UI.
# Defaults to KEYCLOAK_SERVER_URL if not set.
KEYCLOAK_PUBLIC_URL = os.getenv("KEYCLOAK_PUBLIC_URL", KEYCLOAK_SERVER_URL)


# --- FastAPI Security and App Initialization ---

# This tells FastAPI how to find the token. It will look for an
# 'Authorization: Bearer <token>' header in incoming requests.
# IMPORTANT: Swagger/OpenAPI will call this URL from the browser,
# so it must be reachable from your machine (e.g. http://localhost:8080).
oauth2_scheme = OAuth2PasswordBearer(
    tokenUrl=f"{KEYCLOAK_PUBLIC_URL}realms/{KEYCLOAK_REALM}/protocol/openid-connect/token"
)

app = FastAPI(title="QTrobot Backend")


# --- Keycloak Token Verification Logic ---

# We fetch the public keys from Keycloak, which are used to verify the token's signature.
# This is done once when the application starts.
jwks_url = f"{KEYCLOAK_SERVER_URL}realms/{KEYCLOAK_REALM}/protocol/openid-connect/certs"
jwks_client = PyJWKClient(jwks_url)


async def get_current_user(token: str = Depends(oauth2_scheme)) -> Dict:
    """
    This function is a "dependency" that will be run for any endpoint that needs protection.
    It decodes and validates the JWT token from the request header.
    If the token is invalid or expired, it will raise an error.
    If the token is valid, it returns the user's information contained within the token.
    """
    try:
        # Get the key used to sign the token
        signing_key = jwks_client.get_signing_key_from_jwt(token)

        # Decode the token. This also checks the expiration time and signature.
        data = jwt.decode(
            token,
            signing_key.key,
            algorithms=["RS256"],
            # The 'audience' must match our client ID
            audience=KEYCLOAK_CLIENT_ID,
            options={"verify_exp": True},
        )
        return data

    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Your session has expired. Please log in again.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    except jwt.InvalidTokenError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Could not validate user credentials: {e}",
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