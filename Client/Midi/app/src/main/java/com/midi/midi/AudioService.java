package com.midi.midi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by leejw on 2018-05-08.
 */

public class AudioService extends Service {
    private final IBinder mBinder = new AudioServiceBinder();
    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private boolean isPrepared;
    private DBHelper dbHelper;
    private NotificationPlayer mNotificationPlayer;

    public class AudioServiceBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        dbHelper = new DBHelper(getApplicationContext(), "played.db", null, 1);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mp.start();
                sendBroadcast(new Intent(BroadcastActions.PREPARED)); //prepared전송
                updateNotificationPlayer();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
                //isPrepared = false;
                //sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); //재생상태 변경 전송
                forward();
                updateNotificationPlayer();
            }
        });
        //forward()체크 필요

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); //재생상태 변경 전송
                updateNotificationPlayer();

                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener(){
            @Override
            public void onSeekComplete(MediaPlayer mp) {
            }
        });
        mNotificationPlayer = new NotificationPlayer(this, mContext);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class CommandActions {
        public final static String REWIND = "REWIND";
        public final static String TOGGLE_PLAY = "TOGGLE_PLAY";
        public final static String FORWARD = "FORWARD";
        public final static String CLOSE = "CLOSE";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (CommandActions.TOGGLE_PLAY.equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if (CommandActions.REWIND.equals(action)) {
                rewind();
            } else if (CommandActions.FORWARD.equals(action)) {
                forward();
            } else if (CommandActions.CLOSE.equals(action)) {
                pause();
                removeNotificationPlayer();
                onDestroy();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        removeNotificationPlayer();
    }

    private void updateNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.updateNotificationPlayer();
        }
    }

    private void removeNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.removeNotificationPlayer();
        }
    }


    private ArrayList<Long> mAudioIds = new ArrayList<>();
    public void setPlayList(ArrayList<Long> audioIds) {
        if(mAudioIds.size() != audioIds.size()) {
            if(!mAudioIds.equals(audioIds)) {
                mAudioIds.clear();
                mAudioIds.addAll(audioIds);
            }
        }
    }

    private int mCurrentPosition;
    private AudioAdapter.AudioItem mAudioItem;

    private void queryAudioItem(int position) {
        mCurrentPosition = position;
        long audioId = mAudioIds.get(position);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = {String.valueOf(audioId)};
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs,null);
        if(cursor != null) {
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                mAudioItem = AudioAdapter.AudioItem.bindCursor(cursor);
            }
            cursor.close();
        }
    }

    private void prepare(){
        try {
            mMediaPlayer.setDataSource(mAudioItem.mDataPath);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    public void play(int position) {
        queryAudioItem(position);
        stop();
        prepare();
        //현재 플레이중인 음악 ID 저장
        dbHelper.insertPlayed(mAudioIds.get(position));
    }

    public void play() {
        if(isPrepared){
            mMediaPlayer.start();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); //재생상태 변경 전송
            updateNotificationPlayer();
        }
    }

    public void pause() {
        if(isPrepared) {
            mMediaPlayer.pause();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); //재생상태 변경 전송
            updateNotificationPlayer();
        }
    }

    public void forward() {
        if(mAudioIds.size() - 1 > mCurrentPosition) {
            mCurrentPosition++; //다음 포지션으로 이동
        } else {
            mCurrentPosition = 0; //처음 포지션으로 이동
        }
        play(mCurrentPosition);
        sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); //재생상태 변경 전송
    }

    public void rewind() {
        if(mCurrentPosition > 0) {
            mCurrentPosition--; //이전 포지션으로 이동
        } else {
            mCurrentPosition = mAudioIds.size() -1; //마지막 포지션으로 이동
        }
        play(mCurrentPosition);
        sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); //재생상태 변경 전송
    }

    public void seek(int progress){
        mMediaPlayer.seekTo(progress);
    }

    public AudioAdapter.AudioItem getAudioItem() {
        return mAudioItem;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public int getCurrentPosition() { return mMediaPlayer.getCurrentPosition(); }

}
//to_do service tag 추가중, AudioService 수정중