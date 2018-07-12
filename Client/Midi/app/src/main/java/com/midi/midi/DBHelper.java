package com.midi.midi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public void insert(long _id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO PLAYED (song_id) VALUES('"+ _id +"')");
    }

    public int getPlayedCount(long _id){
        SQLiteDatabase db = getReadableDatabase();
        int result;

        Cursor cursor = db.rawQuery("SELECT * FROM PLAYED WHERE song_id = '"+_id+"'", null);
        //while(cursor.moveToNext()){
        //    result += cursor.getInt(0) + " : " + cursor.getLong(1) + "\n";
        //}

        result = cursor.getCount();
        cursor.close();

        return result;
    }

    public void delete(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM PLAYED");
    }
}
