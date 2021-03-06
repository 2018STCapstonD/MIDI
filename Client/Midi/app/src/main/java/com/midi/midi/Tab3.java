
package com.midi.midi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class Tab3 extends Fragment {
    Context mContext;
    private RecyclerView mRecyclerView;
    private RecommendMusicAdapter mRecoMusicAdapter;
    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private ArrayList<String[]> musicList;
    private ArrayList<String[]> recoList;
    private int port = 37771;
    private final String ip = "117.17.198.39";
    private long kakao_id;
    private Handler myHandler;
    private Thread myThread;
    private DBHelper dbHelper;
    RecyclerView.LayoutManager mLayoutManager;


    public Tab3(Context context, DBHelper dbHelper, ArrayList<String[]> musicList){
        mContext = context;
        this.dbHelper = dbHelper;
        this.musicList = musicList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_3,null);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestMe();


        mRecyclerView = (RecyclerView) view.findViewById(R.id.tab3_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mContext); //확인필요
        mRecyclerView.setLayoutManager(mLayoutManager);

        recoList = new ArrayList<>();
        if(dbHelper.getRecoResult() != null)
            recoList = dbHelper.getRecoResult();


        final ArrayList<RecommendMusic> recoMusicArrayList = new ArrayList<>();

        if(recoList != null) {
            for (int i = 0; i < recoList.size(); i++) {
                recoMusicArrayList.add(new RecommendMusic(recoList.get(i)[0], recoList.get(i)[1], recoList.get(i)[2]));
            }
        }

        ImageButton sendDataBtn = (ImageButton) view.findViewById(R.id.sendDataBtn);
        sendDataBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try{
                    String s = "";
                    clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(ip, port), 3000);
                    clientSocket.setSendBufferSize(1024);
                    socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    int totalCount = dbHelper.getTotalCount();

                    dbHelper.deleteReco();
                    recoMusicArrayList.clear();

                    for(int i=0;i<musicList.size();i++){
                        String title = musicList.get(i)[0];
                        String album = musicList.get(i)[1];
                        String artist = musicList.get(i)[2];
                        String _id = musicList.get(i)[3];

                        int rating = (int)(Math.random()*1000); //dbHelper.getPlayedCount(Long.valueOf(_id));

                        //double rating = Math.random() * 4 + 1;
                        //double rating = 100 * playedCount/totalCount;

                        socketOut.println(kakao_id + "\t" + title + "\t" + album + "\t" + artist + "\t" + rating);
                    }

                    while(!(s = socketIn.readLine()).equals("end")){
                        String data[] = s.split("\t");
                        String title = data[0];
                        String album = data[1];
                        String artist = data[2];
                        Log.e("title : ", title);
                        Log.e("artist : ", artist);
                        Log.e("album : ", album);
                        dbHelper.insertReco(title,artist,album);
                        recoMusicArrayList.add(new RecommendMusic(title, artist, album));
                    }
                    myHandler = new MyHandler();
                    myThread = new MyThread();
                    myThread.start();
                    myThread.interrupt();

                    mRecoMusicAdapter = new RecommendMusicAdapter(recoMusicArrayList);
                    mRecyclerView.setAdapter(mRecoMusicAdapter);

                }catch(IOException e){
                    Toast.makeText(mContext, "Internal Server Error", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }catch(ArithmeticException e){
                    //totalCount가 0일 시
                    Toast.makeText(mContext, "보낼 데이터가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Button withdrawBtn = (Button) view.findViewById(R.id.withdrawBtn);
        /* 탈퇴버튼 임시제거
        withdrawBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onClickUnlink();
            }
        });
        */

        mRecoMusicAdapter = new RecommendMusicAdapter(recoMusicArrayList);
        mRecyclerView.setAdapter(mRecoMusicAdapter);

        return view;

    }


    //탈퇴관련
    public void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(mContext)
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
                    String data= socketIn.readLine();
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
        public void handleMessage(Message msg){
            Log.e("msg",msg.toString());
        }
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