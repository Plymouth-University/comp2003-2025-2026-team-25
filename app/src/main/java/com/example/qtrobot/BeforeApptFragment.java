package com.example.qtrobot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BeforeApptFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_before_appointment, container, false);
        view.findViewById(R.id.go_back_button).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        return view;
    }
}
