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

package com.rw.barcharttest;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rw.barcharttest.usagechart.ChartData;
import com.rw.barcharttest.usagechart.ChartData.Entry;
import com.rw.barcharttest.usagechart.ChartDataUsageView;
import com.rw.barcharttest.usagechart.ChartDataUsageView.DataUsageChartListener;
import com.rw.barcharttest.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends FragmentActivity {

	private static SimpleDateFormat sDateFormatter = new SimpleDateFormat("MMM dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }
	}

	public static class BarChartFragment extends Fragment implements DataUsageChartListener {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View v = inflater.inflate(R.layout.fragment_data_usage, container, false);
			v.setBackgroundColor(getResources().getColor(R.color.background));
			
			// Set to Midnight a month ago.
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.MONTH, -1);
			
			// Generate some quick dummy data and bind it to our chart.
			Random random = new Random();
			List<Entry> list = new ArrayList<Entry>(30);
			for (int i = 0; i < 30; i++) {
				Entry entry = new Entry();
				entry.date = cal.getTimeInMillis();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				// Random number of megabytes used for the day.
				entry.totalBytes = (random.nextInt(1000) + 1) * 1024 * 2; 
				list.add(entry);
			}
			
			long start = list.get(0).date;
			long end = list.get(list.size() - 1).date;
			
			// Load the chart data object.
			ChartData data = new ChartData();
			data.addEntries(list);
			data.setStart(start);
			data.setEnd(end);
			
			// Bind it to the chart.
			ChartDataUsageView chart = (ChartDataUsageView) v.findViewById(R.id.chart);
			chart.bindChartData(data);
			chart.setVisibleRange(start - (DateUtils.DAY_IN_MILLIS / 2), end + DateUtils.DAY_IN_MILLIS);
			chart.setListener(this);
			return v;
		}
		
		@Override
        public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        
	        // Initialize the summary message.
	     	onInspectRangeChanged();
        }

		@Override
	    public void onInspectRangeChanged() {
			View v = getView();
	        if (v != null) {
	        	ChartDataUsageView chart = (ChartDataUsageView) v.findViewById(R.id.chart);
	        	TextView summary = (TextView) v.findViewById(R.id.summary_text_view);
	        	if (chart != null && summary != null) {
		        	long start = chart.getInspectStart();
		        	long end = chart.getInspectEnd();

					String startStr = sDateFormatter.format(start);
					String endStr = sDateFormatter.format(end);

		        	long totalUsage = 0;
		        	List<Entry> selectedEntries = chart.getChartData().getEntries(start, end);
		        	for (Entry entry : selectedEntries) {
		        		totalUsage += entry.totalBytes;
		        	}

					summary.setText(getString(R.string.data_usage_between, startStr, endStr,
							StringUtils.humanReadableByteCount(totalUsage, true)));
	        	}
	        }
	    }

	}
	
}
