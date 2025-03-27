package com.example.running_app.data.database.dao;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index("run_id")},
        tableName = "TB_GPS",
        foreignKeys = @ForeignKey(entity = TB_Run.class,
                parentColumns = "run_id",
                childColumns = "run_id",
                onDelete = ForeignKey.CASCADE))
//@Entity(tableName = "TB_GPS")
public class TB_GPS implements Parcelable { //Parcelable은 안드로이드에서 제공하는 인터페이스로, 객체를 전달하기 위해 사용, 객체를 직렬화하여 바이트 배열로 변환하여 전달 가능(Intent나 Bundle에 담아 전달)
    @PrimaryKey(autoGenerate = true)
    private int gps_id;
    @ColumnInfo(name = "run_id")
    private int run_id;
    @ColumnInfo(name = "lat")
    private double lat;
    @ColumnInfo(name = "lon")
    private double lon;

    public TB_GPS() {
    //Parcelable 사용하기 위해서 생성자 생성
    }
    protected TB_GPS(Parcel in) {   //CREATOR 가 사용하는 생성자
        gps_id = in.readInt();
        run_id = in.readInt();
        lat = in.readDouble();
        lon = in.readDouble();
    }

    public static final Creator<TB_GPS> CREATOR = new Creator<TB_GPS>() {
        @Override
        public TB_GPS createFromParcel(Parcel in) {
            return new TB_GPS(in);
        }

        @Override
        public TB_GPS[] newArray(int size) {
            return new TB_GPS[size];
        }
    };



    public int getGps_id() {
        return gps_id;
    }

    public void setGps_id(int gps_id) {
        this.gps_id = gps_id;
    }

    public int getRun_id() {
        return run_id;
    }

    public void setRun_id(int run_id) {
        this.run_id = run_id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(gps_id);
        dest.writeInt(run_id);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }

}
