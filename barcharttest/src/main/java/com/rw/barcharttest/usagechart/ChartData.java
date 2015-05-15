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

import java.util.ArrayList;
import java.util.List;

public class ChartData {
	
	public static class Entry {
		public long date;
		public long totalBytes;
	}
	
	// The time range for this data.
	private long start;
	private long end;
	
	List<Entry> entries;
	
	public ChartData() {
		entries = new ArrayList<Entry>();
		
		this.start = Long.MAX_VALUE;
		this.end = Long.MIN_VALUE;
	}

	public int size() {
		return entries.size();
	}
	
	public Entry getEntry(int index) {
		Entry entry = null;
		
		if (index >= 0 && index < entries.size()) {
			entry = entries.get(index);
		}
		
		return entry;
	}
		
	public void addEntries(List<Entry> entries) {
		this.entries.addAll(entries);
	}
	
	public List<Entry> getEntries(long start, long end) {
		List<Entry> list = new ArrayList<Entry>();

		for (Entry entry : entries) {
			if (entry.date >= start && entry.date <= end) {
				list.add(entry);
			}

			// Entries should be ordered from oldest to newest.
			if (end < entry.date) {
				break;
			}
		}
		
		return list;
	}
	
	/**
	 * Entries should be ordered by date from oldest to newest.
	 * 
	 * @param timestamp
	 * @return
	 */
	public int getIndexBefore(long timestamp) {
		int before = -1;
		for (int i = 0; i < entries.size(); i++) {
			Entry e = entries.get(i);
			if (timestamp <= e.date) {
				before = i;
				break;
			}
		}
		return MathUtils.constrain(before, 0, entries.size() - 1);
	}
	
	/**
	 * Entries should be ordered by date from oldest to newest.
	 * 
	 * @param timestamp
	 * @return
	 */
	public int getIndexAfter(long timestamp) {
		int after = -1;
		
		for (int i = entries.size() - 1; i >= 0; i--) {
			Entry e = entries.get(i);
			if (timestamp >= e.date) {
				after = i;
				break;
			}
		}
		
		return MathUtils.constrain(after, 0, entries.size() - 1);
	}
	
	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getVerticalMax() {
		long max = 0;

		for (Entry entry : entries) {
			max = Math.max(entry.totalBytes, max);
		}
		
	    return max;
    }

}
