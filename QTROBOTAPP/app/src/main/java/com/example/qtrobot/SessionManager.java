package com.example.qtrobot;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager
 *
 * Central class for reading and writing the active user session.
 * Stores the currently logged-in parent's local Room DB id,
 * their display name, email, and the backend access token.
 *
 * Usage:
 *   SessionManager session = new SessionManager(context);
 *   long parentId = session.getParentId();
 *   session.saveSession(parentId, name, email, accessToken);
 *   session.clearSession();
 */
public class SessionManager {

    private static final String PREF_NAME      = "auth";
    private static final String KEY_PARENT_ID  = "parent_local_id";
    private static final String KEY_NAME       = "parent_name";
    private static final String KEY_EMAIL      = "parent_email";
    private static final String KEY_TOKEN      = "access_token";
    private static final long   NO_SESSION     = -1L;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Returns true if a parent session is active. */
    public boolean isLoggedIn() {
        return prefs.getLong(KEY_PARENT_ID, NO_SESSION) != NO_SESSION;
    }

    /** Returns the local Room DB id of the currently active parent, or -1 if none. */
    public long getParentId() {
        return prefs.getLong(KEY_PARENT_ID, NO_SESSION);
    }

    /** Returns the display name of the signed-in parent, or empty string. */
    public String getParentName() {
        return prefs.getString(KEY_NAME, "");
    }

    /** Returns the email of the signed-in parent, or empty string. */
    public String getParentEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    /** Returns the backend JWT access token, or null. */
    public String getAccessToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Saves a full session after a successful login.
     *
     * @param parentLocalId  The auto-generated Room primary key for this parent.
     * @param displayName    Google display name (or registration name).
     * @param email          Parent email address.
     * @param accessToken    JWT from the backend token exchange (may be null).
     */
    public void saveSession(long parentLocalId, String displayName, String email, String accessToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_PARENT_ID, parentLocalId);
        editor.putString(KEY_NAME, displayName != null ? displayName : "");
        editor.putString(KEY_EMAIL, email != null ? email : "");
        if (accessToken != null) {
            editor.putString(KEY_TOKEN, accessToken);
        }
        editor.apply();
    }

    /** Returns true if the user has completed child profile setup. */
    public boolean hasChildProfile() {
        return prefs.getBoolean("has_child_profile", false);
    }

    /** Call this after a child profile is successfully created. */
    public void setHasChildProfile(boolean value) {
        prefs.edit().putBoolean("has_child_profile", value).apply();
    }

    /** Clears all session data (call on sign-out). */
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
