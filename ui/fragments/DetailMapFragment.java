package com.example.running_app.ui.fragments;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.running_app.R;
import com.example.running_app.data.database.dao.TB_GPS;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class DetailMapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mMapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_map, container, false);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        if (getArguments() != null) {
            List<TB_GPS> gpsList = getArguments().getParcelableArrayList("GPS_LIST");

            if (gpsList != null && !gpsList.isEmpty()) {

                PolylineOptions polylineOptions = new PolylineOptions();    //Polyline 객체 생성

                for (TB_GPS gps : gpsList) {
                    double latitude = gps.getLat(); // TB_GPS 객체에서 위도(Lat) 가져오기
                    double longitude = gps.getLon(); // TB_GPS 객체에서 경도(Lon) 가져오기

                    polylineOptions.add(new LatLng(latitude, longitude)); // Polyline에 좌표 추가
                }

                polylineOptions.width(15);
                polylineOptions.color(Color.RED);

                googleMap.addPolyline(polylineOptions);


                // 시작점 마커 추가
                if (!gpsList.isEmpty()) {
                    TB_GPS startPoint = gpsList.get(0); // 첫 번째 항목을 시작점으로 설정
                    LatLng startLatLng = new LatLng(startPoint.getLat(), startPoint.getLon());
                    MarkerOptions startMarker = new MarkerOptions();
                    startMarker.position(startLatLng);
                    startMarker.title(getResources().getString(R.string.map_marker_first_position));
                    googleMap.addMarker(startMarker);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 15));

                    // 끝지점 마커 추가
                    TB_GPS endPoint = gpsList.get(gpsList.size() - 1); // 마지막 항목을 끝지점으로 설정
                    LatLng endLatLng = new LatLng(endPoint.getLat(), endPoint.getLon());
                    MarkerOptions endMarker = new MarkerOptions();
                    endMarker.position(endLatLng);
                    endMarker.title(getResources().getString(R.string.map_marker_last_position));
                    googleMap.addMarker(endMarker);
                }

            } else {
                // gpsList가 비어있는 경우 처리할 코드 작성
                Log.e("DetailMapFragment", "GPS 리스트가 비어 있습니다.");
                // 예를 들어 사용자에게 메시지를 표시할 수 있습니다.
                Toast.makeText(requireContext(), getResources().getString(R.string.detail_map_gpsList), Toast.LENGTH_SHORT).show();

                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

}