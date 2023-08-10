package com.example.ignis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "IGNIS_MA_log";
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

        if(spinner.getCount() > 0) draw(db.read(spinner.getSelectedItem().toString()));
    }

    private void draw(ArrayList<Element> data){
        list.removeAllViews();
        if(data == null) return;

        Comparator<Element> dateComparator = new Comparator<Element>() {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

            @Override
            public int compare(Element e1, Element e2) {
                try {
                    Date d1 = sdf.parse(e1.date);
                    Date d2 = sdf.parse(e2.date);
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };

        Collections.sort(data, dateComparator);

        for(Element e : data){
            TextView t = new TextView(this);
            t.setText(e.date+": "+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
            t.setTag(e.id);
            t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            t.setHeight(65);
            t.setTextSize(25);
            t.setLines(1);
            t.setTextColor(Color.CYAN);
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) v;
                    Element e = db.get(spinner.getSelectedItem().toString(),(int)t.getTag());
                    if(e != null) {
                        alert(e.step1+"+"+e.step2+"+"+e.step3+"+"+e.step4+"+"+e.step5+"+"+e.step6+"="+(e.step1+e.step2+e.step3+e.step4+e.step5+e.step6));
                    }else{
                        alert("Ошибка: не удалось найти елемент!");
                    }
                }
            });
            t.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TextView t = (TextView) v;
                    Element e = db.get(spinner.getSelectedItem().toString(),(int)t.getTag());
                    if(e != null) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Предупреждение")
                                .setMessage("Вы действительно хотите удалить результат: \""+t.getText()+"\"?")
                                .setIcon(R.drawable.fire)
                                .setCancelable(false)
                                .setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.del(spinner.getSelectedItem().toString(),(int)t.getTag());
                                        draw( db.read(spinner.getSelectedItem().toString()) );
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Отмена", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }else{
                        alert("Ошибка: не удалось найти елемент!");
                    }
                    return false;
                }
            });
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
        final String[] data =  db.getCategoryList();

        if(data.length == 0) draw(null);

        MyCustomAdapter adapter = new MyCustomAdapter(this, R.id.row_layout, data);
        spinner.setDropDownVerticalOffset(0);

        spinner.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal());

        spinner.setAdapter(adapter);
        spinner.setPrompt("Title");

        if(sellName==null) {
            spinner.setSelection(0);
        }else{
            int i=0;
            for(String name : data){
                if(sellName.equals(name)){
                    spinner.setSelection(i);
                    break;
                }
                i++;
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                draw(db.read(data[position]));
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

    }
    public void onClick(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.myrotate));
            if(v.getId() == R.id.AddButton && spinner.getSelectedItem() != null ) {
                Intent intent = new Intent(this, AddActivity.class);
                intent.putExtra(EXTRA_MESSAGE, spinner.getSelectedItem().toString());//
                startActivity(intent);
            }else if (v.getId() == R.id.NewButton) {
                if (findViewById(R.id.editText).getVisibility() == View.INVISIBLE) {
                    findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
                    findViewById(R.id.editText).setVisibility(View.VISIBLE);
                    findViewById(R.id.editText).requestFocus();
                } else {
                    findViewById(R.id.spinner).setVisibility(View.VISIBLE);
                    findViewById(R.id.editText).setVisibility(View.INVISIBLE);

                    EditText et = (EditText) findViewById(R.id.editText);
                    if (!et.getText().toString().equals("")) {
                        db.addTable(et.getText().toString());
                        draw(db.read(et.getText().toString()));
                        spinnerUpdate(et.getText().toString());
                        et.setText("");
                    }
                }
            }else if (v.getId() == R.id.DelButton) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Предупреждение")
                        .setMessage("Вы уверены?")
                        .setIcon(R.drawable.fire)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.delTable(spinner.getSelectedItem().toString());
                                spinnerUpdate(spinner.getSelectedItem().toString());
                            }
                        })
                        .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }else if (v.getId() == R.id.importButton) {
                Intent open_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                open_intent.addCategory(Intent.CATEGORY_OPENABLE);
                open_intent.setType("*/*");
                startActivityForResult(open_intent, PICK_DB_FILE);
            }else if (v.getId() == R.id.exportButton) {
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), PICK_EXPORT_PATH);
            }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DB_FILE && resultCode == RESULT_OK && data != null) {
            Uri source = data.getData(); // URI исходного файла
            //File destination = this.getDatabasePath(db.getWritableDatabase().getPath()); // Файл назначения, куда нужно скопировать
            File destination = new File(db.getWritableDatabase().getPath()); // Файл назначения, куда нужно скопировать
            Log.d(LOG_TAG, "Result URI: " + source);
            Log.d(LOG_TAG, "BD path: " + db.getWritableDatabase().getPath());

            db.getWritableDatabase().close();

            try {
                FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(source);
                FileOutputStream outputStream = new FileOutputStream(destination);

                FileChannel inChannel = inputStream.getChannel();
                FileChannel outChannel = outputStream.getChannel();

                inChannel.transferTo(0, inChannel.size(), outChannel);

                outputStream.close();
                inputStream.close();

                db = new DB(this);
                // Файл успешно скопирован
                spinnerUpdate(null);
                draw(db.read(db.getCategoryList()[0]));
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

            File source = new File(db.getWritableDatabase().getPath());//File source = this.getDatabasePath("myDB");
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

            return row;
        }
    }


}