package com.example.otshimka;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "MYlog";
    public final static String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    DB db;
    LinearLayout list;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        String TableName = intent.getStringExtra(AddActivity.EXTRA_MESSAGE);

        db = new DB(this);
        list = (LinearLayout) findViewById(R.id.list);
        spinner = (Spinner) findViewById(R.id.spinner);

        spinnerUpdate(TableName);

        draw(db.read());


    }

    private void draw(ArrayList<Element> data){
       list.removeAllViews();
       for(Element e : data){
           TextView t = new TextView(this);
          // t.setText(e.date+": "+e.step1+"+"+e.step2+"+"+e.step3+"+"+e.step4+"+"+e.step5+"+"+e.step6+"="+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
           t.setText(e.date+": "+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
           t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
           t.setHeight(65);
           t.setTextSize(25);
           t.setLines(1);
           t.setTextColor(Color.CYAN);
           t.setOnClickListener(onClickEl);
           list.addView(t);
       }
    }

    private void alert(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Содержание")
                .setMessage(msg)
                .setIcon(R.drawable.fire)
                .setCancelable(false)
                .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void spinnerUpdate(String sellName){
        final String[] data =  db.getTables();
        MyCustomAdapter adapter = new MyCustomAdapter(this, R.id.weekofday, data);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Title");

        if(sellName==null) {
            spinner.setSelection(0);
        }else{
            int i=0;
            for(String name : data){
                if(sellName.equals(name))spinner.setSelection(i);
                i++;
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //  TextView t = (TextView) view;
               db.selectTable(data[position]);
               draw(db.read());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

    }
    public void onClick(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.myrotate));
        switch (v.getId()) {
            case R.id.AddButton:
              Intent intent = new Intent(this, AddActivity.class);
              intent.putExtra(EXTRA_MESSAGE, spinner.getSelectedItem().toString());//
              startActivity(intent);
            break;
            case R.id.Newbutton:
                if(findViewById(R.id.editText).getVisibility()==View.INVISIBLE) {
                    findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
                    findViewById(R.id.editText).setVisibility(View.VISIBLE);
                    findViewById(R.id.editText).requestFocus();
                }else{
                    findViewById(R.id.spinner).setVisibility(View.VISIBLE);
                    findViewById(R.id.editText).setVisibility(View.INVISIBLE);

                    EditText et = (EditText)findViewById(R.id.editText);
                    if(!et.getText().toString().equals("")){
                        db.addTable(et.getText().toString());
                        db.selectTable(et.getText().toString());
                        draw(db.read());
                        spinnerUpdate(et.getText().toString());
                    }
                }
            break;
            case R.id.graphB:
                Intent intent1 = new Intent(this, GraphActivity.class);
                intent1.putExtra(EXTRA_MESSAGE, spinner.getSelectedItem().toString());
                startActivity(intent1);
            break;

        }
    }
    View.OnClickListener onClickEl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView t = (TextView) v;
            Element e = db.find(t.getText().toString().split(":")[0]);
            if(e != null) {
                alert(e.step1+"+"+e.step2+"+"+e.step3+"+"+e.step4+"+"+e.step5+"+"+e.step6+"="+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
            }else{
                alert("Ошибка: не удалось найти елемент!");
            }
        }
    };

    View.OnClickListener onClickDel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String tag =  v.getTag().toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Предупреждение")
                    .setMessage("Вы уверены?")
                    .setIcon(R.drawable.fire)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.delTable(tag);
                            spinnerUpdate("Подтягивания");
                        }
                    })
                    .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            //.setNegativeButton("Cancel", diclCancel);
            AlertDialog alert = builder.create();
            alert.show();

        }
    };





    public class MyCustomAdapter extends ArrayAdapter<String> {

        private String[] data = null;
        public MyCustomAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
            data=objects;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.weekofday);
            label.setText(data[position]);

            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            icon.setOnClickListener(onClickDel);
            icon.setTag(data[position]);

            return row;
        }
    }






}
