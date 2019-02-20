package com.mqunar.qapm.dao;

import android.os.Handler;
import android.os.HandlerThread;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.utils.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Storage implements IStorage{

    private static final int MAX_SIZE = 10;
    private static Storage sInstance = null;

    private static HandlerThread mStorageHandlerThread;
    private static Handler mStorageHandler;

    private static List<BaseData> mStorageData = new ArrayList<>();

    private Storage() {

    }

    public static Storage newStorage() {
        if (sInstance == null) {
            synchronized (Storage.class) {
                if (sInstance == null) {
                    sInstance = new Storage();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void putData(final BaseData data) {
        getStorageHandler().post(new Runnable() {
            public void run() {
                if(data == null){
                    return ;
                }
                if(mStorageData.size() < MAX_SIZE -1){
                    mStorageData.add(data);
                    return;
                }
                saveData(data);
            }
        });
    }

    @Override
    public void saveData(final BaseData data) {
        if(data != null){
            mStorageData.add(data);
        }
        String saveDataFilePath = QAPM.getSaveDataFile(System.currentTimeMillis() + "");
        if(saveDataFilePath != null){
            IOUtils.str2File(convertBaseData2Json(mStorageData), saveDataFilePath);
            mStorageData.clear();
        }
    }

    private String convertBaseData2Json(List<BaseData> mStorageData) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < mStorageData.size(); i++){
            JSONObject jsonObject = mStorageData.get(i).toJSONObject();
            if(jsonObject != null){
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray.toString();

    }

    @Override
    public void popData() {
        getStorageHandler().post(new Runnable() {
            public void run() {
                if(mStorageData != null && mStorageData.size() > 0){
                    saveData(null);
                }
            }
        });
    }

    private HandlerThread getStorageHandlerThread() {
        if (mStorageHandlerThread == null) {
            mStorageHandlerThread = new HandlerThread(QAPMConstant.THREAD_STORAGE);
            mStorageHandlerThread.start();
        }
        return mStorageHandlerThread;
    }

    private Handler getStorageHandler() {
        if (mStorageHandler == null) {
            synchronized (Storage.class) {
                if (mStorageHandler == null) {
                    mStorageHandler = new Handler(getStorageHandlerThread().getLooper());
                }
            }
        }
        return mStorageHandler;
    }
}
