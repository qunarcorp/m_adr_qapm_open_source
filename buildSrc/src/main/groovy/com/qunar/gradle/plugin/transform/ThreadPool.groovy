package com.qunar.gradle.plugin.transform

import com.qunar.gradle.plugin.utils.QBuildLogger

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

public class ThreadPool {

    static ExecutorService sExecutor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors())
//    static ExecutorService sExecutor = Executors.newFixedThreadPool(1)
    private List<Callable> callableList = new ArrayList<>()

    public void submit(Callable callable) {
        callableList.add(callable)
    }

    public void invokeAll() {
        QBuildLogger.log "start run inject, ThreadCount = " + (Runtime.runtime.availableProcessors())
        sExecutor.invokeAll(callableList)
        callableList.clear()
    }

}
