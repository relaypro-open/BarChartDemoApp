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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.rw.barcharttest.R;

public class AverageSweepView extends ChartSweepView {

	private final static boolean DEBUG = false;
	
	private Drawable icon;
	
	public AverageSweepView(Context context) {
        this(context, null);
    }

    public AverageSweepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AverageSweepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        icon = context.getResources().getDrawable(R.drawable.ic_info);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	// Prevent the user from changing the position of this sweep.
        return event.getAction() == MotionEvent.ACTION_DOWN;
    }
    	
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the icon on the left of the chart.
        if (icon != null) {
        	int iconHeight = icon.getIntrinsicHeight();
        	int iconWidth = icon.getIntrinsicWidth();
        	
        	// Put some space between the icon and the sweep line.
        	float padding = getResources().getDisplayMetrics().density * 25;
        	int x = (int) -padding;
        	int y = (int) (getHeight() / 2) - ((iconHeight / 2));

            icon.setBounds(x, y, x + iconWidth, y + iconHeight);
        	icon.draw(canvas);

        	if (DEBUG) {
	        	Paint paint = new Paint();
	        	paint.setColor(Color.RED);
	            paint.setStrokeWidth(1f);
	            paint.setStyle(Style.STROKE);
	            paint.setColor(Color.BLACK);
	            // Draw a border around the icon.
	            canvas.drawRect(x, y, x + iconWidth, y + iconHeight, paint);
	        	
	            // Draw border around the sweep.	            
	            paint.setColor(Color.RED);
	        	canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        	}
        }
        
    }

}
