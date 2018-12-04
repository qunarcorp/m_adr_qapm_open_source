package com.mqunar.pager;

import com.mqunar.QAPM;
import com.mqunar.dao.Storage;
import com.mqunar.dao.UIDataParse;
import com.mqunar.domain.UIData;

import java.util.Stack;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 *
 */
public class QLoadingReportHelper {

    private Stack<UIData> reportMessages;

    private static QLoadingReportHelper mInstance;

    private QLoadingReportHelper(){
        reportMessages = new Stack<>();
    }

    public static QLoadingReportHelper newInstance(){
        if(mInstance == null){
            mInstance = new QLoadingReportHelper();
        }
        return mInstance;
    }

    public void addReportMessage(UIData uiData){
        reportMessages.push(uiData);
    }

    public UIData popReportMessage(){
        if(reportMessages != null && reportMessages.size() > 0){
            return reportMessages.pop();
        }
        return null;
    }

    public void saveReportMessage(){
        if(reportMessages != null && reportMessages.size() > 0){
            for (UIData uiData : reportMessages){
                uiData.status = UIData.SUCCESS;
                Storage.newStorage(QAPM.mContext).putData(uiData, UIDataParse.newInstance());
            }
            reportMessages.clear();
        }
    }

}
