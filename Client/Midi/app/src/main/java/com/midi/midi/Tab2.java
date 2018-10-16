package com.midi.midi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

@SuppressLint("ValidFragment")
public class Tab2 extends Fragment implements View.OnClickListener {
    Context mContext;

    private ImageView mImgAlbumArt;
    private TextView mTxtTitle;
    private ImageButton mBtnPlayPause;
    private SeekBar seekBar;
    private TextView duration;
    private int musicSec = 0;

    private SeekBarThread sbThread;

    public Tab2(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_2, null);

        mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
        mTxtTitle = (TextView) view.findViewById(R.id.title);
        mBtnPlayPause = (ImageButton) view.findViewById(R.id.play_pause);
        view.findViewById(R.id.lin_miniplayer).setOnClickListener(this);
        view.findViewById(R.id.rewind).setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        view.findViewById(R.id.forward).setOnClickListener(this);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        duration = (TextView) view.findViewById(R.id.duration);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(!fromUser)
                    seekBar.setProgress(AudioApplication.getmInstance().getServiceInterface().getCurrentPosition());
                else
                    AudioApplication.getmInstance().getServiceInterface().seek(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sbThread.stopThread();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sbThread.resumeThread();
            }
        });

        registerBroadcast();
        updateUI();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.lin_miniplayer:
                break;
            case R.id.rewind:
                AudioApplication.getmInstance().getServiceInterface().rewind();
                break;
            case R.id.play_pause:
                AudioApplication.getmInstance().getServiceInterface().togglePlay();
                break;
            case R.id.forward:
                AudioApplication.getmInstance().getServiceInterface().forward();
                break;

        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    private void updateUI() {
        if (AudioApplication.getmInstance().getServiceInterface().isPlaying()) {
            mBtnPlayPause.setImageResource(R.drawable.pause);
        } else {
            mBtnPlayPause.setImageResource(R.drawable.play);
        }
        AudioAdapter.AudioItem audioItem = AudioApplication.getmInstance().getServiceInterface().getAudioItem();
        if (audioItem != null) {
            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioItem.mAlbumId);
            Picasso.with(mContext).load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
            mTxtTitle.setText(audioItem.mTitle);
            seekBar.setMax((int)AudioApplication.getmInstance().getServiceInterface().getAudioItem().mDuration);
            Log.e("****현재 음악 길이 : ",""+musicSec);
            seekBar.setProgress(0);
            sbThread = new SeekBarThread();
            sbThread.start();
        } else {
            mImgAlbumArt.setImageResource(R.drawable.empty_albumart);
            mTxtTitle.setText("재생중인 음악이 없습니다.");
        }
    }

    public void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        filter.addAction(BroadcastActions.PREPARED);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    public void unregisterBroadcast() {
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    class SeekBarThread extends Thread{
        private boolean stopFlag = false;
        private boolean pauseFlag = false;

        public synchronized void resumeThread() {
            pauseFlag = false;
            notify();
        }
        public void stopThread() {
            stopFlag = true;
        }
        public void pause() {
            pauseFlag = true;
        }
        @Override
        public void run(){
            try {
                while(!stopFlag) {
                    synchronized(this) {
                        while(pauseFlag) {
                            wait();
                        }
                    }
                    while(!this.isInterrupted() && AudioApplication.getmInstance().getServiceInterface().isPlaying()){
                        seekBar.setProgress(AudioApplication.getmInstance().getServiceInterface().getCurrentPosition());
                        //duration.setText(musicSec/60 +" : "+musicSec%60);
                        duration.post(new Runnable() {
                            @Override
                            public void run() {
                                int sec = AudioApplication.getmInstance().getServiceInterface().getCurrentPosition()/1000;
                                if(sec >= 60){
                                    if((sec%60) >= 10){
                                        duration.setText(String.valueOf("0"+sec/60+":"+sec%60));
                                    } else
                                        duration.setText(String.valueOf("0"+sec/60+":0"+sec%60));

                                } else if(sec >= 10){
                                    duration.setText(String.valueOf("00:" + sec));
                                } else
                                    duration.setText(String.valueOf("00:0" + sec));

                            }
                        });
                    }
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
