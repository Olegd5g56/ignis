package com.example.otshimka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GraphActivity extends AppCompatActivity {

    public final static String LOG_TAG = "GraphTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        Intent intent = getIntent();
        String TableName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Log.d(LOG_TAG,TableName+" ----------------");
        DB db = new DB(this);
        db.selectTable(TableName);
        ArrayList<Element> allData = db.read();


        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setScalable(true);


        DataPoint[] data = new DataPoint[allData.size()];
        for(int i = 0; i < (allData.size()); i++){
           Element e = allData.get(i);
            try {
                data[i] = new DataPoint(format.parse(e.date), e.step1+e.step2+e.step3+e.step4+e.step5+e.step6);
               // Log.d(LOG_TAG,e.date+" ----------------");
            } catch (ParseException ex) {
                ex.printStackTrace();
               // Log.d(LOG_TAG,e.date+" error");
            }
            Log.d(LOG_TAG,i+"");
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);

        series.setDrawDataPoints(true);
        graph.addSeries(series);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);

        try {
        graph.getViewport().setMinX(format.parse(allData.get(0).date).getTime());
        graph.getViewport().setMaxX(format.parse(allData.get(allData.size()-1).date).getTime());
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}
