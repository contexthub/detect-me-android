package com.contexthub.detectme;

import android.app.Application;

import com.chaione.contexthub.sdk.ContextHub;

/**
 * Created by andy on 10/15/14.
 */
public class DetectMeApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ContextHub.init(this, "YOUR-APP-ID-HERE");
    }
}
