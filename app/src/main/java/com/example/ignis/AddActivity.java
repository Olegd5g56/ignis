package com.example.ignis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private int step = 1;
    public final static String EXTRA_MESSAGE = "EXTRA_MESSAGE1";
    public final static String LOG_TAG = "AddTag";

    ArrayList<EditText> steps;
    DB db;
    String TableName;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    AFListener afListener;
    CountDownTimer cdt;
    EditText et_date;
    DateFormat dateFormat;

    SharedPreferences addActivityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(LOG_TAG, "onCreate: ");

        Intent intent = getIntent();
        TableName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        db = new DB(this);

        addActivityState = getSharedPreferences("AddActivityState", MODE_PRIVATE);

        mediaPlayer = MediaPlayer.create(this, R.raw.baraban);
        mediaPlayer.setOnCompletionListener(this);
        afListener = new AFListener(mediaPlayer, "Sound");

        et_date = findViewById(R.id.editTextDate2);
        Date currentDate = new Date();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        et_date.setText(dateFormat.format(currentDate));

        steps=new ArrayList<EditText>();

        steps.add(0,new EditText(this));
        steps.add(1,(EditText) findViewById(R.id.step1));
        steps.add(2,(EditText) findViewById(R.id.step2));
        steps.add(3,(EditText) findViewById(R.id.step3));
        steps.add(4,(EditText) findViewById(R.id.step4));
        steps.add(5,(EditText) findViewById(R.id.step5));
        steps.add(6,(EditText) findViewById(R.id.step6));

        steps.get(1).requestFocus();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        ArrayList<Element> list = db.read(TableName);
        Element lastElement = list.size() != 0 ? list.get(list.size()-1) : null;
        if(lastElement != null) {
            steps.get(1).setHint(lastElement.step1 + "");
            steps.get(2).setHint(lastElement.step2 + "");
            steps.get(3).setHint(lastElement.step3 + "");
            steps.get(4).setHint(lastElement.step4 + "");
            steps.get(5).setHint(lastElement.step5 + "");
            steps.get(6).setHint(lastElement.step6 + "");
        }else{
            for (EditText i : steps) {
                i.setHint("0");
            }
        }

        if(addActivityState.getAll().size() != 0 && addActivityState.contains("current_step") && addActivityState.getString("category","null").equals(TableName)){
            step=addActivityState.getInt("current_step",1);
            et_date.setText(addActivityState.getString("date","null"));
            steps.get(1).setText(addActivityState.getString("step1","null"));
            steps.get(2).setText(addActivityState.getString("step2","null"));
            steps.get(3).setText(addActivityState.getString("step3","null"));
            steps.get(4).setText(addActivityState.getString("step4","null"));
            steps.get(5).setText(addActivityState.getString("step5","null"));
            steps.get(6).setText(addActivityState.getString("step6","null"));

            for (int i = 1 ; i <= step ; i++){
                steps.get(i).setEnabled(true);
            }
            steps.get(step).requestFocus();
            if(step == 6) ((Button)findViewById(R.id.NextB)).setText("Write");
        }

        findViewById(R.id.skipB).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for(EditText e : steps){
                    e.setEnabled(true);
                    e.setHint("-");
                }
                Button b = (Button)findViewById(R.id.NextB);
                b.setText("Write");
                b.setEnabled(true);
                if(cdt!=null)cdt.cancel();
                TextView tv = (TextView)findViewById(R.id.TimerText);
                tv.setText("00:00");

                addActivityState.edit().clear().apply();

                return true;
            }
        });



    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        if(cdt!=null)cdt.cancel();
        if(db!=null)db.close();
    }

    public void onClick(View v) {
        Button b = (Button)v;
        switch (b.getText().toString()){
            case "Next":
                step++;
                if(step==2) {timer(5); b.setEnabled(false);}
                if(step==3){ timer(4); b.setEnabled(false);}
                if(step==4) {timer(3); b.setEnabled(false);}
                if(step==5){ timer(2); b.setEnabled(false);}
                if(step==6){ timer(1); b.setEnabled(false); b.setText("Write");}

                SharedPreferences.Editor prefEditor = addActivityState.edit();
                prefEditor.clear();
                prefEditor.putInt("current_step", step);
                prefEditor.putString("category", TableName);
                prefEditor.putString("date", et_date.getText().toString());
                prefEditor.putString("step1", steps.get(1).getText().toString());
                prefEditor.putString("step2", steps.get(2).getText().toString());
                prefEditor.putString("step3", steps.get(3).getText().toString());
                prefEditor.putString("step4", steps.get(4).getText().toString());
                prefEditor.putString("step5", steps.get(5).getText().toString());
                prefEditor.putString("step6", steps.get(6).getText().toString());
                prefEditor.apply();

                break;
            case "Write":
                boolean check = true;
                if(steps.get(1).getText().toString().equals(""))check = false;
                if(steps.get(2).getText().toString().equals(""))check = false;
                if(steps.get(3).getText().toString().equals(""))check = false;
                if(steps.get(4).getText().toString().equals(""))check = false;
                if(steps.get(5).getText().toString().equals(""))check = false;
                if(steps.get(6).getText().toString().equals(""))check = false;
                if(et_date.getText().toString().equals(""))check = false;
                try {
                    dateFormat.setLenient(false);
                    dateFormat.parse(et_date.getText().toString());
                } catch (ParseException e) {
                    check=false;
                }

                if (check) {
                    db.add(TableName
                            , et_date.getText().toString()
                            , Integer.parseInt(steps.get(1).getText().toString())
                            , Integer.parseInt(steps.get(2).getText().toString())
                            , Integer.parseInt(steps.get(3).getText().toString())
                            , Integer.parseInt(steps.get(4).getText().toString())
                            , Integer.parseInt(steps.get(5).getText().toString())
                            , Integer.parseInt(steps.get(6).getText().toString()));

                    addActivityState.edit().clear().apply();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(EXTRA_MESSAGE, TableName);
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "Не все поля заполнены правильно!!!", Toast.LENGTH_LONG).show();
                }
                break;
            case "Skip":
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Предупреждение")
                        .setMessage("Вы уверены?")
                        .setIcon(R.drawable.fire)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(cdt!=null){
                                    cdt.cancel();
                                    step_forward();
                                }
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
    }

    private void timer(int minuts){
        findViewById(R.id.skipB).setEnabled(true);
        if(cdt!=null){cdt.cancel(); cdt=null;}
        cdt = new CountDownTimer(minuts*60*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                int sec = (int)(millisUntilFinished / 1000);
                TextView tv = (TextView)findViewById(R.id.TimerText);
                String m = (sec/60) < 10 ? "0"+(sec/60) : ""+(sec/60);
                String s = (sec%60) < 10 ? "0"+(sec%60) : ""+(sec%60);
                tv.setText(m+":"+s);
            }

            public void onFinish() {
                int requestResult = audioManager.requestAudioFocus(afListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                Log.d(LOG_TAG, "Sound request focus, result: " + requestResult);
                mediaPlayer.start();
                step_forward();
            }
        }.start();
    }

    public void step_forward() {
        Log.d(LOG_TAG,"TimeEnd");
        findViewById(R.id.NextB).setEnabled(true);
        steps.get(step).setEnabled(true);
        steps.get(step).requestFocus();
        steps.get(step).setText("");

        TextView tv = (TextView)findViewById(R.id.TimerText);
        tv.setText("00:00");

        //findViewById(R.id.skipB).setEnabled(false);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "Music: abandon focus");
        audioManager.abandonAudioFocus(afListener);
    }

    class AFListener implements AudioManager.OnAudioFocusChangeListener {

        String label = "";
        MediaPlayer mp;

        public AFListener(MediaPlayer mp, String label) {
            this.label = label;
            this.mp = mp;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            String event = "";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    event = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    break;
            }
            Log.d(LOG_TAG, label + " onAudioFocusChange: " + event);
        }
    }




}