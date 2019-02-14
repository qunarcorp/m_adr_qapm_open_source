package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.Fragment;

import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.listener.IFrameBeatListener;
import com.mqunar.qapm.plugin.TracePlugin;
import com.mqunar.qapm.utils.AndroidUtils;

import org.json.JSONObject;

import java.util.HashMap;

public abstract class BaseTracer  implements ApplicationLifeObserver.IObserver, IFrameBeatListener {
    private static final String TAG = "BaseTracer";
    private final TracePlugin mPlugin;
    private boolean isBackground = true;
    private String mScene;
    private boolean isCreated = false;
    private static final HashMap<Class<BaseTracer>, BaseTracer> sTracerMap = new HashMap<>();

    BaseTracer(TracePlugin plugin) {
        this.mPlugin = plugin;
        sTracerMap.put((Class<BaseTracer>) this.getClass(), this);
    }
    public <T extends BaseTracer> T getTracer(Class<T> clazz) {
        return (T) sTracerMap.get(clazz);
    }

    public TracePlugin getPlugin() {
        return mPlugin;
    }


    protected boolean isBackground() {
        return isBackground;
    }


    protected String getScene() {
        return mScene;
    }

    protected abstract String getTag();

    @Override
    public void onChange(final Activity activity, final Fragment fragment) {
        this.mScene = AndroidUtils.getSceneForString(activity, fragment);
    }

    @Override
    public void doFrame(long lastFrameNanos, long frameNanos) {

    }

    @Override
    public void cancelFrame() {

    }

    @Override
    public void onFront(Activity activity) {
        isBackground = false;
    }

    @Override
    public void onBackground(Activity activity) {
        isBackground = true;
    }

    @Override
    public void onActivityCreated(Activity activity) {
    }

    @Override
    public void onActivityPause(Activity activity) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResume(Activity activity) {

    }



    protected boolean isEnableMethodBeat() {
        return false;
    }

    public void onCreate() {

        ApplicationLifeObserver.getInstance().register(this);
        FrameBeat.getInstance().addListener(this);
        isCreated = true;
    }

    public void onDestroy() {

        ApplicationLifeObserver.getInstance().unregister(this);
        FrameBeat.getInstance().removeListener(this);
        isCreated = false;
    }

    public boolean isCreated() {
        return isCreated;
    }

    protected void sendReport(BaseData baseData) {


    }

    protected void sendReport(JSONObject jsonObject, String tag) {

    }


}
