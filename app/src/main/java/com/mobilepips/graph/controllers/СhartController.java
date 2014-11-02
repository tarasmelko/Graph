package com.mobilepips.graph.controllers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.YPositionMetric;
import com.androidplot.ui.widget.Widget;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.mobilepips.graph.listeners.UpdateChartListener;
import com.mobilepips.graph.model.TimeFrame;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by gerardpalma on 30/10/2014.
 */
public class СhartController {

    private XYPlot plot;

    private ArrayList<String> currentTime;

    private Number[] time;
    private Number[] values;
    private Number[] last;
    private Number[] lastUp;
    private Number[] lastDown;

    private SeriesController seriesMain;
    private SeriesController lastPointSeries;
    private SeriesController lastPointSeriesUp;
    private SeriesController lastPointSeriesDown;

    private XValueMarker maxValueMarker;
    private XValueMarker minValueMarker;

    private UpdateChartListener updateListener;

    private boolean running = true;
    private boolean addUp = false;
    private boolean addDown = false;

    private int mChartTimeFrame = 60;
    private int EndPointOfGraph = mChartTimeFrame;

    private long timeForUpdate = 1000L;

    private static final int CHART_MAIN_TEXT_SIZE = 25;
    private static final int DOMAIN_STEP = 7;
    private static final int DOMAIN_VERTICAL_OFFSET = -40;
    private static final int GRID_PAD_TOP = 20;
    private static final int GRID_PAD_RIGHT = 80;
    private static final int GRID_PAD_LEFT = 0;
    private static final int GRID_PAD_BOT = 50;

    private static final String DECIMAL_TYPE = "##.###";
    private static final String DATA_FORMAT = "HH:mm:ss";
    private static final String UP_MARKER_TEXT = "UP";
    private static final String DOWN_MARKER_TEXT = "DOWN";

    public СhartController(XYPlot plot, UpdateChartListener updateListener, Context context) {
        this.plot = plot;
        this.updateListener = updateListener;
    }

    public void stopThread() {
        running = false;
    }

    public void setChartTime(TimeFrame tf) {
        switch (tf) {
            case ThirtySeconds:
                mChartTimeFrame = 60;
                break;
            case OneMinute:
                mChartTimeFrame = 120;
                break;
            case TwoMinute:
                mChartTimeFrame = 300;
                break;
            case FiveMinute:
                mChartTimeFrame = 300;
                break;
            default:
                break;
        }
    }


    public void formatChart() {
        setupPlot();
        setupSeries();
        startThread();
    }

    private void startThread() {
        Thread thread = new Thread(new UpdateEverySecond());
        thread.start();
    }

    private void setupPlot() {

        plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        plot.setGridPadding(GRID_PAD_LEFT, GRID_PAD_TOP, GRID_PAD_RIGHT, GRID_PAD_BOT);

        plot.setBackgroundColor(Color.WHITE);
        plot.getBackgroundPaint().setColor(Color.WHITE);

        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getDomainLabelPaint().setTextSize(CHART_MAIN_TEXT_SIZE);
        plot.getGraphWidget().setDomainLabelVerticalOffset(DOMAIN_VERTICAL_OFFSET);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);

        plot.setDomainStep(XYStepMode.SUBDIVIDE, DOMAIN_STEP);

        plot.getLayoutManager().remove(plot.getLegendWidget());
        plot.getLayoutManager().remove(plot.getDomainLabelWidget());
        plot.getLayoutManager().remove(plot.getRangeLabelWidget());
        plot.getLayoutManager().remove(plot.getTitleWidget());
        plot.getLayoutManager().remove(plot.getDomainLabelWidget());

        Widget gw = plot.getGraphWidget();
        SizeMetrics sm = new SizeMetrics(0, SizeLayoutType.FILL,
                0, SizeLayoutType.FILL);
        gw.setSize(sm);

        plot.setDomainValueFormat(new NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                return new StringBuffer(currentTime.get((int) value));
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                return null;
            }

