package com.midi.midi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;

/**
 * Created by leejw on 2018-05-08.
 *
 * AudioService와 바인딩
 */

public class AudioServiceInterface {
    private ServiceConnection mServiceConnection;
    private AudioService mService;

    public AudioServiceInterface(Context context) {
        mServiceConnection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((AudioService.AudioServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mServiceConnection = null;
                mService = null;
            }
        };
        context.bindService(new Intent(context, AudioService.class).setPackage(context.getPackageName()), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setPlayList(ArrayList<Long> audioIds) {
        if(mService != null) {
            mService.setPlayList(audioIds);
        }
    }

    public void play(int position) {
        if(mService != null) {
            mService.play(position);
        }
    }

    public void pause() {
        if(mService != null) {
            mService.pause();
        }
    }

    public void forward() {
        if(mService != null) {
            mService.forward();
        }
    }

    public void rewind() {
        if(mService != null) {
            mService.rewind();
        }
    }

    public void togglePlay() {
        if(isPlaying()) {
            mService.pause();
        } else {
            mService.play();
        }
    }

    public boolean isPlaying() {
        if (mService != null){
            return mService.isPlaying();
        }
        return false;
    }

    public AudioAdapter.AudioItem getAudioItem() {
        if(mService != null) {
            return mService.getAudioItem();
        }
        return null;
    }

}
