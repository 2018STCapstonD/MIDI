package com.midi.midi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

//데이터베이스 동작 클래스
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //디비 생성
        db.execSQL("CREATE TABLE IF NOT EXISTS PLAYED(_id INTEGER PRIMARY KEY AUTOINCREMENT, song_id LONG NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS RECO(_id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(20) NOT NULL, artist VARCHAR(20) NOT NULL, album VARCHAR(20) NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertPlayed(long _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO PLAYED (song_id) VALUES('" + _id + "')");
    }

    //특정 곡(song_id)의 플레이 횟수 리턴
    public int getPlayedCount(long _id) {
        SQLiteDatabase db = getReadableDatabase();
        int result;

        Cursor cursor = db.rawQuery("SELECT * FROM PLAYED WHERE song_id = '" + _id + "'", null);
        //while(cursor.moveToNext()){
        //    result += cursor.getInt(0) + " : " + cursor.getLong(1) + "\n";
        //}

        result = cursor.getCount();
        cursor.close();

        return result;
    }

    //전체 플레이 횟수 리턴
    public int getTotalCount() {
        SQLiteDatabase db = getReadableDatabase();
        int result;

        Cursor cursor = db.rawQuery("SELECT * FROM PLAYED", null);

        result = cursor.getCount();
        cursor.close();

        return result;
    }

    //PLAYED 테이블 내용 리턴
    public String getPlayedResult() {
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery("SELECT * FROM PLAYED", null);
        while (cursor.moveToNext()) {
            result += cursor.getInt(0) + " : " + cursor.getLong(1) + "\n";
        }
        return result;
    }

    public void deletepalyed(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM PLAYED");
    }

    //RECO 테이블의 title, album, artist 리턴
    public ArrayList<String[]> getRecoResult() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String[]> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT title, artist, album FROM RECO", null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // \t로 구분
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String album = cursor.getString(2);
                result.add(new String[]{title, artist, album});
            }
        }
        return result;
    }


    public void insertReco(String title, String artist, String album){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO RECO (title, artist, album) VALUES('"+ title +"', '"+ artist +"', '"+ album +"')");
    }

    public void deleteReco() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM RECO");
    }

}
