package com.mobilepips.graph;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by gerardpalma on 30/10/2014.
 */
public class MyChart {


    private XYPlot plot;

    private String[] time3;
    private long[] time2;
    private Number[] time;
    private Number[] values;
    private Number[] last;
    private SimpleXYSeries series;
    private SimpleXYSeries lastPointSeries;
    private UpdateChart updateListener;
    private Context mContext;

    private long mMarketId;
    private static final String TAG_MARKET_CHART = "TAG_MARKET_CHART";
    private boolean running = true;
    private XValueMarker currentPriceMarker;
    private XValueMarker maxValueMarker;
    private XValueMarker minValueMarker;
    private int mChartTimeFrame = 60;
    private int EndPointOfGraph = mChartTimeFrame;


    public void stopThread() {
        running = false;
    }

    public MyChart(XYPlot plot, UpdateChart updateListener, Context context) {
        this.plot = plot;
        mContext = context;
        this.updateListener = updateListener;
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
        plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        plot.setPlotMargins(0, 0, 0, 0);
        plot.setPlotPadding(0, 0, 0, 0);
        plot.setGridPadding(0, 100, 100, 50);
        plot.setBackgroundColor(Color.WHITE);

        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.TRANSPARENT);
//        plot.getGraphWidget().setRangeLabelHorizontalOffset(-100);
        plot.getGraphWidget().getDomainLabelPaint().setTextSize(25);
        plot.getGraphWidget().setDomainLabelVerticalOffset(-40);

        //   mySimpleXYPlot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        //Remove legend
        plot.getLayoutManager().remove(plot.getLegendWidget());
        plot.getLayoutManager().remove(plot.getDomainLabelWidget());
        plot.getLayoutManager().remove(plot.getRangeLabelWidget());
        plot.getLayoutManager().remove(plot.getTitleWidget());

        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

//This gets rid of the black border (up to the graph) there is no black border around the labels
        plot.getBackgroundPaint().setColor(Color.WHITE);

//This gets rid of the black behind the graph
        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        series = new SimpleXYSeries(
                Arrays.asList(time),
                Arrays.asList(values),
                "Series1");
        lastPointSeries = new SimpleXYSeries(
                Arrays.asList(time),
                Arrays.asList(last),
                "Series2");


        // Set the display title of the series
//        plot.getGraphWidget().setDomainValueFormat(new GraphXLabelFormat());
        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.BLUE,                   // line color
                null,                   // point color
                null,
                null);              // fill color

        //The domain axis will always have 7 time stamps
        plot.setDomainStep(XYStepMode.SUBDIVIDE, 7);
        Paint paint = series1Format.getLinePaint();
        paint.setStrokeWidth(2);
        series1Format.setLinePaint(paint);
//        series1Format.set

        plot.addSeries(series, series1Format);

        LineAndPointFormatter lastSeriesFormat = new LineAndPointFormatter(
                Color.TRANSPARENT,                   // line color
                Color.RED,                   // point color
                null,
                new PointLabelFormatter(Color.RED));              // fill color

        lastSeriesFormat.setPointLabeler(new PointLabeler() {
            DecimalFormat df = new DecimalFormat("####.###");

            @Override
            public String getLabel(XYSeries series, int index) {
                return df.format(series.getY(index));
            }
        });
        Paint pain = new Paint();
        pain.setTextAlign(Paint.Align.LEFT);
        pain.setTextSize(30);
        pain.setColor(Color.RED);
        lastSeriesFormat.getPointLabelFormatter().setTextPaint(pain);

        plot.addSeries(lastPointSeries, lastSeriesFormat);

        plot.getBackgroundPaint().setColor(Color.WHITE);

        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);

        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        //For Horizontal lines:
        plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);

        plot.getLayoutManager().remove(plot.getDomainLabelWidget());

        Widget gw = plot.getGraphWidget();

        // FILL mode with values of 0 means fill 100% of container:
        SizeMetrics sm = new SizeMetrics(0, SizeLayoutType.FILL,
                0, SizeLayoutType.FILL);

        gw.setSize(sm);
