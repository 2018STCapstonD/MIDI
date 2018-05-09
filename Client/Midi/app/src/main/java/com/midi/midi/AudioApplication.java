package com.midi.midi;

import android.app.Application;

/**
 * Created by leejw on 2018-05-08.
 *
 * 앱 실행되어 Process 생성될 때 호출되는 객체
 */

public class AudioApplication extends Application{
    private static AudioApplication mInstance;
    private AudioServiceInterface mInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mInterface = new AudioServiceInterface(getApplicationContext());
    }

    public static AudioApplication getmInstance() {
        return mInstance;
    }

    public AudioServiceInterface getServiceInterface(){
        return mInterface;
    }
}
