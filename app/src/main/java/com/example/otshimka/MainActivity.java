package com.example.otshimka;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "MYlog";
    public final static String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private static final int PICK_DB_FILE = 1;
    private static final int PICK_EXPORT_PATH = 2;

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
       int i = 0;
       for(Element e : data){
           TextView t = new TextView(this);
          // t.setText(e.date+": "+e.step1+"+"+e.step2+"+"+e.step3+"+"+e.step4+"+"+e.step5+"+"+e.step6+"="+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
           t.setText(e.date+": "+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
           t.setTag(i);
           t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
           t.setHeight(65);
           t.setTextSize(25);
           t.setLines(1);
           t.setTextColor(Color.CYAN);
           t.setOnClickListener(onClickEl);
           list.addView(t);
           i++;
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
            case R.id.importButton:
                Intent open_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                open_intent.addCategory(Intent.CATEGORY_OPENABLE);
                open_intent.setType("*/*");
                startActivityForResult(open_intent,  PICK_DB_FILE);
            break;
            case R.id.exportButton:
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),  PICK_EXPORT_PATH);
            break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DB_FILE && resultCode == RESULT_OK && data != null) {
            Uri source = data.getData(); // URI исходного файла
            File destination = this.getDatabasePath("myDB"); // Файл назначения, куда нужно скопировать
            Log.d(LOG_TAG, "Result URI: " + source);

            try {
                FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(source);
                FileOutputStream outputStream = new FileOutputStream(destination);

                FileChannel inChannel = inputStream.getChannel();
                FileChannel outChannel = outputStream.getChannel();

                inChannel.transferTo(0, inChannel.size(), outChannel);

                outputStream.close();
                inputStream.close();

                // Файл успешно скопирован
                spinnerUpdate(null);
                draw(db.read());
                alert("Импортировано");
                Log.i(LOG_TAG, "Database imported successfully to: " + destination.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                alert("Ошибка импортирования");
            }
        }

        if (requestCode == PICK_EXPORT_PATH && resultCode == RESULT_OK && data != null) {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, data.getData());
            DocumentFile destination = pickedDir.createFile("application/octet-stream", "ignis.db");

            File source = this.getDatabasePath("myDB");
            Log.d(LOG_TAG, "Result URI: " + destination.getUri());

            try {
                FileInputStream inputStream = new FileInputStream(source);
                FileOutputStream outputStream = (FileOutputStream) getContentResolver().openOutputStream(destination.getUri());

                FileChannel inChannel = inputStream.getChannel();
                FileChannel outChannel = outputStream.getChannel();

                inChannel.transferTo(0, inChannel.size(), outChannel);

                outputStream.close();
                inputStream.close();

                // Файл успешно скопирован
                alert("Экспортировано");
                Log.i(LOG_TAG, "Database exported successfully to: " + destination.getUri());
            } catch (IOException e) {
                e.printStackTrace();
                alert("Ошибка экспорта");
            }
        }

    }




    View.OnClickListener onClickEl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView t = (TextView) v;
            Element e = db.read().get((int)t.getTag());
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