            @Override
            public Number parse(String string, ParsePosition position) {
                return null;
            }
        });
    }

    private void setupSeries() {
        //setup main series
        seriesMain = new SeriesController(
                Arrays.asList(time),
                Arrays.asList(values));
        plot.addSeries(seriesMain, seriesMain.getmFormatter());

        //setup lat point marker
        lastPointSeries = new SeriesController(
                Arrays.asList(time),
                Arrays.asList(last), DECIMAL_TYPE,
                "LAST_DATA", Color.RED, CHART_MAIN_TEXT_SIZE);
        plot.addSeries(lastPointSeries, lastPointSeries.getmFormatter());

        //setup up marker
        lastPointSeriesUp = new SeriesController(
                Arrays.asList(time),
                Arrays.asList(lastUp),
                "MARKER_UP", Color.BLACK, CHART_MAIN_TEXT_SIZE, UP_MARKER_TEXT);
        plot.addSeries(lastPointSeriesUp, lastPointSeriesUp.getmFormatter());

        //setup down marker
        lastPointSeriesDown = new SeriesController(
                Arrays.asList(time),
                Arrays.asList(lastDown),
                "MARKER_DOWN", Color.GRAY, CHART_MAIN_TEXT_SIZE, DOWN_MARKER_TEXT);
        plot.addSeries(lastPointSeriesDown, lastPointSeriesDown.getmFormatter());
    }

    public void setupUp() {
        addUp = true;
    }

    public void setupDown() {
        addDown = true;
    }


    public class UpdateEverySecond implements Runnable {

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(timeForUpdate);
                    updateChart(updateListener.getPrice());
                }

            } catch (InterruptedException e) {


            }
        }
    }

    private void updateChart(Float val) {

        if (seriesMain != null && val != null && val != 0) {
            seriesMain.removeFirst();
            seriesMain.addLast(EndPointOfGraph, val);

            lastPointSeries.removeLast();
            lastPointSeries.addLast(EndPointOfGraph, val);

            if (lastPointSeriesUp.size() > 0 && lastPointSeriesUp.getX(0).intValue() == EndPointOfGraph - mChartTimeFrame)
                lastPointSeriesUp.removeFirst();
            if (addUp) {
                lastPointSeriesUp.addLast(EndPointOfGraph, val);
                addUp = false;
            }

            if (lastPointSeriesDown.size() > 0 && lastPointSeriesDown.getX(0).intValue() == EndPointOfGraph - mChartTimeFrame)
                lastPointSeriesDown.removeFirst();
            if (addDown) {
                lastPointSeriesDown.addLast(EndPointOfGraph, val);
                addDown = false;
            }

            plot.redraw();
            EndPointOfGraph++;
            setupMarkerValues(EndPointOfGraph);
            setupDomainStep();
        }
    }

    private void setupDomainStep() {
        currentTime.add(getTimeByCount(0));
    }

    private void setupMarkerValues(int endPointOfGraph) {

        if (maxValueMarker != null && minValueMarker != null) {
            plot.removeMarker(maxValueMarker);
            plot.removeMarker(minValueMarker);
        }

        maxValueMarker = new XValueMarker(endPointOfGraph, "0.0");
        maxValueMarker.getLinePaint().setAlpha(0);
        maxValueMarker.getTextPaint().setColor(Color.BLACK);
        maxValueMarker.getTextPaint().setTextSize(CHART_MAIN_TEXT_SIZE);

        maxValueMarker.setTextPosition(new YPositionMetric(0, YLayoutStyle.ABSOLUTE_FROM_TOP));
        plot.addMarker(maxValueMarker);

        minValueMarker = new XValueMarker(endPointOfGraph, "0.0");
        minValueMarker.getLinePaint().setAlpha(0);
        minValueMarker.getTextPaint().setColor(Color.BLACK);
        minValueMarker.getTextPaint().setTextSize(CHART_MAIN_TEXT_SIZE);

        minValueMarker.setTextPosition(new YPositionMetric(0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM));
        plot.addMarker(minValueMarker);

        maxValueMarker.setText("Max: " + String.valueOf(plot.getCalculatedMaxY().floatValue()));
        minValueMarker.setText("Min: " + String.valueOf(plot.getCalculatedMinY().floatValue()));


    }

    public void loadInitialData(Number[] response) {

        values = new Number[mChartTimeFrame];
        time = new Number[mChartTimeFrame];
        last = new Number[mChartTimeFrame];
        lastUp = new Number[mChartTimeFrame];
        lastDown = new Number[mChartTimeFrame];
        currentTime = new ArrayList<String>(mChartTimeFrame);

        int counter = 0;
        for (int i = response.length - mChartTimeFrame; i < response.length; i++) {
            values[counter] = response[i];
            time[counter] = counter;
            counter++;
        }

        int frame = response.length - mChartTimeFrame;
        for (int i = response.length; i > response.length - mChartTimeFrame; i--) {
            currentTime.add(getTimeByCount(frame));
            frame--;
        }
        formatChart();
    }

    private String getTimeByCount(long counter) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATA_FORMAT);
        long curTime = System.currentTimeMillis();
        return formatter.format(curTime - (counter * 1000));
    }
}




