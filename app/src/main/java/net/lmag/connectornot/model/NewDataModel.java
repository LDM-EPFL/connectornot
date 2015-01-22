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

package net.lmag.connectornot.model;


/**
 * @author Louis
 *
 */
public class NewDataModel {
	
	private long mBytes = 0;
	
	private int mConvDuration = 0;
	
	private int mSMS = 0;
	
	private int mSignalStrength = 0;
	
	private float mCellDistance = (float) 0.0;
	
	private int mMinor = 0;
	
	private String mPhoneID = null;
	
	private String mTS;

    public String getRPLoc() {
        return mRPLoc;
    }

    public void setRPLoc(String mRPLoc) {
        this.mRPLoc = mRPLoc;
    }

    private String mRPLoc;
	
//	private final SQLiteOpenHelper mHelper;
	
	public NewDataModel(long bytes, int convDuration, int sms, int signalStrength, float cellDist, int minor,
                        String rpLoc) {
		mBytes = bytes;
		mConvDuration = convDuration;
		mSMS = sms;
		mSignalStrength = signalStrength;
		mCellDistance = cellDist;
		mMinor = minor;
        mRPLoc = rpLoc;
	}
	
	public long getBytes() {
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
	
	public int getMinor() {
		return mMinor;
	}

	public NewDataModel subtract(NewDataModel m2) {
		return (m2 != null) ? new NewDataModel(
				Math.abs(this.mBytes - m2.mBytes),
                Math.abs(this.mConvDuration - m2.mConvDuration),
                Math.abs(this.mSMS - m2.mSMS),
                Math.abs(this.mSignalStrength - m2.mSignalStrength),
                Math.abs(this.mCellDistance - m2.mCellDistance),
				mMinor,
                mRPLoc) : this;
	}
	
	public NewDataModel setPhoneID(String phoneID) {
		mPhoneID = phoneID;
		return this;
	}
	
	public String getPhoneID() {
		return mPhoneID;
	}
	
	public String getTS() {
		return mTS;
	}
	
	public void setTS(String ts) {
		mTS = ts;
	}
	

}
