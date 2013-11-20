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

package net.jaqpot.netcounter.com;

import java.util.Map;


/**
 * @author Louis
 * 
 */
public class OSCApi {

//	private static String TAG = "net.jaqpot.netcounter.com.OSCApi";


	public static void broadcastMsg(Map<String, Object> payload, Handler handler) {

		SendOSC.of(payload, handler).execute();

	}
	
	

	public interface Handler {
		void callback(String struct);

		void onError(Error error);
	}

}
