package com.midi.midi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//데이터베이스 동작 클래스
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //디비 생성
        db.execSQL("CREATE TABLE PLAYED(_id INTEGER PRIMARY KEY AUTOINCREMENT, song_id LONG NOT NULL);");
        db.execSQL("CREATE TABLE RECO(_id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(20) NOT NULL, album VARCHAR(20) NOT NULL, artist VARCHAR(20) NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertPlayed(long _id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO PLAYED (song_id) VALUES('" + _id + "')");
    }

    public void insertReco(String title, String album, String artist){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO RECO (title, album, artist) VALUES('"+ title +"', '"+ album +"', '"+ title +"')");
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

    //RECO 테이블의 title, album, artist 리턴
    public String getRecoResult(){
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery("SELECT title, album, artist FROM RECO", null);
        while (cursor.moveToNext()) {
            // \t로 구분
            result += cursor.getString(0) + "\t" + cursor.getString(1) + "\t" + cursor.getString(2);
        }
        return result;
    }


    public void delete(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM PLAYED");
    }
}
