package com.mqunar.qapm.dao;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

public class Storage implements IStorage{

    private static final String TAG = "Storage";
    private static final int MAX_SIZE = 10;

    private static HandlerThread mStorageHandlerThread;
    private static Handler mStorageHandler;

    private static List<BaseData> mStorageData = new ArrayList<>();
    private Context context;

    private Storage(Context context) {
        this.context = context;
    }

    public static Storage newStorage(Context context) {
        return new Storage(context);
    }

    @Override
    public void putData(final BaseData data, final IDataParse parse) {
        getStorageHandler().post(new Runnable() {
            public void run() {
                if(data == null){
                    return ;
                }
                if(mStorageData.size() < MAX_SIZE){
                    mStorageData.add(data);
                    return;
                }
                saveData(data, parse);
            }
        });
    }

    @Override
    public void saveData(final BaseData data, IDataParse parse) {
        mStorageData.add(data);
        String saveDataFilePath = IOUtils.getSaveDataFile(context, System.currentTimeMillis() + "");
        IOUtils.str2File(parse.convertBaseData2Json(mStorageData), saveDataFilePath);
        mStorageData.clear();
    }

    @Override
    public void popData() {
        getStorageHandler().post(new Runnable() {
            public void run() {

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
