package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;
import com.example.qtrobot.qrcode.ChildQrHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Lists children for the logged-in parent; tapping one selects them and binds a unique QR token.
 */
public class ChildSelectionActivity extends BaseActivity {

    private ChildListAdapter adapter;
    private TextView emptyView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_selection);

        sessionManager = new SessionManager(this);
        boolean isGuest = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("is_guest", false);
        if (!sessionManager.isLoggedIn() && !isGuest) {
            startActivity(new Intent(this, GoogleSignInActivity.class));
            finish();
            return;
        }

        RecyclerView recycler = findViewById(R.id.children_recycler);
        emptyView = findViewById(R.id.empty_children);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChildListAdapter(this::onChildChosen);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.add_child_fab);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, NewProfileActivity.class);
            intentWithParentExtras(i, isGuest);
            i.putExtra(NewProfileActivity.EXTRA_RETURN_TO_CHILD_SELECTION, true);
            startActivity(i);
        });

        DataRepository repo = new DataRepository(getApplication());
        LiveData<List<ChildProfile>> live;
        if (isGuest) {
            live = repo.getChildrenForGuest();
        } else {
            long pid = sessionManager.getParentId();
            live = repo.getChildrenForParent(pid);
        }
        live.observe(this, children -> {
            boolean empty = children == null || children.isEmpty();
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (!empty) {
                adapter.submit(children);
            }
        });
    }

    private void intentWithParentExtras(Intent i, boolean isGuest) {
        if (!isGuest) {
            i.putExtra(NewProfileActivity.PARENT_ID_KEY, sessionManager.getParentId());
        } else {
            i.putExtra(NewProfileActivity.PARENT_ID_KEY, -1L);
        }
    }

    private void onChildChosen(ChildProfile child) {
        if (child == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ChildProfile row = AppDatabase.getInstance(ChildSelectionActivity.this)
                    .childProfileDao().getChildByIdSync(child.id);
            if (row == null) {
                runOnUiThread(() -> Toast.makeText(this, R.string.error_child_not_found, Toast.LENGTH_SHORT).show());
                return;
            }
            row.qr_string = ChildQrHelper.ensureQrToken(row);
            row.updatedAt = System.currentTimeMillis();
            row.isDirty = true;
            AppDatabase.getInstance(ChildSelectionActivity.this)
                    .childProfileDao().updateChild(row);

            runOnUiThread(() -> {
                sessionManager.setSelectedChildId(row.id);
                sessionManager.setHasChildProfile(true);
                Toast.makeText(this, getString(R.string.child_selected, row.preferredName != null ? row.preferredName : ""), Toast.LENGTH_SHORT).show();
                Intent home = new Intent(ChildSelectionActivity.this, HomeActivity.class);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                finish();
            });
        });
    }
}
