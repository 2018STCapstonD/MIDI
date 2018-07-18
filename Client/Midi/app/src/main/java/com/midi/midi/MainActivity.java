package com.midi.midi;


import android.Manifest;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ActionBar.TabListener{
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    static ViewPager mViewPager; //한번에 하나의 섹션 보여짐

    private final static int LOADER_ID = 0x001;

    private RecyclerView mRecyclerView;
    private AudioAdapter mAdapter;
    private ImageView mImgAlbumArt;
    private TextView mTxtTitle;
    private ImageButton mBtnPlayPause;

    // 소켓통신 관련 변수
    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 37771;
    private final String ip = "117.17.198.39";
    private MyThread myThread;
    private MyHandler myHandler;
    private long kakao_id;
    private static ArrayList<String[]> musicList;
    private DBHelper dbHelper;


    //

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(getApplicationContext(), "played.db", null, 1);

        //recommended Play List로 화면 전환_jw_0710
        Button btnRecommendList = findViewById(R.id.btn_recommendPlayList);
        Button btnPlayList = findViewById(R.id.btn_playList);

        btnRecommendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ReturnToMainPageFromRecommend.class);
                startActivity(intent);
            }
        });

        btnPlayList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ReturnToMainPageFromMusicList.class);
                startActivity(intent);
            }
        });
        //화면전환 끝
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestMe();

        // OS가 Marshmallow 이상일 경우 권한체크를 해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                // READ_EXTERNAL_STORAGE 에 대한 권한이 있음.
                getAudioListFromMediaDatabase(); //여기
            }
        }
        // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else {
            getAudioListFromMediaDatabase();
        }

        //탈퇴버튼
        Button withdraw = (Button) findViewById(R.id.withdraw);
        withdraw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onClickUnlink();
            }
        });

//      소켓통신
        Button sendDataBtn = (Button) findViewById(R.id.sendDataBtn);
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
                    Toast.makeText(getApplicationContext(), "Internal Server Error", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }catch(ArithmeticException e){
                    //totalCount가 0일 시
                    Toast.makeText(getApplicationContext(), "보낼 데이터가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

//        RecyclerView와 AudioAdapter를 연결하여 실제 데이터를 표시 추가_18/05/07_H
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new AudioAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
//

//player기능 추가_정원0508
        mImgAlbumArt = (ImageView) findViewById(R.id.img_albumart);
        mTxtTitle = (TextView) findViewById(R.id.txt_title);
        mBtnPlayPause = (ImageButton) findViewById(R.id.btn_play_pause);
        findViewById(R.id.lin_miniplayer).setOnClickListener(this);
        findViewById(R.id.btn_rewind).setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        findViewById(R.id.btn_forward).setOnClickListener(this);

        registerBroadcast();
        updateUI();


//Swipe기능_jw0717
        //어댑터 생성. 색션마다 프래그먼트를 생성하여 리턴
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        //액션바 설정
        final ActionBar actionBar = getSupportActionBar();
        //액션바 Home버튼 비활성화
        actionBar.setHomeButtonEnabled(true);
        //탭을 액션바에 보여줄 것이라고 지정
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //ViewPager 설정
        mViewPager = (ViewPager) findViewById(R.id.pager);
        //ViewPager에 어댑터 연결
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        //사용자가 섹션사이를 스와이프할 때 발생하는 이벤트에 대한 리스너 설정
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override //스와이프로 페이지 이동시 호출됨
            public void onPageSelected(int position) {
                //화면을 좌우로 스와이핑하여 섹션 사이를 이동할 때, 현재 선택된 탭의 위치
                //액션바의 탭위치를 페이지 위치에 맞춤
                actionBar.setSelectedNavigationItem(position);
            }
        });

        //각각의 섹션을 위한 탭을 액션바에 추가
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            //어댑터에서 정의한 페이지 제목을 탭에 보이는 문자열로 사용
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            //TabListener 인터페이스를 구현할 액티비티 오브젝트 지정
                            .setTabListener(this));
        }
    }

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
        getSupportLoaderManager().initLoader(3, null, new LoaderManager.LoaderCallbacks<Cursor>() {
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
                return new CursorLoader(getApplicationContext(), uri, projection, selection, null, sortOrder);
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
    protected void onDestroy() {
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
            Picasso.with(getApplicationContext()).load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
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
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void unregisterBroadcast() {
        unregisterReceiver(mBroadcastReceiver);
    }


    //소켓통신 관련
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

    //탈퇴관련
    private void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this)
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

    protected void redirectLoginActivity(){
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //유저정보 받아오기
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
                Log.e("kakao_id : ", String.valueOf(result.getId()));
                kakao_id = result.getId();
            }
        });
    }


//Swipe관련코드_jw0717
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        //액션바에서 선택된 탭에 대응되는 페이지를 뷰페이지에서 현재 보여지는 페이지로 변경
        mViewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}

    /*세션에 대응되는 프래그먼트를 리턴*/
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter{
        private FragmentManager fm;

        public AppSectionsPagerAdapter(FragmentManager fm){
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int pos) {
            //태그로 프래그먼트 찾기
            Fragment fragment = fm.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + getItemId(pos));

            //프래그먼트가 이미 생성되어 있는 경우 리턴
            if(fragment != null) {
                return fragment;
            }

            //프래그먼트의 인스턴스를 생성
            switch (pos) {
                case 0: return FirstFragment.newInstance("FirstFragment, Instance 1");
                case 1: return SecondFragment.newInstance("SecondFragment, Instance 2");
                case 2: return FragmentThird.newInstance("ThirdFragment, Instance 3");
                default: return FragmentThird.newInstance("ThirdFragment, Default");
            }
        }

        //프래그먼트를 최대 3개를 생성
        @Override
        public int getCount() {
            return 3;
        }

        //탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            return "Section" + (position + 1);
        }
    }
}