package com.mqunar.qapm.pager;

import android.view.View;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.UIData;

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
                uiData.netType = QAPM.getActiveNetworkWanType();
                Storage.newStorage(QAPM.mContext).putData(uiData);
            }
        }
    }

}
