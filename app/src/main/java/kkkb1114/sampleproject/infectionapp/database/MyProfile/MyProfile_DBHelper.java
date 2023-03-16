package kkkb1114.sampleproject.infectionapp.database.MyProfile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import kkkb1114.sampleproject.infectionapp.database.Bodytemp_DBHelper;

public class MyProfile_DBHelper {

    public MyProfile_DBHelper(){

    }

    public void DBinsert(MyProfile myProfile){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;

        db.beginTransaction();
        db.execSQL("INSERT INTO USER_PROFILE VALUES (" +
                "'"+myProfile.name+"'," +
                ""+myProfile.gender+"," +
                "'"+myProfile.birthDate+"'," +
                "'"+ myProfile.weight +"'," +
                "'"+ myProfile.purpose +"'," +
                "'"+ myProfile.infection +"');");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void DBupdate(MyProfile myProfile){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;
        db.beginTransaction();
        db.execSQL(
                "UPDATE USER_PROFILE SET " +
                "gender = "+myProfile.gender+"," +
                "birthDate = '"+myProfile.birthDate+"'," +
                "weight = '"+myProfile.weight+"'," +
                        "purpose = '"+myProfile.purpose+"'," +
                        "infection = '"+myProfile.infection+"'" +
                        " WHERE name = '"+myProfile.name+"'"
                );
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void DBdelete(MyProfile myProfile){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;
        db.beginTransaction();
        db.execSQL("DELETE FROM USER_PROFILE WHERE name = '"+myProfile.name+"'");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public ArrayList<MyProfile> DBselectAll(){
        ArrayList<MyProfile> myProfiles = new ArrayList<>();

        SQLiteDatabase db = Bodytemp_DBHelper.readableDataBase;
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM USER_PROFILE", null);
        db.setTransactionSuccessful();
        db.endTransaction();

        return getMyProfileCursor(cursor, myProfiles);
    }

    public MyProfile DBselect(String name){
        SQLiteDatabase db = Bodytemp_DBHelper.readableDataBase;
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM USER_PROFILE WHERE name = '"+name+"'", null);

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

        db.setTransactionSuccessful();
        db.endTransaction();

        return myProfile;
    }

    public ArrayList<MyProfile> getMyProfileCursor(Cursor cursor, ArrayList<MyProfile> myProfiles){
        while (cursor.moveToNext()){
            MyProfile myProfile = new MyProfile(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5));
            myProfiles.add(myProfile);
        }
        cursor.close();
        return myProfiles;
    }
}
