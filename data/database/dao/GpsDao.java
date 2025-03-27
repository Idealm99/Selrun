package com.example.running_app.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

//Data Access Object
@Dao
public interface GpsDao {
    //삽입
    @Insert
    void setInsertGps(TB_GPS tb_gps);
    //조회 쿼리
    @Query("SELECT * FROM TB_GPS")
    List<TB_GPS> getGpsAll();

    //전체 좌표 조회
    @Query("SELECT * FROM TB_GPS WHERE TB_GPS.run_id = :runId")
    List<TB_GPS> getAllGpsByRunId(int runId);

}
