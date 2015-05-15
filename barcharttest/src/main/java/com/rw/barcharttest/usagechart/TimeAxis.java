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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.format.Time;

public class TimeAxis implements ChartAxis {
    private static final int FIRST_DAY_OF_WEEK = Calendar.getInstance().getFirstDayOfWeek() - 1;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
    
    private long mMin;
    private long mMax;
    private float mSize;

    public TimeAxis() {
        final long currentTime = System.currentTimeMillis();
        setBounds(currentTime - DateUtils.DAY_IN_MILLIS * 30, currentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mMin, mMax, mSize);
    }

    @Override
    public boolean setBounds(long min, long max) {
        if (mMin != min || mMax != max) {
            mMin = min;
            mMax = max;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean setSize(float size) {
        if (mSize != size) {
            mSize = size;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public float convertToPoint(long value) {
        return (mSize * (value - mMin)) / (mMax - mMin);
    }

    @Override
    public long convertToValue(float point) {
        return (long) (mMin + ((point * (mMax - mMin)) / mSize));
    }

    @Override
    public long buildLabel(Resources res, SpannableStringBuilder builder, long value) {
        // TODO: convert to better string
//        builder.replace(0, builder.length(), Long.toString(value));
    	builder.replace(0, builder.length(), formatter.format(value));
    	return value;
    }

    @Override
    public float[] getTickPoints() {
        final float[] ticks = new float[32];
        int i = 0;

        // tick mark for first day of each week
        final Time time = new Time();
        time.set(mMax);
        time.monthDay -= time.weekDay - FIRST_DAY_OF_WEEK;
        time.hour = time.minute = time.second = 0;

        time.normalize(true);
        long timeMillis = time.toMillis(true);
        while (timeMillis > mMin) {
            if (timeMillis <= mMax) {
                ticks[i++] = convertToPoint(timeMillis);
            }
            time.monthDay -= 7;
            time.normalize(true);
            timeMillis = time.toMillis(true);
        }

        return Arrays.copyOf(ticks, i);
    }

    @Override
    public int shouldAdjustAxis(long value) {
        // time axis never adjusts
        return 0;
    }
}
