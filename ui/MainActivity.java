package com.example.running_app.ui;

import static com.example.running_app.data.model.PolylineUpdater.clearPolyline;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.content.ServiceConnection;

import android.location.Location;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.example.running_app.R;

import com.example.running_app.data.model.GpsTrackerService;
import com.example.running_app.data.model.StepCounter;
import com.example.running_app.databinding.ActivityMainBinding;
import com.example.running_app.ui.fragments.MainHistoryFragment;
import com.example.running_app.ui.fragments.RunFragment;
import com.example.running_app.ui.viewmodels.RunViewModel;
import com.example.running_app.ui.viewmodels.TimerViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    RunFragment runFragment = new RunFragment();
    public GpsTrackerService gpsTracker;


    public ActivityMainBinding binding;

    //timer, room DB
    private TimerViewModel timerViewModel;
    private float initialY;
    public static View bottomSheet;



    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        bottomSheet = binding.bottomSheet;

        BottomSheetBehavior<View> sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setHideable(true);
        sheetBehavior.setSkipCollapsed(false);

        // Bottom Sheet의 높이 조절을 위한 터치 핸들러 설정
        bottomSheet.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                initialY = event.getRawY();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dy = event.getRawY() - initialY;
                float newOffset = Math.max(100, sheetBehavior.getPeekHeight() - dy);
                sheetBehavior.setPeekHeight((int) newOffset);
                initialY = event.getRawY();
            }
            return true;
        });
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        sheetBehavior.setPeekHeight(100); // 최소 높이 설정
                        break;
                    case     BottomSheetBehavior.STATE_HIDDEN:
                        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // 숨겨진 상태일 때는 COLLAPSED 상태로 변경
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        sheetBehavior.setPeekHeight(800);

        //timer - viewModel
        timerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        RunViewModel runViewModel = new ViewModelProvider(this).get(RunViewModel.class);
        timerViewModel.setRunViewModel(runViewModel);

        binding.runStartBtn.setVisibility(View.VISIBLE);
        binding.runEndBtn.setVisibility(View.GONE);
        binding.showRecordBtn.setVisibility(View.VISIBLE);
        binding.stepcountTimerContainer.setVisibility(View.GONE);


        fragmentTransaction.add(R.id.run_fragment_container, runFragment);
        fragmentTransaction.commit();


        Intent gpsTrackerService = new Intent(getApplicationContext(), GpsTrackerService.class);
        bindService(gpsTrackerService, serviceGpsTrackerConnection, Context.BIND_AUTO_CREATE);
        Log.d("HSR", "MainActivity onCreate");


        StepCounter stepCounter = new StepCounter(this, timerViewModel);


//        Log.d("HSR", "" + BuildConfig.GOOGLE_MAP_API_KEY );

        binding.runStartBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RunStartCountdownActivity.class);
            startActivity(intent);

            // 재 시작시, 기존 폴리라인, 마커(처음위치, 현재위치) 초기화
            clearPolyline();
            clearMarkersAndCircle();

            gpsTracker.startLocationUpdate();
            stepCounter.start();

            binding.runStartBtn.setVisibility(View.GONE);
            binding.runEndBtn.setVisibility(View.VISIBLE);
            binding.showRecordBtn.setVisibility(View.GONE);
            binding.stepcountTimerContainer.setVisibility(View.VISIBLE);

            //timer
            timerViewModel.startTimer();
            stepCounter.setStepCountListener(stepCount -> binding.tvStepCount.setText(String.valueOf(stepCount)));
        });

        //timer 관찰
        timerViewModel.getTimeTextLiveData().observe(this, s -> binding.tvTime.setText(s));

        binding.runEndBtn.setOnClickListener(v -> {
            gpsTracker.stopUsingGPS();
            gpsTracker.stopService(new Intent(MainActivity.this, GpsTrackerService.class));
            gpsTracker.stopNotification();
            stepCounter.stop();

            binding.stepcountTimerContainer.setVisibility(View.GONE);
            binding.runEndBtn.setVisibility(View.GONE);
            binding.bottomSheet.setVisibility(View.GONE);

            //timer
            timerViewModel.stopTimer();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.run_history, new MainHistoryFragment());
            transaction.addToBackStack(null);   //transaction 단위 저장
            transaction.commit();

            binding.mainConstraintLayout.setVisibility(View.GONE);

        });

        binding.showRecordBtn.setOnClickListener(v -> {

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.run_history, new MainHistoryFragment());
            transaction.addToBackStack(null);   //transaction 단위 저장
            transaction.commit();

            binding.runStartBtn.setVisibility(View.GONE);
            binding.showRecordBtn.setVisibility(View.GONE);

            binding.mainConstraintLayout.setVisibility(View.GONE);
            binding.bottomSheet.setVisibility(View.GONE);
        });
    }


    GpsTrackerService.updateMap listener = new GpsTrackerService.updateMap() {

        @Override
        public void drawMap(Location location) {
            Log.d("HSR", "MainActivity.updateMap : " + location);

            timerViewModel.setGpsLocation(location);

            runFragment.drawMap(location);
        }

    };

    public void clearMarkersAndCircle() {
        if (runFragment != null) {
            runFragment.clearMarkersAndCircle();
        }
    }

    ServiceConnection serviceGpsTrackerConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            gpsTracker.setListener(null);
            gpsTracker = null;
            Log.d("HSR", "MainActivity onServiceDisconnected");

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                GpsTrackerService.LocalBinder mGpsTrackerServiceBinder = (GpsTrackerService.LocalBinder) service;
                gpsTracker = mGpsTrackerServiceBinder.getService();
                gpsTracker.startForeground();
                gpsTracker.setListener(listener);
                Log.d("HSR", "MainActivity onServiceConnected");

            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceGpsTrackerConnection);

        Log.d("HSR", "MainActivity onDestroy");

    }
}