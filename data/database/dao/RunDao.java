package com.example.running_app.data.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//Data Access Object
@Dao
public interface RunDao {
    @Insert //삽입
    void setInsertRun(TB_Run tb_run);

    @Update //수정
    void setUpdateRun(TB_Run tb_run);

    @Delete //삭제
    void setDeleteRun(TB_Run tb_run);

    //조회 쿼리
    @Query("SELECT * FROM TB_Run ORDER BY run_id DESC") //쿼리 : 데이터베이스에 요청하는 명령문
    List<TB_Run> getRunAll();

    //run_id 한개를 뽑아 내기 위한 조회 쿼리 -> Gps 테이블에 run_id 사용하기 위함
    @Query("SELECT run_id FROM TB_Run ORDER BY run_id DESC LIMIT 1")
    int getLatestRunId();

}
