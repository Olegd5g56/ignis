package com.example.otshimka;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class MyTimer {
    interface MyTimerInterface{
        void TimeEnd();
    }

    TextView tv;
    MyTimerInterface mti;

    public MyTimer(MyTimerInterface mti,TextView tv){
        this.mti=mti;
        this.tv=tv;
    }

    public void start(int minuts){
        MyTimerTask catTask = new MyTimerTask();
        catTask.execute(minuts);
    }

    class MyTimerTask extends AsyncTask<Integer, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... sec) {
            int s=0;
            int target = sec[0]*60;
            while(target>=s){
                try {
                    publishProgress(target);
                    Log.d("MYlog",s+"");
                    Thread.sleep(990);
                    target--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            tv.setText((values[0]/60)+":"+(values[0]%60));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mti.TimeEnd();
        }

    }




}
