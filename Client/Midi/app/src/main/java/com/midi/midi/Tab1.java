package com.midi.midi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.log.Logger;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class Tab1 extends Fragment implements View.OnClickListener{
    Context mContext;

    private RecyclerView mRecyclerView;
    private AudioAdapter mAdapter;
    private ImageView mImgAlbumArt;
    private TextView mTxtTitle;
    private ImageButton mBtnPlayPause;

    private static ArrayList<String[]> musicList;

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 37771;
    private final String ip = "117.17.198.39";
    private long kakao_id;
    private Handler myHandler;
    private Thread myThread;
    private DBHelper dbHelper;

    public Tab1(Context context, DBHelper dbHelper){
        mContext = context;
        this.dbHelper = dbHelper;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        requestMe();
        View view = inflater.inflate(R.layout.activity_1,null);

        // OS가 Marshmallow 이상일 경우 권한체크를 해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                // READ_EXTERNAL_STORAGE 에 대한 권한이 있음.
                getAudioListFromMediaDatabase(); //여기
            }
        }
        // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else {
            getAudioListFromMediaDatabase();
        }

        Button withdraw = (Button) view.findViewById(R.id.withdraw);
        withdraw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onClickUnlink();
            }
        });

        Button sendDataBtn = (Button) view.findViewById(R.id.sendDataBtn);
        sendDataBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try{
                    clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(ip, port), 3000);
                    socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    int totalCount = dbHelper.getTotalCount();

                    for(int i=0;i<musicList.size();i++){
                        String title = musicList.get(i)[0].replace("%","%%");
                        String album = musicList.get(i)[1].replace("%","%%");
                        String artist = musicList.get(i)[2].replace("%","%%");
                        String _id = musicList.get(i)[3].replace("%","%%");

                        int playedCount = dbHelper.getPlayedCount(Long.valueOf(_id));
                        double rating = 100 * playedCount/totalCount;

                        socketOut.println(kakao_id + "::" + title + "::" + album + "::" + artist + "::" + rating);
                    }

                    myHandler = new MyHandler();
                    myThread = new MyThread();
                    myThread.start();
                    myThread.interrupt();
                }catch(IOException e){
                    Toast.makeText(mContext, "Internal Server Error", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }catch(ArithmeticException e){
                    //totalCount가 0일 시
                    Toast.makeText(mContext, "보낼 데이터가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //        RecyclerView와 AudioAdapter를 연결하여 실제 데이터를 표시 추가_18/05/07_H
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mAdapter = new AudioAdapter(this.getContext(), null);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
//

        //player기능 추가_정원0508
        mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
        mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
        mBtnPlayPause = (ImageButton) view.findViewById(R.id.btn_play_pause);
        view.findViewById(R.id.lin_miniplayer).setOnClickListener(this);
        view.findViewById(R.id.btn_rewind).setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        view.findViewById(R.id.btn_forward).setOnClickListener(this);

        registerBroadcast();
        updateUI();


        return view;
    }
    /*********** 기능에 필요한 클래스 정의 ************/

    //player기능 추가_정원0508
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.lin_miniplayer:
                // 플레이어 화면으로 이동할 코드가 들어갈 예정
                break;
            case R.id.btn_rewind:
                // 이전곡으로 이동
                AudioApplication.getmInstance().getServiceInterface().rewind();
                break;
            case R.id.btn_play_pause:
                // 재생 또는 일시정지
                AudioApplication.getmInstance().getServiceInterface().togglePlay();
                break;
            case R.id.btn_forward:
                // 다음곡으로 이동
                AudioApplication.getmInstance().getServiceInterface().forward();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // READ_EXTERNAL_STORAGE 에 대한 권한 획득.
            getAudioListFromMediaDatabase(); //여기
        }
    }

    private void getAudioListFromMediaDatabase() {
        getLoaderManager().initLoader(3, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[]{
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA
                };
                String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
                String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
                return new CursorLoader(mContext, uri, projection, selection, null, sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//                파일 불러와서 로그찍기_18/05/07_H
                if (data != null && data.getCount() > 0) {
                    musicList = new ArrayList<String[]>();
                    while (data.moveToNext()) {
                        String title = data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String album = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        String artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String _id = data.getString(data.getColumnIndex(MediaStore.Audio.Media._ID));

                        musicList.add(new String[]{title, album, artist, _id});
                    }
                }
                //만들어진 AudioAdapter에 LoaderManager를 통해 불러온 오디오 목록이 담긴 Cursor를 적용_18/05/07_H
                mAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapCursor(null);
            }
        });
    }

    //정원추가0508
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

    //0629_jw
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


    //탈퇴관련
    public void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this.getContext())
                .setMessage(appendMessage)
                .setPositiveButton(getString(R.string.com_kakao_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                                    @Override
                                    public void onFailure(ErrorResult errorResult) {
                                        Logger.e(errorResult.toString());
                                    }

                                    @Override
                                    public void onSessionClosed(ErrorResult errorResult) {
                                        redirectLoginActivity();
                                    }

                                    @Override
                                    public void onNotSignedUp() {
                                        //redirectSignupActivity();
                                    }

                                    @Override
                                    public void onSuccess(Long userId) {
                                        redirectLoginActivity();
                                    }
                                });
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

    }
    public void redirectLoginActivity(){
        final Intent intent = new Intent(mContext, LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    class MyThread extends Thread{
        boolean flag = true;
        @Override
        public void run(){
            while(flag){
                try{
                    String data = socketIn.readLine();
                    Message msg = myHandler.obtainMessage();
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void interrupt(){
            try{
                flag = false;
                socketIn.close();
                socketOut.close();
                clientSocket.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg){}
    }

    public void requestMe() {
        //유저의 정보 받아오기
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
//                super.onFailure(errorResult);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                String message = "받아오기 실패 : " + errorResult;
                Logger.e(message);
            }

            @Override
            public void onNotSignedUp() {
                //카카오톡 회원이 아닐시
            }

            @Override
            public void onSuccess(UserProfile result) {
                kakao_id = result.getId();
            }
        });
    }
}