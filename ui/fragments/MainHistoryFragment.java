package com.example.running_app.ui.fragments;

import static com.example.running_app.ui.MainActivity.bottomSheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.running_app.R;

public class MainHistoryFragment extends Fragment {

    Toolbar toolbar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        toolbar = view.findViewById(R.id.runToolbar);

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.listFrameLayout, new RunHistoryFragment());    //add 로 할경우, 상세화면갔다가 돌아와서 item 삭제시 중첩되서 화면에 보여지는 오류발생
        fragmentTransaction.commit();

        //Toolbar
        toolbar.setTitle("운동기록");
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        //뒤로 가기
        toolbar.setNavigationOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().popBackStack();
            viewFind();
            
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (bottomSheet.getVisibility() != View.VISIBLE) {
                    bottomSheet.setVisibility(View.VISIBLE);
                }
                requireActivity().getSupportFragmentManager().popBackStack();
                viewFind();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }



    private void viewFind() {
        View mainView = requireActivity().findViewById(R.id.mainConstraintLayout);
        View runView = requireActivity().findViewById(R.id.run_start_btn);
        View recordView = requireActivity().findViewById(R.id.show_record_btn);
        mainView.setVisibility(View.VISIBLE);
        runView.setVisibility(View.VISIBLE);
        recordView.setVisibility(View.VISIBLE);
    }

}