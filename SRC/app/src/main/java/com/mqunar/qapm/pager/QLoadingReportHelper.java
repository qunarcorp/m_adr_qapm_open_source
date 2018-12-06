package com.mqunar.qapm.pager;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.dao.UIDataParse;
import com.mqunar.qapm.domain.UIData;

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
//                Storage.newStorage(QAPM.mContext).putData(uiData);
            }
            reportMessages.clear();
        }
    }

}
