package com.mobilepips.graph;

import android.app.Activity;
import android.os.Bundle;

import com.androidplot.xy.XYPlot;

import java.util.Random;


public class MyActivity extends Activity implements UpdateChart {

    static float counter = (float) 1.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        XYPlot plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        MyChart mChart = new MyChart(plot, this, getApplicationContext());
        mChart.loadInitialData(generateRandom(100));
    }

    private Number[] generateRandom(int size) {

        Number[] initialDataSet = new Number[size];

        Random randomGenerator = new Random();
        for (int i = 0; i < size; ++i) {
            float randomInt = randomGenerator.nextFloat();
            initialDataSet[i] = randomInt;
        }

        return initialDataSet;
    }

    @Override
    public float getPrice() {
        Random randomGenerator = new Random();
        float count = randomGenerator.nextFloat();
        return count;
    }


}
