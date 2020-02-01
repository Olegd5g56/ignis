package com.example.otshimka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements MyTimer.MyTimerInterface,OnCompletionListener  {

    private int step = 1;
    public final static String EXTRA_MESSAGE = "EXTRA_MESSAGE1";
    public final static String LOG_TAG = "AddTag";

    ArrayList<EditText> steps;
   // MyTimer myTimer;
    DB db;
    String TableName;
    AudioManager audioManager;
    AFListener afListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        TableName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        db = new DB(this);
        db.selectTable(TableName);

        ArrayList<Element> allData = db.read();
        if(allData.size()>0) {
            Element e = allData.get(allData.size() - 1);
            TextView t = (TextView) findViewById(R.id.lastrez);
            t.setText(e.date + ": " + e.step1 + "+" + e.step2 + "+" + e.step3 + "+" + e.step4 + "+" + e.step5 + "+" + e.step6 + "=" + (e.step1 + e.step2 + e.step3 + e.step4 + e.step5 + e.step6));
        }


        steps=new ArrayList<EditText>();

        steps.add(0,new EditText(this));
        steps.add(1,(EditText) findViewById(R.id.step1));
        steps.add(2,(EditText) findViewById(R.id.step2));
        steps.add(3,(EditText) findViewById(R.id.step3));
        steps.add(4,(EditText) findViewById(R.id.step4));
        steps.add(5,(EditText) findViewById(R.id.step5));
        steps.add(6,(EditText) findViewById(R.id.step6));

        //myTimer = new MyTimer(this,(TextView)findViewById(R.id.TimerText));

        steps.get(1).requestFocus();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
                break;
            case "Write":

                boolean check = true;
                if(steps.get(1).getText().toString().equals(""))check = false;
                if(steps.get(2).getText().toString().equals(""))check = false;
                if(steps.get(3).getText().toString().equals(""))check = false;
                if(steps.get(4).getText().toString().equals(""))check = false;
                if(steps.get(5).getText().toString().equals(""))check = false;
                if(steps.get(6).getText().toString().equals(""))check = false;

                if (check) {
                    Date currentDate = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                    db.add(dateFormat.format(currentDate)
                            , Integer.parseInt(steps.get(1).getText().toString())
                            , Integer.parseInt(steps.get(2).getText().toString())
                            , Integer.parseInt(steps.get(3).getText().toString())
                            , Integer.parseInt(steps.get(4).getText().toString())
                            , Integer.parseInt(steps.get(5).getText().toString())
                            , Integer.parseInt(steps.get(6).getText().toString()));

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(EXTRA_MESSAGE, TableName);
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "Не все поля заполнены!!!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    private void timer(int minuts){
        CountDownTimer cdt = new CountDownTimer(minuts*60*1000, 1000) {//minuts*60*1000
            public void onTick(long millisUntilFinished) {
                int sec = (int)(millisUntilFinished / 1000);
                TextView tv = (TextView)findViewById(R.id.TimerText);
                // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                tv.setText((sec/60)+":"+(sec%60));
            }

            public void onFinish() {
                //mTextField.setText("done!");
                TimeEnd();
            }
        }.start();

    }
    @Override
    public void TimeEnd() {
        Log.d("MYlog","lolll");
        findViewById(R.id.MainButton).setEnabled(true);
        steps.get(step).setEnabled(true);
        steps.get(step).requestFocus();
        steps.get(step).setText("");

        int durationHint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.baraban);
        mediaPlayer.setOnCompletionListener(this);
        afListener = new AFListener(mediaPlayer, "Sound");
        int requestResult = audioManager.requestAudioFocus(afListener, AudioManager.STREAM_MUSIC, durationHint);
        Log.d(LOG_TAG, "Sound request focus, result: " + requestResult);
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
            Log.d(LOG_TAG, "Music: abandon focus");
            audioManager.abandonAudioFocus(afListener);
    }


    class AFListener implements OnAudioFocusChangeListener {

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
