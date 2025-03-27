package com.example.running_app.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.running_app.data.database.dao.TB_GPS;
import com.example.running_app.data.database.dao.TB_Run;
import com.example.running_app.data.model.RunRepository;

import java.util.List;

public class RunViewModel extends AndroidViewModel {
    RunRepository runRepository;
    MutableLiveData<List<TB_Run>> runList = new MutableLiveData<>();
    MutableLiveData<List<TB_GPS>> gpsList = new MutableLiveData<>();
    public RunViewModel(@NonNull Application application){
        super(application);
        runRepository = new RunRepository(application);

        runList.postValue(runRepository.getRunAll());
        gpsList.postValue(runRepository.getGpsAll());

    }

    //select 함수
    public MutableLiveData<List<TB_Run>> getRunAll(){
        return runList;
    }
    public List<TB_GPS> getAllGpsByRunId(int runId){
        return runRepository.getAllGpsByRunId(runId);
    }

    //insert 함수
    public void setInsertRun(TB_Run tbRun) {
        runRepository.setInsertRun(tbRun);

        //바로 TimerViewModel 에서 작업
//        int runid = runRepository.getLatestRunId();
    }
    public void setInsertGps(TB_GPS tbGps) {
        runRepository.setInsertGps(tbGps);
    }

    //update 함수
    public void setUpdateRun(TB_Run tbRun){
        runRepository.setUpdateRun(tbRun);
    }

    //delete 함수
    public void setDeleteRun(TB_Run tbRun){
        runRepository.setDeleteRun(tbRun);
    }
}
