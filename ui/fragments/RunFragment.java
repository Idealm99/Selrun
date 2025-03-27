package com.example.running_app.ui.fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.running_app.R;
import com.example.running_app.data.model.GpsTrackerService;
import com.example.running_app.data.model.PermissionManager;
import com.example.running_app.data.model.PolylineUpdater;
import com.example.running_app.databinding.FragmentRunBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RunFragment extends Fragment implements OnMapReadyCallback, GpsTrackerService.updateMap, LocationListener {
    public GoogleMap mGoogleMap;
    Geocoder geocoder;

    private PolylineUpdater polylineMarkerUpdater;
    private static Marker initialMapMarker;
    private Marker runStartMapMarker;

    protected LocationManager locationManager;

    Circle circle;
    ValueAnimator animator;
    public PermissionManager permissionManager;
    public ActivityResultLauncher<String[]> requestPermissionLauncher;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    Location location;


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionManager = new PermissionManager();


        Log.d("HSR", "onCreate()");
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        geocoder = new Geocoder(requireActivity(), Locale.getDefault());

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        String permission = entry.getKey();
                        boolean isGranted = entry.getValue();

                        if (isGranted) {
                            // 권한이 허용된 경우 처리할 코드
                            Toast.makeText(getContext(), permission + " 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                        } else if (!permissionManager.haveRequiredPermissions(getContext())) {
                            // 권한이 거부된 경우 처리할 코드
                            Toast.makeText(getContext(), permission + " 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                            Log.d("HHH", "onActivityResult fail");
                            permissionManager.showPermissionDeniedNotification(requireActivity(), requireActivity().getResources().getString(R.string.permission_denied_notification_location_recognition), Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "app_settings");
                        } else if (!permissionManager.hasBackgroundPermission(getContext())) {
                            permissionManager.backgroundPermissionDeniedDialog(requireActivity(), getContext());
                        }
                    }
                });


    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentRunBinding binding = FragmentRunBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Log.e("HSR", "onCreateView");


        // 구글 맵 띄우기

        SupportMapFragment mapFragment = (SupportMapFragment) requireActivity()
                .getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("HSR", "onViewCreated()");

        permissionManager.requestPermission(requireContext(), this);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        Log.d("HSR", "onStart()");

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (permissionManager.haveRequiredPermissions(requireContext())) {
            // 위치 권한이 허용된 경우 처리할 코드
            // ...
            Log.d("HSR", "onStart() hasLocationPermissions");

            Toast.makeText(getContext(), "권한이 모두 허용되었습니다.", Toast.LENGTH_SHORT).show();

            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

        } else if (!permissionManager.hasBackgroundPermission(requireContext())) {
            // 위치 권한이 거부된 경우 처리할 코드
            // 다이얼로그가 표시된 후 권한을 허용한 경우 처리할 코드
            Toast.makeText(getContext(), "백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.", Toast.LENGTH_SHORT).show();
            permissionManager.backgroundPermissionDeniedDialog(requireActivity(), getContext());
        } else {
            permissionManager.requestPermission(requireContext(), this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("HSR", "onMapReady()");

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setBuildingsEnabled(true);

        polylineMarkerUpdater = new PolylineUpdater(mGoogleMap);

        if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void drawMap(Location location) {
        Log.d("HSR", "RunFragment : " + location);

        double lastLatitude = location.getLatitude();
        double lastLongitude = location.getLongitude();
        LatLng lastKnownLocation = new LatLng(lastLatitude, lastLongitude);

        if (initialMapMarker == null) {
            Log.d("HSR", "geocoder :" + geocoder);
            String initialMarkerTitle = (getAddress(getContext(), lastLatitude, lastLongitude));

            if (isAdded()) {
                initialMapMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(lastKnownLocation)
                        .title(initialMarkerTitle)
                        .snippet(getResources().getString(R.string.map_marker_first_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }

        }


        if (runStartMapMarker == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

            if (geocoder != null) {
                String currentMarkerTitle = (getAddress(getContext(), lastLatitude, lastLongitude));


                runStartMapMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(lastKnownLocation)
                        .title(currentMarkerTitle)
                        .snippet(getResources().getString(R.string.map_marker_current_location))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }


            CircleOptions circleOptions = new CircleOptions()
                    .center(lastKnownLocation)
                    .radius(100)
                    .strokeWidth(0)// 원의 반지름 설정 (미터 단위)
                    .fillColor(Color.parseColor("#33FF0000")); // 내부 색상 설정
            circle = mGoogleMap.addCircle(circleOptions);

            // Circle 깜빡임 애니메이션 적용
            animator = new ValueAnimator();
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setIntValues(0, 100);
            animator.setDuration(2000); // 애니메이션 속도를 조절합니다.
            animator.addUpdateListener(valueAnimator -> {
                int value = (int) valueAnimator.getAnimatedValue();
                circle.setRadius(value); // 원의 반지름을 변경하여 깜빡이는 효과를 줍니다.
            });
            animator.start();

        } else {
            if ("gps".equals(location.getProvider())) {
            runStartMapMarker.setPosition(lastKnownLocation);
            circle.setCenter(lastKnownLocation);
            polylineMarkerUpdater.updatePolyline(location);
            }

        }

    }

    public void clearMarkersAndCircle() {
        if (initialMapMarker != null) {
            initialMapMarker.remove();
            initialMapMarker = null;
        }
        if (runStartMapMarker != null) {
            runStartMapMarker.remove();
            runStartMapMarker = null;
        }
        if (circle != null) {
            circle.remove();
            animator.removeAllUpdateListeners();
            circle = null;
            animator = null;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        double lastLatitude = location.getLatitude();
        double lastLongitude = location.getLongitude();

        LatLng lastKnownLocation = new LatLng(lastLatitude, lastLongitude);

        if (initialMapMarker == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
            Log.d("HSR", "geocoder :" + geocoder);

            if (geocoder != null) {
                String initialMarkerTitle = (getAddress(getContext(), lastLatitude, lastLongitude));

                if (isAdded()) {
                    initialMapMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(lastKnownLocation)
                            .title(initialMarkerTitle)
                            .snippet(getResources().getString(R.string.map_marker_first_position))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }

            }
        }

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        Log.d("HSR", "onResume()");

        if (permissionManager.haveRequiredPermissions(getContext())) {
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

        }

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(getContext(), "위치 권한을 허용해 주세요.", Toast.LENGTH_SHORT).show();
            // 위치 설정 창을 띄우기 위한 인텐트 생성
            permissionManager.showPermissionDeniedNotification(getActivity(), requireActivity().getResources().getString(R.string.permission_location_off), Settings.ACTION_LOCATION_SOURCE_SETTINGS, "location_setting");

        }
    }


    public String getAddress(Context mContext, double lat, double lng) {

        String nowAddr = "현재 위치를 확인 할 수 없습니다.";
        List<Address> address;

        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    nowAddr = address.get(0).getAddressLine(0);
                }
            } else {
                geocoder = new Geocoder(mContext, Locale.getDefault());
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return nowAddr;
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
//        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
//        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HSR", "onDestroy()");

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
}
