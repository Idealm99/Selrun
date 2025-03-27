package com.example.running_app.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.running_app.R;
import com.example.running_app.data.database.dao.TB_Run;

import java.util.Locale;

public class DetailHistoryFragment extends Fragment {
    TextView distance, date, speed, timer, step;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_history, container, false);

        findId(view);

        if (getArguments() != null) {
            TB_Run tbRun = (TB_Run) getArguments().getSerializable("RUN_ITEM");

            if (tbRun != null) {
                date.setText(tbRun.getCreate_at());
                step.setText(String.valueOf(tbRun.getWalk_count()));
            }

            String detailTime = getArguments().getString("time");
            timer.setText(detailTime);

            String detailSpeed = getArguments().getString("speed");
//            speed.setText(detailSpeed + getResources().getString(R.string.adapter_text_speed));
            speed.setText(String.format(Locale.US, getResources().getString(R.string.adapter_text_speed), detailSpeed));

            String detailDistance = getArguments().getString("distance");
            distance.setText(detailDistance);
        }
        return view;
    }

    private void findId(View view) {
        distance = view.findViewById(R.id.detailDistance);
        date = view.findViewById(R.id.detailDate);
        speed = view.findViewById(R.id.detailSpeed);
        timer = view.findViewById(R.id.detailTimer);
        step = view.findViewById(R.id.detailStepCounter);
    }
}