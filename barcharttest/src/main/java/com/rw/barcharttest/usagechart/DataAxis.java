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

import java.util.Objects;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.rw.barcharttest.R;

public class DataAxis implements ChartAxis, ChartConstants {
    private long mMin;
    private long mMax;
    private float mSize;

    private static final boolean LOG_SCALE = false;

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
        if (LOG_SCALE) {
            // derived polynomial fit to make lower values more visible
            final double normalized = ((double) value - mMin) / (mMax - mMin);
            final double fraction = Math.pow(10,
                    0.36884343106175121463 * Math.log10(normalized) + -0.04328199452018252624);
            return (float) (fraction * mSize);
        } else {
            return (mSize * (value - mMin)) / (mMax - mMin);
        }
    }

    @Override
    public long convertToValue(float point) {
        if (LOG_SCALE) {
            final double normalized = point / mSize;
            final double fraction = 1.3102228476089056629
                    * Math.pow(normalized, 2.7111774693164631640);
            return (long) (mMin + (fraction * (mMax - mMin)));
        } else {
            return (long) (mMin + ((point * (mMax - mMin)) / mSize));
        }
    }

    private static final Object sSpanSize = new Object();
    private static final Object sSpanUnit = new Object();

    @Override
    public long buildLabel(Resources res, SpannableStringBuilder builder, long value) {

        final CharSequence unit;
        final long unitFactor;
        if (value < 1000 * MB_IN_BYTES) {
//            unit = res.getText(com.android.internal.R.string.megabyteShort);
        	unit = res.getString(R.string.mb);
            unitFactor = MB_IN_BYTES;
        } else {
//            unit = res.getText(com.android.internal.R.string.gigabyteShort);
            unit = res.getString(R.string.gb);
            unitFactor = GB_IN_BYTES;
        }

        final double result = (double) value / unitFactor;
        final double resultRounded;
        final CharSequence size;

        if (result < 10) {
            size = String.format("%.1f", result);
            resultRounded = (unitFactor * Math.round(result * 10)) / 10;
        } else {
            size = String.format("%.0f", result);
            resultRounded = unitFactor * Math.round(result);
        }

        setText(builder, sSpanSize, size, "^1");
        setText(builder, sSpanUnit, unit, "^2");

        return (long) resultRounded;
    }

    @Override
    public float[] getTickPoints() {
        final long range = mMax - mMin;

        // target about 16 ticks on screen, rounded to nearest power of 2
//        final long tickJump = roundUpToPowerOfTwo(range / 16);
        final long tickJump = roundUpToPowerOfTwo(range / 8); // Show about 8 bars on screen.
        final int tickCount = (int) (range / tickJump);
        final float[] tickPoints = new float[tickCount];
        long value = mMin;
        for (int i = 0; i < tickPoints.length; i++) {
            tickPoints[i] = convertToPoint(value);
            value += tickJump;
        }

        return tickPoints;
    }

    @Override
    public int shouldAdjustAxis(long value) {
        final float point = convertToPoint(value);
        if (point < mSize * 0.1) {
            return -1;
        } else if (point > mSize * 0.85) {
            return 1;
        } else {
            return 0;
        }
    }
    
    private static void setText(
            SpannableStringBuilder builder, Object key, CharSequence text, String bootstrap) {
        int start = builder.getSpanStart(key);
        int end = builder.getSpanEnd(key);
        if (start == -1) {
            start = TextUtils.indexOf(builder, bootstrap);
            end = start + bootstrap.length();
            if (start >= 0 && end < bootstrap.length()) {
                builder.setSpan(key, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        builder.replace(start, end, text);
    }
    
    private static long roundUpToPowerOfTwo(long i) {
        // NOTE: borrowed from Hashtable.roundUpToPowerOfTwo()

        i--; // If input is a power of two, shift its high-order bit right

        // "Smear" the high-order bit all the way to the right
        i |= i >>>  1;
        i |= i >>>  2;
        i |= i >>>  4;
        i |= i >>>  8;
        i |= i >>> 16;
        i |= i >>> 32;

        i++;

        return i > 0 ? i : Long.MAX_VALUE;
    }
}
