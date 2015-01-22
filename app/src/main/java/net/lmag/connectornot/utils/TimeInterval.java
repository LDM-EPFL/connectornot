package net.lmag.connectornot.utils;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class TimeInterval {
	//We could also specify days of the week in this class.
	
	private int mBegin;
	private int mEnd;
	
	public TimeInterval(String begin, String end) throws TimeIntervalException {
		//TODO check that the parsing goes well.
		mBegin = DateTime.parse(begin,
				ISODateTimeFormat.timeParser()).getMillisOfDay();
		mEnd = DateTime.parse(end,
				ISODateTimeFormat.timeParser()).getMillisOfDay();
		
		if (mBegin >= mEnd) {
			throw new TimeIntervalException("It must be true that begin < end.");
		}
	}
	
	/**
	 * 
	 * @param time - time to test
	 * @return 0 if in the interval.<br>
	 * A negative number representing the number of milis before interval.<br>
	 * A positive number representing the number of milis after the interval.
	 * @throws TimeIntervalException If the given time is null.
	 */
	public int inInterval(DateTime time) throws TimeIntervalException {
		if (time == null) {
			throw new TimeIntervalException("TimeInterval was given a null time");
		}
		
		int timeInMilis = time.getMillisOfDay();
		
		if (timeInMilis < mBegin) {
			//Return a negative # when before the interval
			return timeInMilis - mBegin;
		} else if (mBegin <= timeInMilis && timeInMilis < mEnd) {
			//Return 0 when in the interval
			return 0;
		} else {
			//return positive # when after the interval
			return timeInMilis - mEnd;
		}
		
	}
	
	public int getBegin() {
		return mBegin;
	}
	
	public int getEnd() {
		return mEnd;
	}
	
	
}
