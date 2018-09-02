package com.example.skyworthclub.visible_light_communication.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * Created by skyworthclub on 2018/5/16.
 * 创建后台线程,捕获线程中抛出的异常
 */

public class DaemonThreadFactory implements ThreadFactory, Thread.UncaughtExceptionHandler {

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("caught: " + e);
    }
}
