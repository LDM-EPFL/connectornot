/*
 * Copyright (C) 2009 Cyril Jaquier
 *
 * This file is part of NetCounter.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.jaqpot.netcounter.model;


/**
 * @author Louis
 *
 */
public class NewDataModel {
	
//	private static NewDataModel sInitialState;
	
	private static Integer sLock = 0;
	
//	private static long sSentBytes = 0;
	
	private int mBytes = 0;
	
	private int mConvDuration = 0;
	
	private int mSMS = 0;
	
	private int mSignalStrength = 0;
	
	private float mCellDistance = (float) 0.0;
	
//	private final SQLiteOpenHelper mHelper;
	
	public NewDataModel(int bytes, int convDuration, int sms, int signalStrength, float cellDist) {
		mBytes = bytes;
		mConvDuration = convDuration;
		mSMS = sms;
		mSignalStrength = signalStrength;
		mCellDistance = cellDist;
	}
	
	public int getBytes() {
		return mBytes;
	}
	
	/**
	 * @return the mConvDuration
	 */
	public int getConvDuration() {
		return mConvDuration;
	}

	/**
	 * @return the mSMS
	 */
	public int getSMS() {
		return mSMS;
	}

	/**
	 * @return the mSignalStrength
	 */
	public int getSignalStrength() {
		return mSignalStrength;
	}

	/**
	 * @return the mCellDistance
	 */
	public float getCellDistance() {
		return mCellDistance;
	}

//	public static NewDataModel getDiffToStart(NewDataModel m2) {
//		synchronized (sLock) {			
//			return (sInitialState == null || m2 == null) ? null :
//				new NewDataModel(
//					Math.abs(sInitialState.mBytes - m2.mBytes),
//					Math.abs(sInitialState.mConvDuration - m2.mConvDuration),
//					Math.abs(sInitialState.mSMS - m2.mSMS),
//					Math.abs(sInitialState.mSignalStrength - m2.mSignalStrength),
//					Math.abs(sInitialState.mCellDistance - m2.mCellDistance));
//		}
//	}
	
	public NewDataModel subtract(NewDataModel m2) {
		return (m2 != null) ? new NewDataModel(
				this.mBytes - m2.mBytes,
				this.mConvDuration - m2.mConvDuration,
				this.mSMS - m2.mSMS,
				this.mSignalStrength - m2.mSignalStrength,
				this.mCellDistance - m2.mCellDistance) : this;
	}
	
//	public static void initialize(int bytes, int convDuration, int sms, int signalStrength, float cellDist) {
//		synchronized (sLock) {
//			sInitialState = new NewDataModel(bytes, convDuration, sms, signalStrength, cellDist);
//		}
//	}
	
//	public static void sentBytes(int bytes) {
//		synchronized(sLock) {
//			sInitialState.mBytes += bytes;
//		}
//	}

}
