package com.example.qtrobot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qtrobot.data.local.entity.ChildProfile;

import java.util.ArrayList;
import java.util.List;

public class ChildListAdapter extends RecyclerView.Adapter<ChildListAdapter.VH> {

    public interface OnChildClickListener {
        void onChildClick(ChildProfile child);
    }

    private final List<ChildProfile> items = new ArrayList<>();
    private final OnChildClickListener listener;

    public ChildListAdapter(OnChildClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<ChildProfile> children) {
        items.clear();
        if (children != null) {
            items.addAll(children);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChildProfile c = items.get(position);
        String name = c.preferredName != null ? c.preferredName : "?";
        String user = c.childUsername != null ? c.childUsername : "";
        holder.title.setText(name);
        holder.subtitle.setText(user.isEmpty() ? holder.itemView.getContext().getString(R.string.child_no_username) : "@" + user);
        holder.itemView.setOnClickListener(v -> listener.onChildClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.child_row_title);
            subtitle = itemView.findViewById(R.id.child_row_subtitle);
        }
    }
}
