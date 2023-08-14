package com.example.ignis;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

class DB extends SQLiteOpenHelper {
    private static final String LOG_TAG = "IGNIS_DB_LOG";
    public static final String dbName = "ignis.db";
    private final Object lock = new Object();
    private SQLiteDatabase db;
    Context context;
    public DB(Context context) {
        super(context, dbName, null, 3);
        this.context=context;
        db=this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {Log.d(LOG_TAG, "--- onCreate database ---");}
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "--- onUpgrade database ---");
            if (oldVersion == 2 && newVersion == 3) {
                Log.d(LOG_TAG, "Upgrade from 2 to 3");
                for (String table : getTableNames(db)) {
                    db.execSQL("CREATE TABLE temp_table AS SELECT * FROM `" + table + "`");
                    db.execSQL("DROP TABLE IF EXISTS `" + table + "`");
                    addTable(db, table);
                    db.execSQL("INSERT INTO `" + table + "` (date,step1,step2,step3,step4,step5,step6) SELECT date,step1,step2,step3,step4,step5,step6 FROM temp_table");
                    db.execSQL("DROP TABLE IF EXISTS temp_table");
                }
            } else if (oldVersion == 1 && newVersion == 3) {
                Log.d(LOG_TAG, "Upgrade from 1 to 3");
                Cursor c = db.rawQuery("SELECT * FROM tablelist", null);
                if (c.moveToFirst()) {
                    do {
                        String category_name = c.getString(0);
                        String table_name = c.getString(1);
                        addTable(db, category_name);
                        db.execSQL("INSERT INTO `" + category_name + "` (date,step1,step2,step3,step4,step5,step6) SELECT date,step1,step2,step3,step4,step5,step6 FROM " + table_name);
                        db.execSQL("DROP TABLE IF EXISTS " + table_name);
                    } while (c.moveToNext());
                } else
                    Log.d(LOG_TAG, "0 rows");
                c.close();
                db.execSQL("DROP TABLE IF EXISTS tablelist");
            } else {
                Log.e(LOG_TAG, "Upgrade not supported! oldVersion=" + oldVersion + "; newVersion=" + newVersion);
            }
    }

    public void add(String category ,String date,int step1, int step2,int step3,int step4,int step5,int step6){
        Log.d(LOG_TAG, "add to "+category);

        ContentValues cv = new ContentValues();
        cv.put("date", date);
        cv.put("step1", step1);
        cv.put("step2", step2);
        cv.put("step3", step3);
        cv.put("step4", step4);
        cv.put("step5", step5);
        cv.put("step6", step6);
        db.insert( "`"+category+"`", null, cv);
    }
    public void del(String category, int id){
        db.execSQL("DELETE FROM `"+category+"` WHERE id="+id);
    }
    public int addCategory(String category){
        return addTable(db,category);
    }
    private int addTable(SQLiteDatabase db, String category){
        try{
            db.execSQL("create table `"+category+"` ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "date text,"
                    + "step1 integer,"
                    + "step2 integer,"
                    + "step3 integer,"
                    + "step4 integer,"
                    + "step5 integer,"
                    + "step6 integer" + ");");
            return 0;
        }catch(Exception e){
            Log.e(LOG_TAG, "addTable: "+e.getLocalizedMessage());
            return 1;
        }
    }
    public void delTable(String category){
        db.execSQL("DROP TABLE `"+category+"`;");
    }

    public Element get(String category, int id){
        Cursor c = db.rawQuery("SELECT * FROM `"+category+"` WHERE id="+id, null);
        c.moveToFirst();
        c.getString(0);
        Element rez = new Element(
                c.getInt(0)
                ,c.getString(1)
                ,c.getInt(2)
                ,c.getInt(3)
                ,c.getInt(4)
                ,c.getInt(5)
                ,c.getInt(6)
                ,c.getInt(7));
        c.close();

        return rez;
    }
    public ArrayList<Element> read(String category){
        Log.d(LOG_TAG, "read "+category);

        Cursor c = db.rawQuery("SELECT * FROM `"+category+"`", null);
        ArrayList<Element> rez = new ArrayList<Element>();

        if (c.moveToFirst()) {
            do {
                rez.add(new Element(
                        c.getInt(0)
                        ,c.getString(1)
                        ,c.getInt(2)
                        ,c.getInt(3)
                        ,c.getInt(4)
                        ,c.getInt(5)
                        ,c.getInt(6)
                        ,c.getInt(7)));
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return rez;
    }

    public String[] getCategoryList(){
        return getTableNames(db);
    }
    private String[] getTableNames(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' AND name!='sqlite_sequence'", null);
        String rez[] = new String[c.getCount()];
        int i = 0;

        try {
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    rez[i] = c.getString(0);
                    i++;
                    c.moveToNext();
                }
            }
        } finally {
            c.close();
        }
        return rez;
    }
}