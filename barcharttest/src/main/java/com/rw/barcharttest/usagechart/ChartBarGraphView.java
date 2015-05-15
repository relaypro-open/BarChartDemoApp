/*
 * Copyright (C) 2015 Republic Wireless
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rw.barcharttest.usagechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.rw.barcharttest.R;

/**
 * Bar chart view.  This class is used to draw the bars on the chart.
 */
public class ChartBarGraphView extends View {
    private static final String TAG = "ChartBarGraphView";
    private static final boolean LOGD = false;

    private ChartAxis mHoriz;
    private ChartAxis mVert;

    private Paint mPaint;
    private int mSelectedColor;
    private int mDefaultColor;

    private ChartData mData;

    private long mStart;
    private long mEnd;

    private long mPrimaryLeft;
    private long mPrimaryRight;
    
    private long mMax;

    public ChartBarGraphView(Context context) {
        this(context, null, 0);
    }

    public ChartBarGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartBarGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ChartBarGraphView, defStyle, 0);

        mSelectedColor = a.getColor(R.styleable.ChartBarGraphView_selectedColor,
                getResources().getColor(R.color.selected_bar));
        mDefaultColor = a.getColor(R.styleable.ChartBarGraphView_defaultColor,
                getResources().getColor(R.color.default_bar));

        initPaint();
        setWillNotDraw(false);

        a.recycle();
    }

    void init(ChartAxis horiz, ChartAxis vert) {
        mHoriz = Preconditions.checkNotNull(horiz, "missing horiz");
        mVert = Preconditions.checkNotNull(vert, "missing vert");
    }

    public void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mSelectedColor);
        mPaint.setStyle(Style.FILL_AND_STROKE);
    }

    public void bindChartData(ChartData data) {
    	mData = data;
    	invalidate();
    }

    public void setBounds(long start, long end) {
        mStart = start;
        mEnd = end;
    }

    /**
     * Set the range to paint with {@link #mSelectedColor}, leaving the remaining
     * area to be painted with {@link #mDefaultColor}.
     */
    public void setPrimaryRange(long left, long right) {
        mPrimaryLeft = left;
        mPrimaryRight = right;
        invalidate();
    }

    public long getMaxVisible() {
        final long maxVisible = mMax;
        if (maxVisible <= 0 && mData != null) {
            // haven't generated bars yet; fall back to raw data.
            return mData.getVerticalMax();
        } else {
            return maxVisible;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Determine the area selected by the left and right sweeps.
        final float primaryLeftPoint = mHoriz.convertToPoint(mPrimaryLeft);
        final float primaryRightPoint = mHoriz.convertToPoint(mPrimaryRight);

        // Draw the data using a bar chart.
        drawBarChart(canvas, primaryLeftPoint, primaryRightPoint);
    }
    
    private void drawBarChart(Canvas canvas, float primaryLeftPoint, float primaryRightPoint) {
    	if (LOGD) Log.d(TAG, "drawBarChart()");
    	
        // Bail when not enough data to render.
        if (mData == null) {
            return;
        }

        mMax = 0;
        final int height = getHeight();
        
        ChartData.Entry entry = null;

        final Rect rect = new Rect();
        final int start = mData.getIndexBefore(mStart);
        final int end = mData.getIndexAfter(mEnd);
        for (int i = start; i <= end; i++) {
            entry = mData.getEntry(i);
            mMax = Math.max(entry.totalBytes, mMax);
            
            final long startTime = entry.date;
            final long endTime = startTime + (DateUtils.DAY_IN_MILLIS / 2);
            
            final float startX = mHoriz.convertToPoint(startTime);
            final float endX = mHoriz.convertToPoint(endTime);
            final float y = mVert.convertToPoint(entry.totalBytes);
            
            // skip until we find first data to show on screen
            if (endX < 0) continue;
            
            // Set bar bounds
            int left = (int) startX;
            int top = (int) (y);
            int right = (int) endX;
            int bottom = height;
            
            rect.set(left, top, right, bottom);

            /*
             * Draw bar.  Bars outside of the the selected area between the left and right
             * sweeps are drawn differently to indicate that they aren't included.
             */
            if (startX >= primaryLeftPoint && endX <= primaryRightPoint) {
            	// Within the selected area.
            	mPaint.setColor(mSelectedColor);
            } else {
            	// Outside of the selected area.
            	mPaint.setColor(mDefaultColor);
            }
 			
 			canvas.drawRect(rect, mPaint);
        }
        
    }
    
}
