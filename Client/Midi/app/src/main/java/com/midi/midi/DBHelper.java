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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(long _id) {
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

    //디비 내용 리턴
    public String getResult() {
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery("SELECT * FROM PLAYED", null);
        while (cursor.moveToNext()) {
            result += cursor.getInt(0) + " : " + cursor.getLong(1) + "\n";
        }
        return result;
    }


    public void delete(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM PLAYED");
    }
}
