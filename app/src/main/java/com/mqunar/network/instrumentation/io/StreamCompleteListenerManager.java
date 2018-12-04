package com.mqunar.network.instrumentation.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public class StreamCompleteListenerManager {
    private boolean streamComplete = false;
    private ArrayList<StreamCompleteListener> streamCompleteListeners = new ArrayList();

    StreamCompleteListenerManager() {
    }

    public boolean isComplete() {
        synchronized(this) {
            return this.streamComplete;
        }
    }

    public void addStreamCompleteListener(StreamCompleteListener streamCompleteListener) {
        ArrayList var2 = this.streamCompleteListeners;
        synchronized(this.streamCompleteListeners) {
            this.streamCompleteListeners.add(streamCompleteListener);
        }
    }

    public void removeStreamCompleteListener(StreamCompleteListener streamCompleteListener) {
        ArrayList var2 = this.streamCompleteListeners;
        synchronized(this.streamCompleteListeners) {
            this.streamCompleteListeners.remove(streamCompleteListener);
        }
    }

    public void notifyStreamComplete(StreamCompleteEvent ev) {
        if(!this.checkComplete()) {
            Iterator i$ = this.getStreamCompleteListeners().iterator();

            while(i$.hasNext()) {
                StreamCompleteListener listener = (StreamCompleteListener)i$.next();
                listener.streamComplete(ev);
            }
        }

    }

    public void notifyStreamError(StreamCompleteEvent ev) {
        if(!this.checkComplete()) {
            Iterator i$ = this.getStreamCompleteListeners().iterator();

            while(i$.hasNext()) {
                StreamCompleteListener listener = (StreamCompleteListener)i$.next();
                listener.streamError(ev);
            }
        }

    }

    private boolean checkComplete() {
        synchronized(this) {
            boolean streamComplete = this.isComplete();
            if(!streamComplete) {
                this.streamComplete = true;
            }

            return streamComplete;
        }
    }

    private List<StreamCompleteListener> getStreamCompleteListeners() {
        ArrayList var1 = this.streamCompleteListeners;
        synchronized(this.streamCompleteListeners) {
            ArrayList listeners = new ArrayList(this.streamCompleteListeners);
            this.streamCompleteListeners.clear();
            return listeners;
        }
    }
}
