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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.rw.barcharttest.R;
import com.rw.barcharttest.usagechart.ChartData.Entry;
import com.rw.barcharttest.usagechart.ChartSweepView.OnSweepListener;

import java.util.Date;

/**
 * Specific {@link ChartView} that displays {@link ChartBarGraphView} along
 * with {@link ChartSweepView} for inspection ranges and warning/limits.
 */
public class ChartDataUsageView extends ChartView implements ChartConstants {

	private final static String TAG = ChartDataUsageView.class.getSimpleName();
	
    private static final int MSG_UPDATE_AXIS = 100;
    private static final long DELAY_MILLIS = 250;

    private static final boolean LIMIT_SWEEPS_TO_VALID_DATA = false;

    private ChartGridView mGrid;
    private ChartBarGraphView mBarChart;

    private ChartData mData; // added -- Trey

    private ChartSweepView mSweepLeft;
    private ChartSweepView mSweepRight;
    private ChartSweepView mSweepAverage;

    private Handler mHandler;

    /** Current maximum value of {@link #mVert}. */
    private long mVertMax;

    public interface DataUsageChartListener {
        void onInspectRangeChanged();
    }

    private DataUsageChartListener mListener;

    public ChartDataUsageView(Context context) {
        this(context, null, 0);
    }

    public ChartDataUsageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartDataUsageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(new TimeAxis(), new InvertedChartAxis(new DataAxis()));

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final ChartSweepView sweep = (ChartSweepView) msg.obj;
                updateVertAxisBounds(sweep);

