package kkkb1114.sampleproject.infectionapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile;

public class Bodytemp_DBHelper extends SQLiteOpenHelper {

    public static Bodytemp_DBHelper mInstance;
    public static SQLiteDatabase writableDatabase;
    public static SQLiteDatabase readableDataBase;

    public Bodytemp_DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS USER_PROFILE (" +
                "name TEXT PRIMARY KEY, " +
                "gender INTEGER, " +
                "birthDate TEXT," +
                "weight TEXT," +
                "purpose TEXT," +
                "infection TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS TEMPDATA (" +
                "name TEXT, " +
                "tempValue DOUBLE, " +
                "tempDateTime DATETIME PRIMARY KEY);");

        db.execSQL("CREATE TABLE IF NOT EXISTS TIMELINEDATA (" +
                "name TEXT, " +
                "Value TEXT , " +
                "TimelineDateTime DATETIME PRIMARY KEY , " +
                "Source TEXT, " +
                "amount FLOAT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS OVULDATA (" +
                "name TEXT PRIMARY KEY, " +
                "period INT, " +
                "ovulDateTime DATETIME );");

        db.close();
    }

    public static Bodytemp_DBHelper getInstance(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        if (mInstance == null){
            mInstance = new Bodytemp_DBHelper(context, name, factory, version);
            writableDatabase = mInstance.getWritableDatabase();
            readableDataBase = mInstance.getReadableDatabase();
        }
        return mInstance;
    }

    /*
     * SQLiteOpenHelper의 onCreate()는 db.getWritableDatabase() 또는 db.getReadableDatabase()가
     * 실행될때 실행되기에 TABLE 생성 코드는 생성자에 넣었다.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public void ProfileDelete(String name){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;
        db.beginTransaction();
        db.execSQL("DELETE FROM USER_PROFILE WHERE name = '"+name+"'");
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    public void TempDelete(String name){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;
        db.beginTransaction();
        db.execSQL("DELETE FROM TEMPDATA WHERE name = '"+name+"'");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void TimeLineDelete(String name){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;
        db.beginTransaction();
        db.execSQL("DELETE FROM TIMELINEDATA WHERE name = '"+name+"'");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public MyProfile DBselect(String name){
        SQLiteDatabase db = Bodytemp_DBHelper.readableDataBase;
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM USER_PROFILE WHERE name = '"+name+"'", null);
        db.setTransactionSuccessful();
        db.endTransaction();

        MyProfile myProfile = null;
        while (cursor.moveToNext()){
            myProfile = new MyProfile(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5));
        }

        return myProfile;
    }


    public void closeDBHelper(){
        /*mInstance.close();
        writableDatabase.close();
        readableDataBase.close();*/
        mInstance = null;
        writableDatabase = null;
        readableDataBase = null;
    }
}
