package com.example.otshimka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
           t.setText(e.date+": "+e.step1+"+"+e.step2+"+"+e.step3+"+"+e.step4+"+"+e.step5+"+"+e.step6+"="+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
           t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
           t.setHeight(65);
           t.setTextSize(20);
           t.setLines(1);
           t.setTextColor(Color.CYAN);
           list.addView(t);
       }
    }

    private void spinnerUpdate(String sellName){
        final String[] data =  db.getTables();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.weekofday, data);
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

        }
    }


}
