package com.mobilepips.graph;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.androidplot.xy.XYPlot;
import com.mobilepips.graph.controllers.СhartController;
import com.mobilepips.graph.listeners.UpdateChartListener;

import java.util.Random;


public class MyActivity extends Activity implements UpdateChartListener, View.OnClickListener {

    private int randomCount = 100;
    private СhartController mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        initChart();
        initBtn();
    }

    private void initChart() {
        XYPlot plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        mChart = new СhartController(plot, this, getApplicationContext());
        mChart.loadInitialData(generateRandom(randomCount));
    }

    private void initBtn() {
        findViewById(R.id.down_btn).setOnClickListener(this);
        findViewById(R.id.up_btn).setOnClickListener(this);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.up_btn:
                mChart.setupUp();
                break;
            case R.id.down_btn:
                mChart.setupDown();
                break;
        }
    }
}