//
//        // get a handle to the layout manager:
//        LayoutManager lm = plot.getLayoutManager();

        //plot.getGraphWidget().setDomainValueFormat(new GraphXLabelFormat());
        Thread thread = new Thread(new UpdateEverySecond());
        thread.start();
    }


    public class UpdateEverySecond implements Runnable {

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(1000L);
                    Log.d("TAG", " UPDATER EVERY SECOND " + EndPointOfGraph + "  PRICE " + updateListener.getPrice());
                    updateChart(updateListener.getPrice());
                }

            } catch (InterruptedException e) {


            }
        }
    }

    private void updateChart(Float val) {

        if (series != null && val != null && val != 0) {
            //     Log.d(TAG, "Updating the chart " + val);
//            if (currentPriceMarker != null)
//                plot.removeMarker(currentPriceMarker);
            series.removeFirst();
            series.addLast(EndPointOfGraph, val);
            lastPointSeries.removeLast();
            lastPointSeries.addLast(EndPointOfGraph, val);
            plot.redraw();
            EndPointOfGraph++;

//            int merkerPad = 10;
//
//            currentPriceMarker = new XValueMarker(EndPointOfGraph + merkerPad, String.valueOf(val).substring(0, 3));
//
//            currentPriceMarker.getLinePaint().setAlpha(100);
//            currentPriceMarker.getTextPaint().setColor(Color.RED);
//            currentPriceMarker.getTextPaint().setTextSize(25);
//            float pos = plot.getCalculatedMaxY().floatValue() - val;
//            currentPriceMarker.setTextPosition(new YPositionMetric(pos, YLayoutStyle.RELATIVE_TO_TOP));
//            plot.addMarker(currentPriceMarker);


            setupMarkerValues(EndPointOfGraph);


        }
    }

    private void setupMarkerValues(int endPointOfGraph) {

        if (maxValueMarker != null && minValueMarker != null) {
            plot.removeMarker(maxValueMarker);
            plot.removeMarker(minValueMarker);
        }

        maxValueMarker = new XValueMarker(endPointOfGraph, "0.0");
        maxValueMarker.getLinePaint().setAlpha(0);
        maxValueMarker.getTextPaint().setColor(Color.BLACK);
        maxValueMarker.getTextPaint().setTextSize(20);

        maxValueMarker.setTextPosition(new YPositionMetric(0, YLayoutStyle.ABSOLUTE_FROM_TOP));
        plot.addMarker(maxValueMarker);

        minValueMarker = new XValueMarker(endPointOfGraph, "0.0");
        minValueMarker.getLinePaint().setAlpha(0);
        minValueMarker.getTextPaint().setColor(Color.BLACK);
        minValueMarker.getTextPaint().setTextSize(20);

        minValueMarker.setTextPosition(new YPositionMetric(0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM));
        plot.addMarker(minValueMarker);

        maxValueMarker.setText("Max: " + String.valueOf(plot.getCalculatedMaxY().floatValue()));
        minValueMarker.setText("Min: " + String.valueOf(plot.getCalculatedMinY().floatValue()));

    }


    /**
     * Listener class for login response
     */
    public void loadInitialData(Number[] response) {

        values = new Number[mChartTimeFrame];
        time = new Number[mChartTimeFrame];
        time2 = new long[mChartTimeFrame];
        last = new Number[mChartTimeFrame];

        int counter = 0;
        for (int i = response.length - mChartTimeFrame; i < response.length; i++) {
            Log.d("TAG", "Time : " + i + "    Price : " + response[i]);
            values[counter] = response[i];
            time[counter] = counter;
            counter++;
        }
        formatChart();

        populateTimeAxis();


    }

    //
//
//
//
    private String calculateTime(long sinceMidnight) {

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sinceMidnight);

        Log.d("TAG", "WELL :" + formatter.format(calendar.getTime()));

        return formatter.format(calendar.getTime());


    }


    private long getCurrentTime() {

        Calendar rightNow = Calendar.getInstance();
        long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                rightNow.get(Calendar.DST_OFFSET);

        return (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000);


    }

    private void populateTimeAxis() {

        Log.d("TAG", " WHAT IS THIS Populate Axis");
        int offSet = mChartTimeFrame;

        time3 = new String[time2.length];

        for (int i = 0; i < time.length; i++) {
            time2[i] = getCurrentTime() - offSet * 1000;
            offSet--;
            Log.d("TAG", " WHAT IS THIS Populate Axis" + calculateTime(time2[i]));
            time3[i] = calculateTime(time2[i]);


        }
    }

    //
//
    private class GraphXLabelFormat extends Format {

        @Override
        public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
            int parsedInt = Math.round(Float.parseFloat(object.toString()));
            String labelString = time3[parsedInt];

            buffer.append(labelString);
            return buffer;
        }

        @Override
        public Object parseObject(String string, ParsePosition position) {
            return Arrays.asList(time3).indexOf(string);
        }
    }
}




