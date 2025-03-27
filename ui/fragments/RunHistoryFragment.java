package com.example.running_app.ui.fragments;


import android.os.Bundle;

import android.os.Parcelable;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.running_app.R;



import com.example.running_app.databinding.FragmentRunHistoryBinding;
import com.example.running_app.ui.RunningAdapter;
import com.example.running_app.ui.viewmodels.RunViewModel;


import java.util.ArrayList;


public class RunHistoryFragment extends Fragment {
    RunViewModel viewModel;
    RecyclerView recyclerView;
    RunningAdapter runningAdapter;

    public FragmentRunHistoryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRunHistoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        Log.d("onCreateView", "onCreateView 화면");
        findID(view);

        runningAdapter = new RunningAdapter(requireActivity().getApplication(), getActivity());
        recyclerView.setAdapter(runningAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        viewModel = new ViewModelProvider(this).get(RunViewModel.class);
        viewModel.getRunAll().observe(getViewLifecycleOwner(), tbRuns -> {
            runningAdapter.setRunItems(tbRuns);
            runningAdapter.notifyDataSetChanged();
        });

        runningAdapter.setOnItemClickListener((item, distanceFormat, speedFormat, timeFormat, allGps) -> {
            DetailMainHistoryFragment detailMainHistoryFragment = new DetailMainHistoryFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("RUN_ITEM", item);    //객체로 전달하기 위해 TB_RUN 클래스 Serializable사용
            bundle.putString("distance", distanceFormat);
            bundle.putString("speed", speedFormat);
            bundle.putString("time", timeFormat);
            bundle.putParcelableArrayList("GPS_LIST", (ArrayList<? extends Parcelable>) allGps);    //용량제한?
            detailMainHistoryFragment.setArguments(bundle);

            // 화면 전환
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.run_history, detailMainHistoryFragment);
            transaction.addToBackStack(null);   //백스택에 add,replace 할때 사용(transaction 단위 저장) -> popBackStack 실행으로 백스택에 저장된 transaction을 pop해서 역순으로 동작 시킨다.
            transaction.commit();

            binding.recyclerview.setVisibility(View.GONE);
        });

        return view;
    }


    private void findID(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
    }

}