                // we keep dispatching repeating updates until sweep is dropped
                sendUpdateAxisDelayed(sweep, true);
            }
        };
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mGrid = (ChartGridView) findViewById(R.id.grid);
        mBarChart = (ChartBarGraphView) findViewById(R.id.bar_graph);

        mSweepLeft = (ChartSweepView) findViewById(R.id.sweep_left);
        mSweepRight = (ChartSweepView) findViewById(R.id.sweep_right);
        mSweepAverage = (ChartSweepView) findViewById(R.id.sweep_average);

        // prevent sweeps from crossing each other
        mSweepLeft.setValidRangeDynamic(null, mSweepRight);
        mSweepRight.setValidRangeDynamic(mSweepLeft, null);

        // mark neighbors for checking touch events against
        mSweepLeft.setNeighbors(mSweepRight);
        mSweepRight.setNeighbors(mSweepLeft);

        mSweepLeft.addOnSweepListener(mHorizListener);
        mSweepRight.addOnSweepListener(mHorizListener);

        // TODO: make time sweeps adjustable through dpad
        mSweepLeft.setClickable(false);
        mSweepLeft.setFocusable(false);
        mSweepRight.setClickable(false);
        mSweepRight.setFocusable(false);

        // tell everyone about our axis
        mGrid.init(mHoriz, mVert);
        mBarChart.init(mHoriz, mVert);
        mSweepLeft.init(mHoriz);
        mSweepRight.init(mHoriz);
        mSweepAverage.init(mVert);

        // We show the average conditionally later.
        mSweepAverage.setVisibility(View.INVISIBLE);
        
        setActivated(false);
    }

    public void setListener(DataUsageChartListener listener) {
        mListener = listener;
    }

    public void bindChartData(ChartData data) {
    	mBarChart.bindChartData(data);
    	mData = data;
        
    	long avg = 1;
    	if (data.entries.size() > 0) {
    		long sum = 0;
	    	for (Entry entry : data.entries) {
	    		sum += entry.totalBytes;
	    	}
	    	avg = sum / data.entries.size();
    	}
      	mSweepAverage.setValue(avg);
      	mSweepAverage.setVisibility(View.VISIBLE);
        
        updateVertAxisBounds(null);
        updatePrimaryRange();
        requestLayout();
    }

    public ChartData getChartData() {
    	return mData;
    }

    /**
     * Update {@link #mVert} to both show data proper data.
     */
    private void updateVertAxisBounds(ChartSweepView activeSweep) {
        final long max = mVertMax;

        long newMax = 0;
        if (activeSweep != null) {
            final int adjustAxis = activeSweep.shouldAdjustAxis();
            if (adjustAxis > 0) {
                // hovering around upper edge, grow axis
                newMax = max * 11 / 10;
            } else if (adjustAxis < 0) {
                // hovering around lower edge, shrink axis
                newMax = max * 9 / 10;
            } else {
                newMax = max;
            }
        }

        // always show known data and policy lines
        final long maxVisible = mBarChart.getMaxVisible() * 12 / 10;
        final long maxDefault = Math.max(maxVisible, MB_IN_BYTES); // Default used to be 50 MB. -- Trey
        newMax = Math.max(maxDefault, newMax);

        // only invalidate when vertMax actually changed
        if (newMax != mVertMax) {
            mVertMax = newMax;

            final boolean changed = mVert.setBounds(0L, newMax);
            mSweepAverage.setValidRange(0L, newMax);

            if (changed) {
                mBarChart.invalidate();
            }

            mGrid.invalidate();

            // since we just changed axis, make sweep recalculate its value
            if (activeSweep != null) {
                activeSweep.updateValueFromPosition();
            }
        }
    }

    private OnSweepListener mHorizListener = new OnSweepListener() {
        @Override
        public void onSweep(ChartSweepView sweep, boolean sweepDone) {
            updatePrimaryRange();

            // update detail list only when done sweeping
            if (sweepDone && mListener != null) {
                mListener.onInspectRangeChanged();
            }
        }

        @Override
        public void requestEdit(ChartSweepView sweep) {
            // ignored
        }
    };

    private void sendUpdateAxisDelayed(ChartSweepView sweep, boolean force) {
        if (force || !mHandler.hasMessages(MSG_UPDATE_AXIS, sweep)) {
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(MSG_UPDATE_AXIS, sweep), DELAY_MILLIS);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isActivated()) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                return true;
            }
            case MotionEvent.ACTION_UP: {
                setActivated(true);
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public long getInspectStart() {
        return mSweepLeft.getValue();
    }

    public long getInspectEnd() {
        return mSweepRight.getValue();
    }

    private long getHistoryStart() {
        return mData != null ? mData.getStart() : Long.MAX_VALUE;
    }

    private long getHistoryEnd() {
        return mData != null ? mData.getEnd() : Long.MIN_VALUE;
    }

    /**
     * Set the exact time range that should be displayed.  Moves inspection ranges to be the
     * last "week" of available data, without triggering listener events.
     */
    public void setVisibleRange(long visibleStart, long visibleEnd) {
    	String msg = "Setting visible range to " + new Date(visibleStart) + " - " + new Date(visibleEnd) + ".";
		Log.d(TAG, msg);
    	
        final boolean changed = mHoriz.setBounds(visibleStart, visibleEnd);
        mGrid.setBounds(visibleStart, visibleEnd);
        mBarChart.setBounds(visibleStart, visibleEnd);

        final long historyStart = getHistoryStart();
        final long historyEnd = getHistoryEnd();

        final long validStart = historyStart == Long.MAX_VALUE ? visibleStart
                : Math.max(visibleStart, historyStart);
        final long validEnd = historyEnd == Long.MIN_VALUE ? visibleEnd
                : Math.min(visibleEnd, historyEnd);

        if (LIMIT_SWEEPS_TO_VALID_DATA) {
            // prevent time sweeps from leaving valid data
            mSweepLeft.setValidRange(validStart, validEnd);
            mSweepRight.setValidRange(validStart, validEnd);
        } else {
            mSweepLeft.setValidRange(visibleStart, visibleEnd);
            mSweepRight.setValidRange(visibleStart, visibleEnd);
        }

        mSweepLeft.setValue(visibleStart);
        mSweepRight.setValue(visibleEnd);

        // Date template is used for sizing calculations.
        mSweepLeft.setLabelTemplate(R.string.date_template);
        mSweepRight.setLabelTemplate(R.string.date_template);

        requestLayout();
        if (changed) {
            mBarChart.invalidate();
        }

        updateVertAxisBounds(null);
        updatePrimaryRange();
    }

    private void updatePrimaryRange() {
        final long left = mSweepLeft.getValue();
        final long right = mSweepRight.getValue();
        mBarChart.setPrimaryRange(left, right);
    }
    
}
