package kkkb1114.sampleproject.infectionapp.database.TempData;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import kkkb1114.sampleproject.infectionapp.database.Bodytemp_DBHelper;

public class TempData_DBHelper {

    public TempData_DBHelper(){

    }


    public void DBdelete(TempData tempData){
        SQLiteDatabase db = Bodytemp_DBHelper.writableDatabase;
        db.beginTransaction();
        db.execSQL("DELETE FROM USER_PROFILE WHERE name = '"+tempData.name+"'");
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public TempData DBselectDel(String name){
        SQLiteDatabase db = Bodytemp_DBHelper.readableDataBase;
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM TEMPDATA WHERE name = '"+name+"'", null);
        db.setTransactionSuccessful();
        db.endTransaction();

        TempData tempData = null;
        while (cursor.moveToNext()){
            tempData = new TempData(
                    cursor.getString(0),
                    cursor.getDouble(2),
                    cursor.getString(3));

        }

        return tempData;
    }

}
