package com.example.running_app.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.running_app.R;
import com.example.running_app.data.database.dao.TB_GPS;
import com.example.running_app.data.database.dao.TB_Run;
import com.example.running_app.data.model.OnRunHistoryItemClickListener;
import com.example.running_app.ui.viewmodels.RunViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunningAdapter extends RecyclerView.Adapter<RunningAdapter.ViewHolder> implements OnRunHistoryItemClickListener {
    private OnRunHistoryItemClickListener clickListener;
    private List<TB_Run> runItems = new ArrayList<>();
    private final Activity activity;
    private final RunViewModel viewModel;

    int runId;

    public RunningAdapter(Application application, Activity activity) {
        super();
        this.activity = activity;
        viewModel = new RunViewModel(application);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TB_Run data = runItems.get(position);
        holder.bind(data);

        holder.list_setting.setImageResource(R.drawable.baseline_delete_24);
        holder.list_setting.setOnClickListener(v -> {
            //Alert 창
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(activity.getResources().getString(R.string.adapter_dialog_message));
            builder.setNegativeButton(activity.getResources().getString(R.string.adapter_dialog_negative), null);
            builder.setPositiveButton(activity.getResources().getString(R.string.adapter_dialog_positive), (dialog, which) -> {

                Toast.makeText(activity, activity.getResources().getString(R.string.adapter_dialog_complete), Toast.LENGTH_SHORT).show();

                //delete
                /*
//                        TB_Run tbRun = new TB_Run();
//                        runId = data.getRun_id();
//                        tbRun.setRun_id(runId);
                 */
                viewModel.setDeleteRun(data);
                //현재 list 화면에서 삭제시 바로 화면 반영
                runItems.remove(holder.getAbsoluteAdapterPosition());
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return runItems.size();
    }

    public void setRunItems(List<TB_Run> tbRuns) {
        this.runItems = tbRuns;
        notifyDataSetChanged();
    }

    //Adapter onClick 설정
    @Override
    public void onItemClickListener(TB_Run item, String distanceFormat, String speedFormat, String timeFormat, List<TB_GPS> allGps) {
        if (clickListener != null){
            clickListener.onItemClickListener(item, distanceFormat, speedFormat, timeFormat, allGps);
        }
    }

    public void setOnItemClickListener(OnRunHistoryItemClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView t_date, t_distance, t_runTime, t_speed, t_walkCount;

        //popUpMenu
        ImageButton list_setting;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            t_date = itemView.findViewById(R.id.date);
            t_distance = itemView.findViewById(R.id.distance);
            t_runTime = itemView.findViewById(R.id.run_time);
            t_speed = itemView.findViewById(R.id.speed);
            t_walkCount = itemView.findViewById(R.id.walk_count);
            list_setting = itemView.findViewById(R.id.setting);
        }

        @SuppressLint("SetTextI18n")
        public void bind(TB_Run data) {
            //timer -> string 형태로 변환
            long timeValue = data.getTimer();
            timeFormat(timeValue);

            // activeRunId = repository.getLatestRunId();
            runId = data.getRun_id();
            List<TB_GPS> allGps = viewModel.getAllGpsByRunId(runId);
            double tDistance = totalDistance(allGps);

            //평균 속력
            double avgSpeed = resultAvgSpeed(timeValue, tDistance);

            t_date.setText(data.getCreate_at());
//            t_distance.setText(distanceFormat(tDistance) + activity.getResources().getString(R.string.adapter_text_distance));
            t_distance.setText(String.format(Locale.US, activity.getResources().getString(R.string.adapter_text_distance),distanceFormat(tDistance)));
            t_runTime.setText(timeFormat(timeValue));
//            t_speed.setText(speedFormat(avgSpeed) + activity.getResources().getString(R.string.adapter_text_speed));
            t_speed.setText(String.format(Locale.US, activity.getResources().getString(R.string.adapter_text_speed), speedFormat(avgSpeed)));
            t_walkCount.setText(String.valueOf(data.getWalk_count()));

            //Adapter onClick 설정
            itemView.setOnClickListener(v -> {
                // 어댑터의 컨텍스트에서 호출하는 경우 getBindingAdapterPosition()을 호출  /   RecyclerView가 표시하는 위치를 호출하려면 getAbsoluteAdapterPosition()을 호출
                int position = getBindingAdapterPosition();
                if (clickListener != null) {
                    TB_Run item = runItems.get(position);
                    clickListener.onItemClickListener(item, distanceFormat(tDistance), speedFormat(avgSpeed), timeFormat(timeValue), allGps);


                }
            });
        }

        private String speedFormat(double avgSpeed) {
            DecimalFormat df = new DecimalFormat("#");
            return df.format(avgSpeed);
        }

        private double resultAvgSpeed(long timeValue, double tDistance) {
            if (timeValue <= 0) {
                return 0.0;
            } else {
                double totalTimeInHours = timeValue / 3600.0;   //초 -> 시간 변환
                return tDistance / totalTimeInHours;  //   Km/h
            }
        }

        private String distanceFormat(double tDistance) {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(tDistance);
        }

        @SuppressLint("DefaultLocale")
        private String timeFormat(long timeValue) {

            int hours = (int) (timeValue / 3600);
            int minutes = (int) ((timeValue % 3600) / 60);
            int seconds = (int) (timeValue % 60);

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    private double totalDistance(List<TB_GPS> allGps) {
        double totalDistance = 0;
        TB_GPS prevGps = null;

        for (TB_GPS currentGps : allGps) {
            if (prevGps != null) {
                // 이전 GPS와 현재 GPS 사이의 거리 계산하여 누적
                double segmentDistance = haversine(prevGps.getLat(), prevGps.getLon(), currentGps.getLat(), currentGps.getLon());
                totalDistance += segmentDistance;
            }
            prevGps = currentGps;   // 현재 GPS를 이전 GPS로 설정하여 다음 순회에 사용
        }

        return totalDistance;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        // 지구의 반경 (단위: km)
        final double R = 6371.0;

        // 라디안으로 변환
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // 위도와 경도의 차이 계산
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Haversine 공식 적용
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 거리 계산

        return R * c;
    }
}
