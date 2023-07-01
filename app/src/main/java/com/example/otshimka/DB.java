package com.example.otshimka;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DB {
    String LOG_TAG = "MYlog";
    Context context;

    DBHelper dbHelper;
    SQLiteDatabase db;

    private String selTable = "t1";

    public DB(Context context){
        this.context=context;
        dbHelper = new DBHelper(this.context);
        db = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG,db.getPath());

    }

   public void add(String date,int step1, int step2,int step3,int step4,int step5,int step6){
       Log.d(LOG_TAG, "add: "+selTable);
        ContentValues cv = new ContentValues();

        cv.put("date", date);
        cv.put("step1", step1);
        cv.put("step2", step2);
        cv.put("step3", step3);
        cv.put("step4", step4);
        cv.put("step5", step5);
        cv.put("step6", step6);

        long rowID = db.insert( selTable, null, cv);
    }
   public ArrayList<Element> read(){
       Log.d(LOG_TAG, "read "+selTable);
        Cursor c = db.query( selTable, null, null, null, null, null, null);
        ArrayList<Element> rez = new ArrayList<Element>();

        if (c.moveToFirst()) {
            int dateCI = c.getColumnIndex("date");
            int step1CI = c.getColumnIndex("step1");
            int step2CI = c.getColumnIndex("step2");
            int step3CI = c.getColumnIndex("step3");
            int step4CI = c.getColumnIndex("step4");
            int step5CI = c.getColumnIndex("step5");
            int step6CI = c.getColumnIndex("step6");

            do {
                rez.add(new Element(
                         c.getString(dateCI)
                        ,c.getInt(step1CI)
                        ,c.getInt(step2CI)
                        ,c.getInt(step3CI)
                        ,c.getInt(step4CI)
                        ,c.getInt(step5CI)
                        ,c.getInt(step6CI)));
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return rez;
    }

    public int addTable(String name){
        try{
            Cursor c = db.query( "tablelist", null, null, null, null, null, null);
            c.moveToLast();
            int id = Integer.parseInt(c.getString(c.getColumnIndex("tableName")).replaceAll("t", ""))+1;
            Log.d(LOG_TAG, "addTable " + c.getString(c.getColumnIndex("tableName")));
            db.execSQL("create table "+("t"+id)+" ("
                    + "date text,"
                    + "step1 integer,"
                    + "step2 integer,"
                    + "step3 integer,"
                    + "step4 integer,"
                    + "step5 integer,"
                    + "step6 integer" + ");");

            ContentValues cv =new ContentValues();
            cv.put("name",name);
            cv.put("tableName","t"+id);
            db.insert("tablelist", null,cv);
            selTable="t"+(c.getCount()+1);

            return 0;
        }catch(Exception e){
            Log.d(LOG_TAG, "addTable "+e.getLocalizedMessage());
            return 1;
        }
    }
    public String[] getTables(){
        Cursor c = db.query( "tablelist", null, null, null, null, null, null);
        String rez[] = new String[c.getCount()];


        int i = 0;
        if (c.moveToFirst()) {
            int dateCI = c.getColumnIndex("name");
            do{
                rez[i]=c.getString(dateCI);
                Log.d(LOG_TAG, "zdes "+c.getString(c.getColumnIndex("tableName")));
                i++;
            }while (c.moveToNext());
        }else {Log.d(LOG_TAG, "0 rows (getTables)");}
        c.close();
        return rez;
    }

    public void selectTable(String name){
        selTable=findTableName(name);
        //Log.d(LOG_TAG, "selected table: "+name);
    }

    public void delTable(String name){
        db.execSQL("DROP TABLE "+findTableName(name)+";");
        db.execSQL("DELETE FROM tablelist WHERE name='"+name+"'");
    }

    public String getCurrentTable(){
        return selTable;
    }

    private String findTableName(String name){
        Cursor c = db.query( "tablelist", null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int tableNameCI = c.getColumnIndex("tableName");
            int nameCI = c.getColumnIndex("name");

            do{
                if(c.getString(nameCI).equals(name))return c.getString(tableNameCI);
            }while (c.moveToNext());
        }else {Log.d(LOG_TAG, "0 rows");}
        c.close();
        return null;
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table tablelist (name text,tableName text);");

            db.execSQL("create table t1 ("
                    + "date text,"
                    + "step1 integer,"
                    + "step2 integer,"
                    + "step3 integer,"
                    + "step4 integer,"
                    + "step5 integer,"
                    + "step6 integer" + ");");

            ContentValues cv =new ContentValues();
            cv.put("name","Подтягивания");
            cv.put("tableName","t1");
            db.insert("tablelist", null,cv);
            selTable="t1";

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
