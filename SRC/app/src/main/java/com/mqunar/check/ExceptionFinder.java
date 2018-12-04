package com.mqunar.check;

import android.content.Context;

/**
 * Created by chaos on 17/1/18.
 */

public class ExceptionFinder {

    private boolean setPid = false;
    private boolean setVid = false;
    private boolean setSender = false;

    private static final int STATE_RUN_ON_QUNAR_INIT = 0;
    private static final int STATE_RUN_ON_QUNAR_YES = 1;
    private static final int STATE_RUN_ON_QUNAR_NO = 2;

    private int stateRunOnQunar = STATE_RUN_ON_QUNAR_INIT;

    private static ExceptionFinder instance;

    public static ExceptionFinder getInstance() {
        if (instance == null) {
            synchronized (ExceptionFinder.class) {
                if (instance == null) {
                    instance = new ExceptionFinder();
                }
            }
        }
        return instance;
    }

    public void setSender() {
        this.setSender = true;
    }

    public void setPid() {
        this.setPid = true;
    }

    public void setVid() {
        this.setVid = true;
    }

    public boolean runOnQunar(Context context) {
        if (stateRunOnQunar == STATE_RUN_ON_QUNAR_INIT) {
            if ("com.Qunar".equals(context.getPackageName())) {
                stateRunOnQunar = STATE_RUN_ON_QUNAR_YES;
            } else {
                stateRunOnQunar = STATE_RUN_ON_QUNAR_NO;
            }
        }
        return stateRunOnQunar == STATE_RUN_ON_QUNAR_YES;
    }

    //早发现早治疗
    public void checkForThrows(Context context) {
        if (runOnQunar(context) && !setSender) {
            throw new NotSetSenderException("没有设置Sender!");
        }
    }

    static class NotSetIDException extends IllegalArgumentException {

        public NotSetIDException(String message) {
            super(message);
        }
    }

    static class NotSetSenderException extends IllegalArgumentException {

        public NotSetSenderException(String message) {
            super(message);
        }
    }
}
