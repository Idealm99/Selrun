package com.example.running_app.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.running_app.R;

public class DetailMainHistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_main_history, container, false);

        DetailHistoryFragment detailHistoryFragment = new DetailHistoryFragment();
        DetailMapFragment detailMapFragment = new DetailMapFragment();

        // DetailMainHistoryFragment에서 받아온 Bundle 객체
        Bundle receivedBundle = getArguments();

        // 전달할 Bundle 객체 설정
        Bundle bundle = new Bundle();
        if (receivedBundle != null) {
            bundle.putAll(receivedBundle); // DetailMainHistoryFragment에서 받아온 Bundle 객체를 전달할 Bundle 객체에 추가
        }

        // DetailHistoryFragment에 전달할 Bundle 설정
        detailHistoryFragment.setArguments(bundle);
        detailMapFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mapLayout, detailMapFragment);
        fragmentTransaction.replace(R.id.detailLatout, detailHistoryFragment);
        fragmentTransaction.commit();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Log.d("HSR", "DetailMainHistoryFragment" + " => handleOnBackPressed");

                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }


}