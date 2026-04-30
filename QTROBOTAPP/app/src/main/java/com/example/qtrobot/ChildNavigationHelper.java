package com.example.qtrobot;

import android.content.Context;
import android.content.Intent;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;

/**
 * Resolves whether the user should create a profile, pick a child, or go home.
 * Call from a background thread (Room queries).
 */
public final class ChildNavigationHelper {

    private ChildNavigationHelper() {}

    public static Intent resolveNextScreen(Context ctx, SessionManager session, boolean isGuest) {
        ChildProfileDao dao = AppDatabase.getInstance(ctx).childProfileDao();
        long parentLocalId = session.getParentId();

        int count;
        if (isGuest) {
            count = dao.countChildrenForGuest();
        } else {
            if (parentLocalId < 0) {
                count = 0;
            } else {
                count = dao.countChildrenForParent(parentLocalId);
            }
        }

        if (count > 0) {
            session.setHasChildProfile(true);
        }

        if (count == 0) {
            Intent i = new Intent(ctx, NewProfileActivity.class);
            i.putExtra(NewProfileActivity.PARENT_ID_KEY, isGuest ? -1L : parentLocalId);
            return i;
        }

        long selected = session.getSelectedChildId();
        if (selected < 0 && count > 1) {
            return new Intent(ctx, ChildSelectionActivity.class);
        }
        if (selected < 0 && count == 1) {
            ChildProfile only = isGuest ? dao.getFirstGuestChildSync() : dao.getFirstChildForParentSync(parentLocalId);
            if (only != null) {
                session.setSelectedChildId(only.id);
                session.setHasChildProfile(true);
            }
        }
        return new Intent(ctx, HomeActivity.class);
    }
}
