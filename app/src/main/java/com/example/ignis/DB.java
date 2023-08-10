package com.example.ignis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

class DB extends SQLiteOpenHelper {
    private static final String LOG_TAG = "IGNIS_DB_LOG";
    private SQLiteDatabase db;
    public DB(Context context) {
        super(context, "ignis.db", null, 3);
        db=this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {Log.d(LOG_TAG, "--- onCreate database ---");}
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "--- onUpgrade database ---");
        if(oldVersion==2 && newVersion==3){
            for (String table : getTableNames(db)){
                db.execSQL("CREATE TABLE temp_table AS SELECT * FROM `"+table+"`");
                // Удаление старой таблицы
                db.execSQL("DROP TABLE IF EXISTS `"+table+"`");
                // Создание новой таблицы с новой схемой, включая столбец id
                db.execSQL("create table `"+table+"` ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "date text,"
                        + "step1 integer,"
                        + "step2 integer,"
                        + "step3 integer,"
                        + "step4 integer,"
                        + "step5 integer,"
                        + "step6 integer" + ");");
                // Копирование данных из временной таблицы в новую таблицу
                db.execSQL("INSERT INTO `"+table+"` (date,step1,step2,step3,step4,step5,step6) SELECT date,step1,step2,step3,step4,step5,step6 FROM temp_table");
                // Удаление временной таблицы
                db.execSQL("DROP TABLE IF EXISTS temp_table");

                Cursor c = db.rawQuery("SELECT * FROM `"+table+"`", null);
                int idCounter = 1;
                if (c.moveToFirst()) {
                    do {
                        int dateColumIndex = c.getColumnIndex("date");
                        db.execSQL("UPDATE `"+table+"` SET id="+idCounter+" WHERE date = '"+c.getString(dateColumIndex)+"'");
                        idCounter++;
                    } while (c.moveToNext());
                } else
                    Log.d(LOG_TAG, "0 rows");
                c.close();
            }

        }
    }

    public void add(String category ,String date,int step1, int step2,int step3,int step4,int step5,int step6){
        Log.d(LOG_TAG, "add to "+category);
        ArrayList<Element> data = read(category);

        //int id = data.size() == 0 ? 0 : data.get(data.size()-1).id+1;

        ContentValues cv = new ContentValues();
        //cv.put("id", id);
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
    public int addTable(String category){
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