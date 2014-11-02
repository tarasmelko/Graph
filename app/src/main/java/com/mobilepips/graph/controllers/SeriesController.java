package com.mobilepips.graph.controllers;

import android.graphics.Color;
import android.graphics.Paint;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Taras on 02.11.2014.
 */
public class SeriesController extends SimpleXYSeries {

    private LineAndPointFormatter mFormatter;

    public SeriesController(List<? extends Number> xVals, List<? extends Number> yVals, final String decimalType, String title, int color, int textSize) {
        super(xVals, yVals, title);
        mFormatter = new LineAndPointFormatter(Color.TRANSPARENT, color, null, new PointLabelFormatter(color));
        Paint pain = new Paint();
        pain.setTextAlign(Paint.Align.LEFT);
        pain.setTextSize(textSize);
        pain.setColor(color);
        mFormatter.getPointLabelFormatter().setTextPaint(pain);
        mFormatter.setPointLabeler(new PointLabeler() {
            DecimalFormat df = new DecimalFormat(decimalType);

            @Override
            public String getLabel(XYSeries series, int index) {
                return df.format(series.getY(index));
            }
        });
    }

    public SeriesController(List<? extends Number> xVals, List<? extends Number> yVals) {
        super(xVals, yVals, "main");
        mFormatter = new LineAndPointFormatter(Color.BLUE, null, null, null);
        Paint paint = mFormatter.getLinePaint();
        paint.setStrokeWidth(2);
        mFormatter.setLinePaint(paint);
    }

    public SeriesController(List<? extends Number> xVals, List<? extends Number> yVals, String title, int color, int textSize, final String markerText) {
        super(xVals, yVals, title);
        mFormatter = new LineAndPointFormatter(Color.TRANSPARENT, color, null, new PointLabelFormatter(color));
        Paint pain = new Paint();
        pain.setTextAlign(Paint.Align.LEFT);
        pain.setTextSize(textSize);
        pain.setColor(color);
        mFormatter.getPointLabelFormatter().setTextPaint(pain);
        mFormatter.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                return markerText;
            }
        });
    }

    public LineAndPointFormatter getmFormatter() {
        return mFormatter;
    }


}
