package com.mqunar.qapm.check;

import android.content.Context;

public class ExceptionFinder {

    private boolean setPid = false;
    private boolean setVid = false;
    private boolean setSender = false;

    private static final int STATE_RUN_ON_QUNAR_INIT = 0;
    private static final int STATE_RUN_ON_QUNAR_YES = 1;
    private static final int STATE_RUN_ON_QUNAR_NO = 2;


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

    public void checkForThrows(Context context) {
        if (!setSender) {
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
