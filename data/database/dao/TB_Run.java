package com.example.running_app.data.database.dao;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "TB_Run")
public class TB_Run implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int run_id;
    @ColumnInfo(name = "walk_count")
    private int walk_count;
    @ColumnInfo(name = "timer")
    private long timer;
    @ColumnInfo(name = "create_at")
    private String create_at;


    public int getRun_id() {
        return run_id;
    }

    public void setRun_id(int run_id) {
        this.run_id = run_id;
    }

    public int getWalk_count() {
        return walk_count;
    }

    public void setWalk_count(int walk_count) {
        this.walk_count = walk_count;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

}
