a# Google Sign-In Integration Guide

This guide explains how to use the Google Sign-In integration for your Android QTrobot app with Keycloak.

---

## Architecture Overview

```
Android App (Google Sign-In)
    ↓
Keycloak (Identity Provider Broker)
    ↓
Google OAuth
    ↓
Backend (FastAPI + JWT validation)
```

---

## Setup Steps

### 1. Android App Configuration

#### 1.1 Get Your Google OAuth Client ID

1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Create a new **OAuth 2.0 Client ID** (or use existing web app credentials)
3. Get the **Web Client ID** (format: `xxx-xxx-xxx.apps.googleusercontent.com`)

#### 1.2 Update Android App

1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_GOOGLE_OAUTH_WEB_CLIENT_ID_HERE` with your actual Google Client ID:

```xml
<string name="server_client_id">YOUR_ACTUAL_CLIENT_ID.apps.googleusercontent.com</string>
```

#### 1.3 Register Android App with Google

Add your app to Google's OAuth 2.0 credentials:

1. In [Google Cloud Console Credentials](https://console.cloud.google.com/apis/credentials)
2. Click on your OAuth 2.0 Client ID
3. Add **Android** restrictions:
   - **Package name:** `com.example.qtrobot`
   - **SHA-1 certificate fingerprint:** `69:5B:73:F8:E4:C1:1E:FF:18:A5:58:DD:96:61:02:1B:53:36:BB:14`
4. Save

### 2. Keycloak Configuration

#### 2.1 Add Google as Identity Provider

1. Log in to Keycloak Admin Console (http://localhost:8080)
2. Select your `qtrobot` realm
3. Go to **Identity Providers** → **Add provider** → **Google**
4. Fill in your Google credentials:
   - **Client ID:** Your Google OAuth 2.0 Client ID
   - **Client Secret:** Your Google OAuth 2.0 Client Secret
5. Set **Default Scope:** `openid email profile`
6. Click **Save**

#### 2.2 Configure Mappers (Optional)

If you want to automatically assign roles to Google users:

1. Click the Google provider you just created
2. Go to **Mappers**
3. Create a new mapper to assign roles (e.g., `user` role for all Google signings)

### 3. Backend Configuration

The backend is already configured with:

- `GET /auth/google` — Redirect to Keycloak's Google login flow
- `GET /auth/login?idp=google` — Generic redirect with identity provider hint
- `POST /auth/exchange-google-token` — (Future) Direct token exchange for native apps

No additional backend configuration is needed for the browser-based flow.

---

## How the Flow Works

### Browser/Web Flow

1. User clicks "Login with Google" on your frontend
2. Frontend redirects to `http://localhost:8000/auth/google`
3. Backend redirects to Keycloak's authorization endpoint with `kc_idp_hint=google`
4. Keycloak redirects to Google OAuth
5. User signs in with Google
6. Google redirects back to Keycloak
7. Keycloak creates a JWT token and returns it
8. User is authenticated and can call protected endpoints with the token

### Android Native App Flow

1. User clicks "Login with Google" button in Android app
2. Android calls Google Sign-In API → User signs in
3. Android app gets Google ID token
4. Android app opens browser to `http://localhost:8000/auth/google` OR
   uses WebView for the Keycloak/Google OAuth flow
5. Backend/Keycloak handles the exchange
6. App receives Keycloak JWT
7. App stores JWT in SharedPreferences
8. All subsequent API calls include `Authorization: Bearer <jwt>`

---

## Testing the Integration

### 1. Start the Services

```bash
cd backend
docker compose up -d
```

### 2. Build and Run Android App

```bash
cd QTROBOTAPP
./gradlew assembleDebug
# Or install via Android Studio
```

### 3. Test the Flow

**Option A: Browser**
- Navigate to `http://localhost:8080/realms/qtrobot/account/#/`
- Click "Google" button
- Sign in with your Google account
- You'll be redirected back and logged in to Keycloak

**Option B: Android App**
- Click "Google Sign-In" button
- The native Google sign-in dialog appears
- After signing in, the app should redirect and authenticate
- Check logs for token exchange status

### 4. Verify Authentication

Use curl/Postman to test a protected endpoint with the JWT token you received:

```bash
curl -H "Authorization: Bearer <your_jwt_token>" \
  http://localhost:8000/user/me
```

Expected response:
```json
{
  "id": "user-uuid",
  "username": "your.email@gmail.com",
  "email": "your.email@gmail.com",
  "name": "Your Name"
}
```

---

## Troubleshooting

### Android App Shows "Google Services Error"

- Check that Google Play Services is installed on the device/emulator
- Verify your package name matches Google Cloud configuration
- Double-check the SHA-1 fingerprint in Google Console

### "Client not found" Error in Keycloak

- Ensure your realm and client are created
- Check that `KEYCLOAK_CLIENT_ID=qtrobot-realm` in `.env`
- Verify the client secret is correct in `.env`

### Keycloak Redirect Loop

- Ensure valid redirect URIs are configured in Keycloak client settings
- Check that `KEYCLOAK_PUBLIC_URL` is reachable from your client

### Token Validation Fails

- Verify the JWT was issued by Keycloak (check the `iss` claim)
- Ensure the token hasn't expired (`exp` claim)
- Check that your client ID matches the token's `aud` claim

---

## Next Steps

1. **Customization**: Add user profile mapping (name, picture, etc.)
2. **Role Assignment**: Set up automatic role assignment for Google users
3. **Logout**: Implement logout flow (revoke Keycloak tokens)
4. **Refresh Tokens**: Add token refresh logic in Android app
5. **Error Handling**: Enhance error messages in both Android and backend

---

## Useful Links

- [Keycloak Identity Provider Docs](https://www.keycloak.org/docs/latest/server_admin/#_identity_brokers)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [OAuth 2.0 Playground](https://developers.google.com/oauthplayground/) (for testing tokens)

