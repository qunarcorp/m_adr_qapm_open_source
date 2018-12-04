package com.mqunar.pager;

import android.view.View;

import com.mqunar.QAPM;
import com.mqunar.dao.Storage;
import com.mqunar.dao.UIDataParse;
import com.mqunar.domain.UIData;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 *
 */

public class QNetworkFailedContainer {

    public static void onVisibilityChanged(Object object, View changedView, int visibility) {
        if (visibility == View.VISIBLE) {
            UIData uiData = QLoadingReportHelper.newInstance().popReportMessage();
            if (uiData != null){
                uiData.status = UIData.ERROR;
                Storage.newStorage(QAPM.mContext).putData(uiData, UIDataParse.newInstance());
            }
        }
    }

}
